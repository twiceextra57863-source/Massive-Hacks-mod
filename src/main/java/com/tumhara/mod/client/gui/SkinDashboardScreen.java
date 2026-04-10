package com.tumhara.mod.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;

public class SkinDashboardScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("SkinChanger");
    private static final File CONFIG_DIR = new File("config/skinchanger");
    private static final File CURRENT_SKIN_FILE = new File(CONFIG_DIR, "current_skin.json");
    
    private TextFieldWidget urlField;
    private TextFieldWidget usernameField;
    private final Screen parent;
    private String statusMessage = "§7Welcome to Skin Studio";
    private int statusColor = 0xAAAAAA;
    private boolean isLoading = false;
    private int activeTab = 0;
    private String currentSkinPreview = "Steve";
    private float statueRotation = 0;
    
    // Colors - Full opacity
    private static final int COLOR_BORDER = 0xFFD4AF37;
    private static final int COLOR_BG = 0xFF1A1A1A;
    private static final int COLOR_PANEL = 0xFF2D2D2D;
    private static final int COLOR_PREVIEW_BG = 0xFF3A3A3A;
    
    public SkinDashboardScreen(Screen parent) {
        super(Text.literal(""));
        this.parent = parent;
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        loadCurrentSkin();
    }
    
    private void loadCurrentSkin() {
        if (CURRENT_SKIN_FILE.exists()) {
            try (FileReader reader = new FileReader(CURRENT_SKIN_FILE)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("current_skin")) {
                    currentSkinPreview = json.get("current_skin").getAsString();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load current skin", e);
            }
        }
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // URL Field - Right side
        this.urlField = new TextFieldWidget(
            this.textRenderer,
            centerX + 20,
            centerY + 50,
            200,
            22,
            Text.literal("https://example.com/skin.png")
        );
        this.urlField.setMaxLength(512);
        this.urlField.setVisible(true);
        this.addDrawableChild(this.urlField);
        
        // Username Field - Right side
        this.usernameField = new TextFieldWidget(
            this.textRenderer,
            centerX + 20,
            centerY + 50,
            200,
            22,
            Text.literal("Steve")
        );
        this.usernameField.setMaxLength(64);
        this.usernameField.setVisible(false);
        this.addDrawableChild(this.usernameField);
        
        // Tab Buttons - Right side
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(activeTab == 0 ? "§6§lURL" : "§7URL"),
            button -> {
                activeTab = 0;
                urlField.setVisible(true);
                usernameField.setVisible(false);
                refreshButtons();
            }
        ).dimensions(centerX + 20, centerY + 18, 95, 24).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(activeTab == 1 ? "§6§lNAME" : "§7NAME"),
            button -> {
                activeTab = 1;
                urlField.setVisible(false);
                usernameField.setVisible(true);
                refreshButtons();
            }
        ).dimensions(centerX + 125, centerY + 18, 95, 24).build());
        
        // Action Buttons - Right side
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§aAPPLY"),
            button -> applySkin()
        ).dimensions(centerX + 20, centerY + 90, 95, 32).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§cRESET"),
            button -> resetSkin()
        ).dimensions(centerX + 125, centerY + 90, 95, 32).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§7CLOSE"),
            button -> close()
        ).dimensions(centerX + 70, centerY + 135, 95, 24).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§bOPEN FOLDER"),
            button -> openConfigFolder()
        ).dimensions(centerX + 50, centerY + 170, 140, 20).build());
    }
    
    private void refreshButtons() {
        this.clearChildren();
        this.init();
    }
    
    private void applySkin() {
        isLoading = true;
        statusMessage = "§eApplying skin...";
        statusColor = 0xFFFF55;
        
        String input = activeTab == 0 ? urlField.getText() : usernameField.getText();
        
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                MinecraftClient.getInstance().execute(() -> {
                    saveCurrentSkin(input);
                    currentSkinPreview = input;
                    statusMessage = "§a✓ Skin applied! Restart to see changes";
                    statusColor = 0x55FF55;
                    isLoading = false;
                    
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().player.sendMessage(
                            Text.literal("§a✓ Skin changed to §6" + input), true
                        );
                    }
                });
            } catch (Exception e) {
                MinecraftClient.getInstance().execute(() -> {
                    statusMessage = "§cFailed to apply skin";
                    statusColor = 0xFF5555;
                    isLoading = false;
                });
            }
        }).start();
    }
    
    private void saveCurrentSkin(String skinInfo) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("current_skin", skinInfo);
            json.addProperty("last_updated", System.currentTimeMillis());
            try (FileWriter writer = new FileWriter(CURRENT_SKIN_FILE)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save skin info", e);
        }
    }
    
    private void resetSkin() {
        statusMessage = "§a✓ Reset to default Steve skin";
        statusColor = 0x55FF55;
        currentSkinPreview = "Steve";
        saveCurrentSkin("Steve");
        
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(
                Text.literal("§a✓ Skin reset to default!"), true
            );
        }
    }
    
    private void openConfigFolder() {
        try {
            java.awt.Desktop.getDesktop().open(CONFIG_DIR);
            statusMessage = "§aConfig folder opened!";
            statusColor = 0x55FF55;
        } catch (Exception e) {
            statusMessage = "§cPath: " + CONFIG_DIR.getAbsolutePath();
            statusColor = 0xFF5555;
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Solid background - no opacity issue
        context.fill(0, 0, this.width, this.height, COLOR_BG);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Animated border glow
        long time = System.currentTimeMillis();
        float glow = (float) (Math.sin(time / 500.0) * 0.3 + 0.7);
        int borderColor = mixColor(COLOR_BORDER, 0xFFFFFFFF, glow);
        
        // Main Panel - Full opacity
        context.fill(centerX - 230, centerY - 130, centerX + 230, centerY + 210, COLOR_PANEL);
        drawBorder(context, centerX - 230, centerY - 130, 460, 340, borderColor);
        
        // Title
        drawShinyText(context, "§6§lSKIN STUDIO", centerX, centerY - 105, 1.5f);
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("§7Change your Minecraft skin"), centerX, centerY - 85, 0xAAAAAA);
        context.fill(centerX - 200, centerY - 70, centerX + 200, centerY - 69, COLOR_BORDER);
        
        // ===== LEFT SIDE: STATUE SKIN PREVIEW =====
        context.drawTextWithShadow(this.textRenderer, 
            Text.literal("§6◆ SKIN PREVIEW"), centerX - 210, centerY - 55, COLOR_BORDER);
        
        // Preview background - Solid
        context.fill(centerX - 210, centerY - 45, centerX - 30, centerY + 115, COLOR_PREVIEW_BG);
        drawBorder(context, centerX - 210, centerY - 45, 180, 160, 0xFF888888);
        
        // Draw Minecraft Style Statue
        drawMinecraftStatue(context, centerX - 120, centerY + 10, 70);
        
        // Statue rotation animation
        statueRotation += 1.5f;
        
        // Current skin name display
        String displayName = currentSkinPreview.length() > 18 ? 
            currentSkinPreview.substring(0, 15) + "..." : currentSkinPreview;
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("§e" + displayName), centerX - 120, centerY + 85, 0xFFFFAA);
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("§7Current Skin"), centerX - 120, centerY + 98, 0x888888);
        
        // ===== RIGHT SIDE: CONTROLS =====
        context.drawTextWithShadow(this.textRenderer, 
            Text.literal("§6◆ SELECT SOURCE"), centerX + 30, centerY - 55, COLOR_BORDER);
        
        context.drawTextWithShadow(this.textRenderer, 
            Text.literal(activeTab == 0 ? "§7▸ Enter Skin URL:" : "§7▸ Enter Username:"), 
            centerX + 20, centerY + 38, 0xCCCCCC);
        
        // Divider line
        context.fill(centerX - 210, centerY + 125, centerX + 210, centerY + 126, 0xFF555555);
        
        // Status Bar - Solid
        context.fill(centerX - 210, centerY + 170, centerX + 210, centerY + 195, 0xFF111111);
        drawBorder(context, centerX - 210, centerY + 170, 420, 25, 0xFF444444);
        
        // Shine effect on status bar
        drawShineEffect(context, centerX, centerY + 182);
        
        // Status Message
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal(statusMessage), centerX, centerY + 186, statusColor);
        
        // Loading Animation
        if (isLoading) {
            drawLoadingAnimation(context, centerX + 170, centerY + 65);
        }
        
        // Hover Effects for buttons
        renderHoverEffects(context, mouseX, mouseY, centerX, centerY);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void drawMinecraftStatue(DrawContext context, int x, int y, int size) {
        int headSize = size;
        int bodySize = (int)(size * 1.2);
        int legSize = (int)(size * 0.6);
        
        // Head
        context.fill(x - headSize/2, y - headSize, x + headSize/2, y, 0xFF8B6946);
        drawBorder(context, x - headSize/2, y - headSize, headSize, headSize, 0xFFD4AF37);
        
        // Face - Eyes
        context.fill(x - headSize/4, y - headSize + headSize/3, x - headSize/8, y - headSize + headSize/2, 0xFFFFFFFF);
        context.fill(x + headSize/8, y - headSize + headSize/3, x + headSize/4, y - headSize + headSize/2, 0xFFFFFFFF);
        
        // Mouth
        context.fill(x - headSize/4, y - headSize + 2*headSize/3, x + headSize/4, y - headSize + 2*headSize/3 + 2, 0xFFFFFFFF);
        
        // Hat
        context.fill(x - headSize/2 - 2, y - headSize - 6, x + headSize/2 + 2, y - headSize, 0xFF4A2A1A);
        context.fill(x - headSize/3, y - headSize - 10, x + headSize/3, y - headSize - 6, 0xFF4A2A1A);
        
        // Body
        context.fill(x - bodySize/2, y, x + bodySize/2, y + bodySize, 0xFF5A3A2A);
        drawBorder(context, x - bodySize/2, y, bodySize, bodySize, 0xFFD4AF37);
        
        // Chest plate
        context.fill(x - bodySize/3, y + bodySize/4, x + bodySize/3, y + bodySize/2, 0xFF3A2A1A);
        
        // Left Leg
        context.fill(x - bodySize/2, y + bodySize, x - bodySize/6, y + bodySize + legSize, 0xFF4A3A2A);
        drawBorder(context, x - bodySize/2, y + bodySize, bodySize/3, legSize, 0xFFD4AF37);
        
        // Right Leg
        context.fill(x + bodySize/6, y + bodySize, x + bodySize/2, y + bodySize + legSize, 0xFF4A3A2A);
        drawBorder(context, x + bodySize/6, y + bodySize, bodySize/3, legSize, 0xFFD4AF37);
        
        // Left Arm
        context.fill(x - bodySize/2 - legSize/2, y + legSize/2, x - bodySize/2, y + bodySize - legSize/2, 0xFF5A3A2A);
        drawBorder(context, x - bodySize/2 - legSize/2, y + legSize/2, legSize/2, bodySize - legSize, 0xFFD4AF37);
        
        // Right Arm
        context.fill(x + bodySize/2, y + legSize/2, x + bodySize/2 + legSize/2, y + bodySize - legSize/2, 0xFF5A3A2A);
        drawBorder(context, x + bodySize/2, y + legSize/2, legSize/2, bodySize - legSize, 0xFFD4AF37);
        
        // Rotation indicator
        context.fill(x - 5, y - headSize - 15, x + 5, y - headSize - 10, 0xFFD4AF37);
        context.fill(x - 15, y - headSize - 13, x - 5, y - headSize - 12, 0xFFD4AF37);
        context.fill(x + 5, y - headSize - 13, x + 15, y - headSize - 12, 0xFFD4AF37);
    }
    
    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
        
        // Corner accents
        context.fill(x, y, x + 12, y + 1, 0xFFFFFFFF);
        context.fill(x + width - 12, y, x + width, y + 1, 0xFFFFFFFF);
        context.fill(x, y + height - 1, x + 12, y + height, 0xFFFFFFFF);
        context.fill(x + width - 12, y + height - 1, x + width, y + height, 0xFFFFFFFF);
    }
    
    private void drawShinyText(DrawContext context, String text, int x, int y, float scale) {
        var matrices = context.getMatrices();
        matrices.push();
        matrices.scale(scale, scale, 1.0f);
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal(text), (int)(x / scale), (int)(y / scale), COLOR_BORDER);
        matrices.pop();
    }
    
    private void drawShineEffect(DrawContext context, int centerX, int y) {
        long time = System.currentTimeMillis();
        int shineX = centerX - 200 + (int)((time % 2000) / 2000.0 * 400);
        context.fill(shineX, y - 11, shineX + 60, y + 11, 0x22FFFFFF);
    }
    
    private void drawLoadingAnimation(DrawContext context, int x, int y) {
        long time = System.currentTimeMillis();
        int dot = (int)((time / 300) % 4);
        String dots = ".".repeat(dot) + " ".repeat(3 - dot);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§eLoading" + dots), x, y, 0xFFFF55);
    }
    
    private void renderHoverEffects(DrawContext context, int mouseX, int mouseY, int centerX, int centerY) {
        // Tab buttons
        if (mouseX >= centerX + 20 && mouseX <= centerX + 115 && mouseY >= centerY + 18 && mouseY <= centerY + 42) {
            context.fill(centerX + 20, centerY + 18, centerX + 115, centerY + 42, 0x33FFD700);
        }
        if (mouseX >= centerX + 125 && mouseX <= centerX + 220 && mouseY >= centerY + 18 && mouseY <= centerY + 42) {
            context.fill(centerX + 125, centerY + 18, centerX + 220, centerY + 42, 0x33FFD700);
        }
        // Apply button
        if (mouseX >= centerX + 20 && mouseX <= centerX + 115 && mouseY >= centerY + 90 && mouseY <= centerY + 122) {
            context.fill(centerX + 20, centerY + 90, centerX + 115, centerY + 122, 0x3344AA44);
        }
        // Reset button
        if (mouseX >= centerX + 125 && mouseX <= centerX + 220 && mouseY >= centerY + 90 && mouseY <= centerY + 122) {
            context.fill(centerX + 125, centerY + 90, centerX + 220, centerY + 122, 0x33FF5555);
        }
        // Close button
        if (mouseX >= centerX + 70 && mouseX <= centerX + 165 && mouseY >= centerY + 135 && mouseY <= centerY + 159) {
            context.fill(centerX + 70, centerY + 135, centerX + 165, centerY + 159, 0x33FFFFFF);
        }
    }
    
    private int mixColor(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int r = (int)(r1 * (1 - ratio) + r2 * ratio);
        int g = (int)(g1 * (1 - ratio) + g2 * ratio);
        int b = (int)(b1 * (1 - ratio) + b2 * ratio);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
    
    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
            }
