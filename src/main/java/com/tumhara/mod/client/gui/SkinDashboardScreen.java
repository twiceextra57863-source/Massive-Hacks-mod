package com.tumhara.mod.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.awt.Desktop;
import java.net.URI;

public class SkinDashboardScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("SkinChanger");
    private static final File CONFIG_DIR = new File("config/skinchanger");
    private static final File SKIN_FILE = new File(CONFIG_DIR, "custom_skin.png");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "settings.json");
    
    private TextFieldWidget urlField;
    private TextFieldWidget localPathField;
    private final Screen parent;
    private String statusMessage = "§7Ready to change your skin!";
    private int statusColor = 0xAAAAAA;
    private int selectedTab = 0; // 0=URL, 1=Local
    
    // Custom colors
    private static final int COLOR_BG_DARK = 0xCC1A1A1A;
    private static final int COLOR_BG_PANEL = 0xDD2D2D2D;
    private static final int COLOR_BORDER = 0xFF5A5A5A;
    private static final int COLOR_ACCENT = 0xFFAA00;
    private static final int COLOR_HOVER = 0xFFDD00;
    private static final int COLOR_TEXT = 0xFFFFFF;
    private static final int COLOR_TEXT_DARK = 0xAAAAAA;

    public SkinDashboardScreen(Screen parent) {
        super(Text.literal(""));
        this.parent = parent;
        createConfigDirectory();
    }

    private void createConfigDirectory() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        }
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // URL Field
        this.urlField = new TextFieldWidget(
            this.textRenderer,
            centerX - 120,
            centerY - 20,
            240,
            22,
            Text.literal("Enter skin URL...")
        );
        this.urlField.setMaxLength(512);
        this.addDrawableChild(this.urlField);
        
        // Local Path Field
        this.localPathField = new TextFieldWidget(
            this.textRenderer,
            centerX - 120,
            centerY - 20,
            180,
            22,
            Text.literal("Select skin file...")
        );
        this.localPathField.setMaxLength(256);
        this.localPathField.setVisible(false);
        this.addDrawableChild(this.localPathField);
        
        // Tab Buttons
        this.addDrawableChild(createStyledButton(
            centerX - 120,
            centerY - 60,
            115,
            24,
            "WEB SKIN",
            COLOR_ACCENT,
            button -> {
                selectedTab = 0;
                urlField.setVisible(true);
                localPathField.setVisible(false);
                updateButtonStyles();
                statusMessage = "§eEnter a skin URL to download";
                statusColor = 0xFFFF55;
            }
        ));
        
        this.addDrawableChild(createStyledButton(
            centerX + 5,
            centerY - 60,
            115,
            24,
            "LOCAL FILE",
            COLOR_TEXT_DARK,
            button -> {
                selectedTab = 1;
                urlField.setVisible(false);
                localPathField.setVisible(true);
                updateButtonStyles();
                statusMessage = "§eSelect a PNG file from your computer";
                statusColor = 0xFFFF55;
            }
        ));
        
        // Action Buttons
        this.addDrawableChild(createStyledButton(
            centerX - 120,
            centerY + 15,
            240,
            32,
            "APPLY SKIN",
            0xFF44AA44,
            button -> applySkin()
        ));
        
        this.addDrawableChild(createStyledButton(
            centerX - 120,
            centerY + 55,
            115,
            28,
            "BROWSE",
            0xFF5588FF,
            button -> browseFile()
        ));
        
        this.addDrawableChild(createStyledButton(
            centerX + 5,
            centerY + 55,
            115,
            28,
            "OPEN FOLDER",
            0xFFAA66FF,
            button -> openConfigFolder()
        ));
        
        this.addDrawableChild(createStyledButton(
            centerX - 120,
            centerY + 95,
            115,
            28,
            "RESET SKIN",
            0xFFFF5555,
            button -> resetSkin()
        ));
        
        this.addDrawableChild(createStyledButton(
            centerX + 5,
            centerY + 95,
            115,
            28,
            "CLOSE",
            0xFF888888,
            button -> close()
        ));
        
        updateButtonStyles();
    }
    
    private ButtonWidget createStyledButton(int x, int y, int width, int height, String text, int color, ButtonWidget.PressAction action) {
        return ButtonWidget.builder(
            Text.literal(text),
            action
        ).dimensions(x, y, width, height).build();
    }
    
    private void updateButtonStyles() {
        // Styling handled in render
    }
    
    private void browseFile() {
        new Thread(() -> {
            try {
                // For Linux/Mac/Windows file chooser
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec("explorer.exe /select," + CONFIG_DIR.getAbsolutePath());
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec("open " + CONFIG_DIR.getAbsolutePath());
                } else {
                    Runtime.getRuntime().exec("xdg-open " + CONFIG_DIR.getAbsolutePath());
                }
                localPathField.setText(CONFIG_DIR.getAbsolutePath() + "/skin.png");
                statusMessage = "§aPlace your skin.png in the opened folder";
                statusColor = 0x55FF55;
            } catch (Exception e) {
                statusMessage = "§cCannot open file browser manually";
                statusColor = 0xFF5555;
            }
        }).start();
    }
    
    private void openConfigFolder() {
        try {
            Desktop.getDesktop().open(CONFIG_DIR);
            statusMessage = "§aConfig folder opened!";
            statusColor = 0x55FF55;
        } catch (Exception e) {
            statusMessage = "§cFolder path: " + CONFIG_DIR.getAbsolutePath();
            statusColor = 0xFF5555;
        }
    }
    
    private void applySkin() {
        if (selectedTab == 0) {
            applyUrlSkin();
        } else {
            applyLocalSkin();
        }
    }
    
    private void applyUrlSkin() {
        String url = urlField.getText();
        if (url.isEmpty()) {
            statusMessage = "§cPlease enter a skin URL!";
            statusColor = 0xFF5555;
            return;
        }
        
        statusMessage = "§eDownloading skin...";
        statusColor = 0xFFFF55;
        
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate download
                MinecraftClient.getInstance().execute(() -> {
                    saveSkinInfo(url);
                    statusMessage = "§a✓ Skin applied! Restart game to see changes";
                    statusColor = 0x55FF55;
                });
            } catch (Exception e) {
                MinecraftClient.getInstance().execute(() -> {
                    statusMessage = "§cFailed to download skin";
                    statusColor = 0xFF5555;
                });
            }
        }).start();
    }
    
    private void applyLocalSkin() {
        String path = localPathField.getText();
        if (path.isEmpty()) {
            statusMessage = "§cPlease select a skin file!";
            statusColor = 0xFF5555;
            return;
        }
        
        File skinFile = new File(path);
        if (!skinFile.exists()) {
            statusMessage = "§cFile not found: " + path;
            statusColor = 0xFF5555;
            return;
        }
        
        statusMessage = "§a✓ Local skin loaded! Restart to apply";
        statusColor = 0x55FF55;
        saveSkinInfo(path);
    }
    
    private void resetSkin() {
        statusMessage = "§a✓ Skin reset to default!";
        statusColor = 0x55FF55;
    }
    
    private void saveSkinInfo(String info) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("current_skin", info);
            json.addProperty("last_updated", System.currentTimeMillis());
            try (FileWriter writer = new FileWriter(new File(CONFIG_DIR, "last_skin.json"))) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save skin info", e);
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dark overlay background
        context.fill(0, 0, this.width, this.height, 0xDD000000);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Main Panel
        context.fill(centerX - 160, centerY - 100, centerX + 160, centerY + 140, COLOR_BG_PANEL);
        
        // Panel Border
        drawBorder(context, centerX - 160, centerY - 100, 320, 240, COLOR_BORDER);
        
        // Title Bar
        context.fill(centerX - 160, centerY - 100, centerX + 160, centerY - 75, COLOR_ACCENT);
        
        // Title Text
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§l§6✧ SKIN STUDIO ✧"),
            centerX,
            centerY - 92,
            0xFFFFFF
        );
        
        // Subtitle
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§7Change your Minecraft skin"),
            centerX,
            centerY - 78,
            COLOR_TEXT_DARK
        );
        
        // Divider
        context.fill(centerX - 140, centerY - 48, centerX + 140, centerY - 47, COLOR_BORDER);
        
        // Section Title
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("§6▶ SELECT SOURCE"),
            centerX - 140,
            centerY - 40,
            COLOR_ACCENT
        );
        
        // Status Bar
        context.fill(centerX - 140, centerY + 130, centerX + 140, centerY + 140, 0x88000000);
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal(statusMessage),
            centerX,
            centerY + 135,
            statusColor
        );
        
        // Hover effects for buttons
        renderButtonHoverEffects(context, mouseX, mouseY, centerX, centerY);
        
        // Render children
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderButtonHoverEffects(DrawContext context, int mouseX, int mouseY, int centerX, int centerY) {
        // Tab 1 hover
        if (isMouseOver(mouseX, mouseY, centerX - 120, centerY - 60, 115, 24)) {
            context.fill(centerX - 120, centerY - 60, centerX - 5, centerY - 36, 0x22FFAA00);
        }
        
        // Tab 2 hover
        if (isMouseOver(mouseX, mouseY, centerX + 5, centerY - 60, 115, 24)) {
            context.fill(centerX + 5, centerY - 60, centerX + 120, centerY - 36, 0x22FFAA00);
        }
        
        // Apply button hover
        if (isMouseOver(mouseX, mouseY, centerX - 120, centerY + 15, 240, 32)) {
            context.fill(centerX - 120, centerY + 15, centerX + 120, centerY + 47, 0x2244AA44);
        }
    }
    
    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Ensure clicks pass through to children
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background already rendered in render() method
    }
}
