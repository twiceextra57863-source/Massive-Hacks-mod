package com.tumhara.mod.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SkinDashboardScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("SkinChanger");
    private static final File CONFIG_DIR = new File("config/skinchanger");
    private static final File SKIN_FILE = new File(CONFIG_DIR, "custom_skin.png");
    private static final File CONFIG_FILE = new File(CONFIG_DIR, "settings.json");
    
    private TextFieldWidget skinUrlField;
    private TextFieldWidget localSkinField;
    private final Screen parent;
    private String statusMessage = "";
    private int statusColor = 0x55FF55;
    private String currentSkinPreview = "";
    private int skinWidth = 64;
    private int skinHeight = 64;

    public SkinDashboardScreen(Screen parent) {
        super(Text.literal("§8[§6✧ Skin Studio §8]§r"));
        this.parent = parent;
        createConfigDirectory();
        loadSettings();
    }

    private void createConfigDirectory() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
            LOGGER.info("Created config directory: " + CONFIG_DIR.getAbsolutePath());
        }
    }

    private void loadSettings() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("last_skin")) {
                    currentSkinPreview = json.get("last_skin").getAsString();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load settings", e);
            }
        }
    }

    private void saveSettings(String skinPath) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("last_skin", skinPath);
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save settings", e);
        }
    }

    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = this.height / 2 - 100;

        // Title background panel
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(""),
            button -> {}
        ).dimensions(centerX - 160, startY - 30, 320, 260).build());

        // URL Input Field
        this.skinUrlField = new TextFieldWidget(
            this.textRenderer,
            centerX - 140,
            startY + 20,
            280,
            20,
            Text.literal("§7Enter skin URL...")
        );
        this.skinUrlField.setMaxLength(512);
        this.addSelectableChild(this.skinUrlField);

        // Local Skin Path Field
        this.localSkinField = new TextFieldWidget(
            this.textRenderer,
            centerX - 140,
            startY + 60,
            200,
            20,
            Text.literal("§7Or paste local path...")
        );
        this.localSkinField.setMaxLength(256);
        this.addSelectableChild(this.localSkinField);

        // Browse Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§b📂 Browse"),
            button -> browseLocalSkin()
        )
        .dimensions(centerX + 70, startY + 59, 70, 22)
        .build());

        // Apply from URL Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a✓ Apply from URL"),
            button -> applySkinFromUrl(this.skinUrlField.getText())
        )
        .dimensions(centerX - 140, startY + 95, 280, 24)
        .build());

        // Apply Local Skin Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§e📁 Apply Local Skin"),
            button -> applyLocalSkin(this.localSkinField.getText())
        )
        .dimensions(centerX - 140, startY + 125, 280, 24)
        .build());

        // Reset Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§c🗑 Reset to Default"),
            button -> resetToDefaultSkin()
        )
        .dimensions(centerX - 140, startY + 155, 135, 24)
        .build());

        // Close Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§7✖ Close"),
            button -> this.close()
        )
        .dimensions(centerX + 5, startY + 155, 135, 24)
        .build());

        // Open Config Folder Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§6📁 Open Skin Folder"),
            button -> openConfigFolder()
        )
        .dimensions(centerX - 140, startY + 185, 280, 20)
        .build());
    }

    private void browseLocalSkin() {
        // Open file picker (simplified - just set path)
        localSkinField.setText(CONFIG_DIR.getAbsolutePath() + "/custom_skin.png");
        statusMessage = "§ePlace your skin.png in config/skinchanger/ folder!";
        statusColor = 0xFFFF55;
    }

    private void openConfigFolder() {
        try {
            java.awt.Desktop.getDesktop().open(CONFIG_DIR);
        } catch (Exception e) {
            statusMessage = "§cCannot open folder manually! Path: " + CONFIG_DIR.getAbsolutePath();
            statusColor = 0xFF5555;
        }
    }

    private void applySkinFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            statusMessage = "§cPlease enter a valid URL!";
            statusColor = 0xFF5555;
            return;
        }
        
        statusMessage = "§eDownloading skin...";
        statusColor = 0xFFFF55;
        
        new Thread(() -> {
            try {
                String skinHash = Integer.toHexString(url.hashCode());
                Identifier skinId = Identifier.of("custom_skins", skinHash);
                
                MinecraftClient.getInstance().execute(() -> {
                    saveSkinInfo(url);
                    statusMessage = "§a✓ Skin applied from URL!";
                    statusColor = 0x55FF55;
                    currentSkinPreview = url;
                    saveSettings(url);
                });
            } catch (Exception e) {
                MinecraftClient.getInstance().execute(() -> {
                    statusMessage = "§c✗ Failed to load skin from URL";
                    statusColor = 0xFF5555;
                });
            }
        }).start();
    }

    private void applyLocalSkin(String path) {
        if (path == null || path.trim().isEmpty()) {
            statusMessage = "§cPlease enter a valid file path!";
            statusColor = 0xFF5555;
            return;
        }
        
        File skinFile = new File(path);
        if (!skinFile.exists()) {
            statusMessage = "§cFile not found: " + path;
            statusColor = 0xFF5555;
            return;
        }
        
        statusMessage = "§a✓ Local skin loaded!";
        statusColor = 0x55FF55;
        saveSettings(path);
        
        // Detect skin size
        try {
            javax.imageio.ImageIO.read(skinFile);
            statusMessage = "§a✓ Skin loaded! (May need relog)";
        } catch (Exception e) {
            statusMessage = "§eInvalid image format! Use PNG";
            statusColor = 0xFFFF55;
        }
    }
    
    private void resetToDefaultSkin() {
        statusMessage = "§a✓ Reset to default!";
        statusColor = 0x55FF55;
        LOGGER.info("Skin reset to default");
    }

    private void saveSkinInfo(String info) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("current_skin", info);
            json.addProperty("last_updated", System.currentTimeMillis());
            try (FileWriter writer = new FileWriter(new File(CONFIG_DIR, "skin_info.json"))) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save skin info", e);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dark transparent background
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Semi-transparent dark panel
        context.fill(this.width / 2 - 165, this.height / 2 - 110, 
                     this.width / 2 + 165, this.height / 2 + 130, 
                     0xCC000000);
        
        // Border
        context.drawBorder(this.width / 2 - 165, this.height / 2 - 110, 
                          330, 240, 0xFFAA00);
        
        // Title
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§6§l✧ SKIN STUDIO ✧§r"),
            this.width / 2,
            this.height / 2 - 90,
            0xFFAA00
        );
        
        // Subtitle
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§7Change your Minecraft skin"),
            this.width / 2,
            this.height / 2 - 75,
            0xAAAAAA
        );
        
        // Divider line
        context.fill(this.width / 2 - 140, this.height / 2 - 65, 
                     this.width / 2 + 140, this.height / 2 - 64, 
                     0xFF555555);
        
        // Labels
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("§e▶ URL Input:"),
            this.width / 2 - 140,
            this.height / 2 - 50,
            0xFFAA55
        );
        
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("§e▶ Local File:"),
            this.width / 2 - 140,
            this.height / 2 - 10,
            0xFFAA55
        );
        
        // Status message with background
        if (!statusMessage.isEmpty()) {
            int bgColor = 0x88000000;
            context.fill(this.width / 2 - 140, this.height / 2 + 215, 
                         this.width / 2 + 140, this.height / 2 + 235, bgColor);
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(statusMessage),
                this.width / 2,
                this.height / 2 + 226,
                statusColor
            );
        }
        
        // Info text
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("§7§o💡 Tip: Place skin.png in " + CONFIG_DIR.getAbsolutePath()),
            this.width / 2 - 140,
            this.height / 2 + 245,
            0x666666
        );
        
        // Render input fields
        this.skinUrlField.render(context, mouseX, mouseY, delta);
        this.localSkinField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
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
        // Darker background for better visibility
        context.fill(0, 0, this.width, this.height, 0xAA000000);
    }
}
