package com.tumhara.mod.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileWriter;
import com.google.gson.JsonObject;
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
    private float rotationAngle = 0;
    private PlayerEntity previewPlayer;
    private int activeTab = 0;
    
    static {
        ModelLoader.loadModelConfig();
    }
    
    public SkinDashboardScreen(Screen parent) {
        super(Text.literal(""));
        this.parent = parent;
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        createPreviewPlayer();
    }
    
    private void createPreviewPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.world != null) {
            // Create a preview player with default skin
            previewPlayer = new AbstractClientPlayerEntity(client.world, 
                client.getSession().getProfile()) {
                @Override
                public boolean isPartVisible() { return true; }
            };
        }
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        this.urlField = new TextFieldWidget(
            this.textRenderer,
            centerX + 10,
            centerY + 60,
            220,
            22,
            Text.literal("https://example.com/skin.png")
        );
        this.urlField.setMaxLength(512);
        this.urlField.setVisible(true);
        this.addDrawableChild(this.urlField);
        
        this.usernameField = new TextFieldWidget(
            this.textRenderer,
            centerX + 10,
            centerY + 60,
            220,
            22,
            Text.literal("Steve")
        );
        this.usernameField.setMaxLength(64);
        this.usernameField.setVisible(false);
        this.addDrawableChild(this.usernameField);
        
        // Tabs
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(activeTab == 0 ? "§6§l[ URL ]" : "§7[ URL ]"),
            button -> {
                activeTab = 0;
                urlField.setVisible(true);
                usernameField.setVisible(false);
                refreshButtons();
            }
        ).dimensions(centerX + 10, centerY + 25, 105, 24).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(activeTab == 1 ? "§6§l[ USERNAME ]" : "§7[ USERNAME ]"),
            button -> {
                activeTab = 1;
                urlField.setVisible(false);
                usernameField.setVisible(true);
                refreshButtons();
            }
        ).dimensions(centerX + 125, centerY + 25, 105, 24).build());
        
        // Action Buttons
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a✓ APPLY"),
            button -> applySkin()
        ).dimensions(centerX + 10, centerY + 100, 100, 30).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§c🗑 RESET"),
            button -> resetSkin()
        ).dimensions(centerX + 130, centerY + 100, 100, 30).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§7✖ CLOSE"),
            button -> close()
        ).dimensions(centerX + 60, centerY + 145, 100, 24).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§b📁 OPEN CONFIG"),
            button -> openConfigFolder()
        ).dimensions(centerX + 40, centerY + 180, 140, 20).build());
    }
    
    private void refreshButtons() {
        this.clearChildren();
        this.init();
    }
    
    private void applySkin() {
        isLoading = true;
        statusMessage = "§eApplying skin...";
        statusColor = 0xFFFF55;
        
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                MinecraftClient.getInstance().execute(() -> {
                    saveCurrentSkin(activeTab == 0 ? urlField.getText() : usernameField.getText());
                    statusMessage = "§a✓ Skin applied! Restart to see changes";
                    statusColor = 0x55FF55;
                    isLoading = false;
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
        statusMessage = "§a✓ Reset to default skin";
        statusColor = 0x55FF55;
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
        // Background
        context.fill(0, 0, this.width, this.height, ModelLoader.getBackgroundColor());
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Animated border
        long time = System.currentTimeMillis();
        float glow = (float) (Math.sin(time / 500.0) * 0.3 + 0.7);
        int borderColor = mixColor(ModelLoader.getBorderColor(), 0xFFFFFFFF, glow);
        
        // Main Panel - Left side for model, Right side for controls
        context.fill(centerX - 200, centerY - 120, centerX + 200, centerY + 220, 0xEE2D2D2D);
        drawBorder(context, centerX - 200, centerY - 120, 400, 340, borderColor);
        
        // Title
        drawShinyText(context, "§6§l" + ModelLoader.getTitleText(), centerX, centerY - 95, 1.5f);
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("§7Change your Minecraft skin"), centerX, centerY - 75, 0xAAAAAA);
        context.fill(centerX - 170, centerY - 60, centerX + 170, centerY - 59, ModelLoader.getBorderColor());
        
        // ===== LEFT SIDE: 3D PLAYER MODEL =====
        context.drawTextWithShadow(this.textRenderer, 
            Text.literal("§6◆ SKIN PREVIEW"), centerX - 180, centerY - 45, ModelLoader.getBorderColor());
        
        // Model background
        context.fill(centerX - 180, centerY - 35, centerX - 20, centerY + 105, 0xFF333333);
        drawBorder(context, centerX - 180, centerY - 35, 160, 140, 0xFF888888);
        
        // Render 3D Player Model
        if (previewPlayer != null) {
            ModelLoader.renderPlayerModel(context.getMatrices(), centerX - 100, centerY + 10, 60, previewPlayer, rotationAngle);
            rotationAngle += 2;
        }
        
        // Model label
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("§73D Model Preview"), centerX - 100, centerY + 80, 0x888888);
        
        // ===== RIGHT SIDE: CONTROLS =====
        context.drawTextWithShadow(this.textRenderer, 
            Text.literal("§6◆ SELECT SOURCE"), centerX + 20, centerY - 45, ModelLoader.getBorderColor());
        
        context.drawTextWithShadow(this.textRenderer, 
            Text.literal(activeTab == 0 ? "§7▸ Skin URL:" : "§7▸ Username:"), 
            centerX + 10, centerY + 48, 0xCCCCCC);
        
        // Status Bar
        context.fill(centerX - 180, centerY + 180, centerX + 180, centerY + 200, 0x88000000);
        drawShineEffect(context, centerX, centerY + 190);
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal(statusMessage), centerX, centerY + 190, statusColor);
        
        // Loading Animation
        if (isLoading) {
            drawLoadingAnimation(context, centerX + 160, centerY + 75);
        }
        
        // Hover Effects
        renderHoverEffects(context, mouseX, mouseY, centerX, centerY);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 2, color);
        context.fill(x, y + height - 2, x + width, y + height, color);
        context.fill(x, y, x + 2, y + height, color);
        context.fill(x + width - 2, y, x + width, y + height, color);
        
        // Corner accents
        context.fill(x, y, x + 15, y + 1, 0xFFFFFFFF);
        context.fill(x + width - 15, y, x + width, y + 1, 0xFFFFFFFF);
        context.fill(x, y + height - 1, x + 15, y + height, 0xFFFFFFFF);
        context.fill(x + width - 15, y + height - 1, x + width, y + height, 0xFFFFFFFF);
    }
    
    private void drawShinyText(DrawContext context, String text, int x, int y, float scale) {
        var matrices = context.getMatrices();
        matrices.push();
        matrices.scale(scale, scale, 1.0f);
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal(text), (int)(x / scale), (int)(y / scale), ModelLoader.getBorderColor());
        matrices.pop();
    }
    
    private void drawShineEffect(DrawContext context, int centerX, int y) {
        long time = System.currentTimeMillis();
        int shineX = centerX - 170 + (int)((time % 2000) / 2000.0 * 340);
        context.fill(shineX, y - 9, shineX + 50, y + 9, 0x22FFFFFF);
    }
    
    private void drawLoadingAnimation(DrawContext context, int x, int y) {
        long time = System.currentTimeMillis();
        int dot = (int)((time / 300) % 4);
        String dots = ".".repeat(dot) + " ".repeat(3 - dot);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§eLoading" + dots), x, y, 0xFFFF55);
    }
    
    private void renderHoverEffects(DrawContext context, int mouseX, int mouseY, int centerX, int centerY) {
        int[][] areas = {
            {centerX + 10, centerY + 25, 105, 24},
            {centerX + 125, centerY + 25, 105, 24},
            {centerX + 10, centerY + 100, 100, 30},
            {centerX + 130, centerY + 100, 100, 30}
        };
        
        for (int[] area : areas) {
            if (mouseX >= area[0] && mouseX <= area[0] + area[2] && 
                mouseY >= area[1] && mouseY <= area[1] + area[3]) {
                context.fill(area[0], area[1], area[0] + area[2], area[1] + area[3], 0x33FFD700);
            }
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
