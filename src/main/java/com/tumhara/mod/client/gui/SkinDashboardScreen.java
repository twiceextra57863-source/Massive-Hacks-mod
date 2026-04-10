package com.tumhara.mod.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;

public class SkinDashboardScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("SkinChanger");
    private static final File CONFIG_DIR = new File("config/skinchanger");
    private static final File SKINS_DIR = new File(CONFIG_DIR, "skins");
    private static final File CURRENT_SKIN_FILE = new File(CONFIG_DIR, "current_skin.json");
    
    private List<SkinEntry> availableSkins = new ArrayList<>();
    private List<SkinEntry> filteredSkins = new ArrayList<>();
    private int scrollOffset = 0;
    private int selectedSkinIndex = -1;
    private String statusMessage = "§7Select a skin to apply";
    private int statusColor = 0xAAAAAA;
    private TextFieldWidget searchField;
    private String searchQuery = "";
    
    // Colors
    private static final int COLOR_BG = 0xDD1A1A1A;
    private static final int COLOR_PANEL = 0xEE2D2D2D;
    private static final int COLOR_BORDER = 0xFF6A6A6A;
    private static final int COLOR_ACCENT = 0xFFAA33;
    private static final int COLOR_SELECTED = 0x88FFAA33;
    private static final int COLOR_HOVER = 0x44FFFFFF;
    
    private final Screen parent;
    
    public SkinDashboardScreen(Screen parent) {
        super(Text.literal(""));
        this.parent = parent;
        initDirectories();
        scanForSkins();
    }
    
    private void initDirectories() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        if (!SKINS_DIR.exists()) SKINS_DIR.mkdirs();
        createReadme();
    }
    
    private void createReadme() {
        File readme = new File(SKINS_DIR, "README.txt");
        if (!readme.exists()) {
            try (FileWriter writer = new FileWriter(readme)) {
                writer.write("Place your Minecraft skin PNG files here!\n");
                writer.write("Supported formats: .png\n");
                writer.write("Skin size: 64x64 or 128x128 pixels\n");
            } catch (Exception e) {
                LOGGER.error("Failed to create readme", e);
            }
        }
    }
    
    private void scanForSkins() {
        availableSkins.clear();
        
        // Add default skin
        availableSkins.add(new SkinEntry("Default Steve", "default", 
            Identifier.of("minecraft", "textures/entity/player/wide/steve.png"), null));
        
        // Add default Alex skin
        availableSkins.add(new SkinEntry("Default Alex", "default_alex", 
            Identifier.of("minecraft", "textures/entity/player/wide/alex.png"), null));
        
        // Scan skins folder
        File[] skinFiles = SKINS_DIR.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (skinFiles != null) {
            for (File file : skinFiles) {
                String name = file.getName().replace(".png", "");
                Identifier textureId = Identifier.of("skinchanger", "skins/" + file.getName());
                availableSkins.add(new SkinEntry(name, file.getAbsolutePath(), textureId, file));
            }
        }
        
        updateFilteredSkins();
        loadCurrentSkin();
    }
    
    private void updateFilteredSkins() {
        filteredSkins.clear();
        if (searchQuery.isEmpty()) {
            filteredSkins.addAll(availableSkins);
        } else {
            for (SkinEntry skin : availableSkins) {
                if (skin.getName().toLowerCase().contains(searchQuery.toLowerCase())) {
                    filteredSkins.add(skin);
                }
            }
        }
        scrollOffset = 0;
    }
    
    private void loadCurrentSkin() {
        if (CURRENT_SKIN_FILE.exists()) {
            try (FileReader reader = new FileReader(CURRENT_SKIN_FILE)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String lastSkin = json.get("last_skin").getAsString();
                for (int i = 0; i < filteredSkins.size(); i++) {
                    if (filteredSkins.get(i).getName().equals(lastSkin)) {
                        selectedSkinIndex = i;
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load current skin", e);
            }
        }
    }
    
    private void saveCurrentSkin(String skinName) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("last_skin", skinName);
            try (FileWriter writer = new FileWriter(CURRENT_SKIN_FILE)) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save current skin", e);
        }
    }
    
    private void applySelectedSkin() {
        if (selectedSkinIndex < 0 || selectedSkinIndex >= filteredSkins.size()) {
            statusMessage = "§cPlease select a skin first!";
            statusColor = 0xFF5555;
            return;
        }
        
        SkinEntry skin = filteredSkins.get(selectedSkinIndex);
        statusMessage = "§a✓ Applied: " + skin.getName();
        statusColor = 0x55FF55;
        saveCurrentSkin(skin.getName());
        
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(
                Text.literal("§a✓ Skin changed to §6" + skin.getName() + "§a! Restart to see effect"), 
                true
            );
        }
    }
    
    private void openSkinsFolder() {
        try {
            java.awt.Desktop.getDesktop().open(SKINS_DIR);
            statusMessage = "§aSkins folder opened!";
            statusColor = 0x55FF55;
        } catch (Exception e) {
            statusMessage = "§cPath: " + SKINS_DIR.getAbsolutePath();
            statusColor = 0xFF5555;
        }
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        
        // Search Field
        this.searchField = new TextFieldWidget(
            this.textRenderer,
            centerX - 100,
            45,
            200,
            20,
            Text.literal("Search skins...")
        );
        this.searchField.setChangedListener(text -> {
            searchQuery = text;
            updateFilteredSkins();
        });
        this.addDrawableChild(this.searchField);
        
        // Apply Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a✓ APPLY SKIN"),
            button -> applySelectedSkin()
        )
        .dimensions(centerX - 100, this.height - 45, 95, 22)
        .build());
        
        // Refresh Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§6⟳ REFRESH"),
            button -> {
                scanForSkins();
                statusMessage = "§aScanned " + availableSkins.size() + " skins!";
                statusColor = 0x55FF55;
            }
        )
        .dimensions(centerX + 5, this.height - 45, 95, 22)
        .build());
        
        // Close Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§c✖ CLOSE"),
            button -> close()
        )
        .dimensions(this.width - 55, 10, 45, 20)
        .build());
        
        // Open Folder Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§b📁 OPEN FOLDER"),
            button -> openSkinsFolder()
        )
        .dimensions(10, 10, 100, 20)
        .build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        context.fill(0, 0, this.width, this.height, COLOR_BG);
        
        int centerX = this.width / 2;
        int startY = 80;
        int skinSize = 70;
        int skinsPerRow = Math.max(1, (this.width - 100) / (skinSize + 15));
        int rowHeight = skinSize + 35;
        
        // Title Bar
        context.fill(0, 0, this.width, 38, COLOR_ACCENT);
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§l§0✧ SKIN STUDIO ✧"),
            centerX,
            14,
            0xFFFFFF
        );
        
        // Search Label
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("§7Search:"),
            centerX - 115,
            49,
            0xAAAAAA
        );
        
        // Skin Grid
        int visibleRows = (this.height - 150) / rowHeight;
        int startIndex = scrollOffset * skinsPerRow;
        int endIndex = Math.min(startIndex + (visibleRows * skinsPerRow), filteredSkins.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            SkinEntry skin = filteredSkins.get(i);
            int row = (i - startIndex) / skinsPerRow;
            int col = (i - startIndex) % skinsPerRow;
            int x = 40 + col * (skinSize + 15);
            int y = startY + row * rowHeight;
            
            boolean isSelected = (i == selectedSkinIndex);
            boolean isHovered = mouseX >= x && mouseX <= x + skinSize && mouseY >= y && mouseY <= y + skinSize;
            
            // Card background
            int cardColor = isSelected ? COLOR_SELECTED : (isHovered ? COLOR_HOVER : COLOR_PANEL);
            context.fill(x - 3, y - 3, x + skinSize + 3, y + skinSize + 28, cardColor);
            
            // Card border
            drawBorder(context, x - 3, y - 3, skinSize + 6, skinSize + 31, COLOR_BORDER);
            
            // Skin preview area
            context.fill(x, y, x + skinSize, y + skinSize, 0xFF444444);
            drawBorder(context, x, y, skinSize, skinSize, 0xFF888888);
            
            // Skin preview placeholder
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("§8[SKIN]"),
                x + skinSize / 2,
                y + skinSize / 2 - 4,
                0x888888
            );
            
            // Skin name
            String displayName = skin.getName();
            if (displayName.length() > 12) displayName = displayName.substring(0, 10) + "...";
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(displayName),
                x + skinSize / 2,
                y + skinSize + 8,
                0xFFFFFF
            );
            
            // Click handling
            if (isHovered && mouseY < y + skinSize + 5) {
                if (mouseY > y && mouseY < y + skinSize) {
                    selectedSkinIndex = i;
                    statusMessage = "§eSelected: " + skin.getName();
                    statusColor = 0xFFFF55;
                }
            }
        }
        
        // Status Bar
        context.fill(0, this.height - 28, this.width, this.height, 0xCC000000);
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal(statusMessage),
            10,
            this.height - 22,
            statusColor
        );
        
        // Skin count
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("§7Skins: §e" + filteredSkins.size()),
            this.width - 100,
            this.height - 22,
            0xAAAAAA
        );
        
        // Instructions for empty folder
        if (availableSkins.size() <= 2) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("§7§o📁 Place .png skin files in:"),
                centerX,
                this.height - 70,
                0x666666
            );
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("§7§o" + SKINS_DIR.getAbsolutePath()),
                centerX,
                this.height - 58,
                0x666666
            );
        }
        
        // Render search field
        this.searchField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int skinSize = 70;
        int skinsPerRow = Math.max(1, (this.width - 100) / (skinSize + 15));
        int totalRows = (int) Math.ceil((double) filteredSkins.size() / skinsPerRow);
        int visibleRows = (this.height - 150) / (skinSize + 35);
        int maxScroll = Math.max(0, totalRows - visibleRows);
        
        scrollOffset -= (int) verticalAmount;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        return true;
    }
    
    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    // Inner class for skin entry
    private static class SkinEntry {
        private final String name;
        private final String path;
        private final Identifier textureId;
        private final File file;
        
        public SkinEntry(String name, String path, Identifier textureId, File file) {
            this.name = name;
            this.path = path;
            this.textureId = textureId;
            this.file = file;
        }
        
        public String getName() { return name; }
        public String getPath() { return path; }
        public Identifier getTextureId() { return textureId; }
        public File getFile() { return file; }
    }
}
