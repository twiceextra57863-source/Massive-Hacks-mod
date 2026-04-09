package com.yourname.skinchanger.screen;

import com.yourname.skinchanger.SkinChangerClient;
import com.yourname.skinchanger.SkinChangerMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SkinSelectScreen extends Screen {
    private static final Identifier BACKGROUND = Identifier.ofVanilla("textures/gui/options_background.png");
    private final Screen parent;
    private List<String> availableSkins = new ArrayList<>();
    private int selectedIndex = -1;
    private static final Path SKINS_DIR = Paths.get("config", "skinchanger", "skins");

    public SkinSelectScreen(Screen parent) {
        super(Text.literal("Select Your Skin"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        loadAvailableSkins();
        
        // Back button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> {
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 30, 200, 20).build());

        // Skin selection buttons
        int y = 50;
        for (int i = 0; i < availableSkins.size(); i++) {
            String skinName = availableSkins.get(i);
            final int index = i;
            this.addDrawableChild(ButtonWidget.builder(Text.literal(skinName), button -> {
                selectedIndex = index;
                applySelectedSkin();
            }).dimensions(this.width / 2 - 100, y, 200, 20).build());
            y += 25;
        }

        // Refresh button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Refresh List"), button -> {
            refreshSkins();
        }).dimensions(this.width / 2 - 50, y + 10, 100, 20).build());
    }

    private void loadAvailableSkins() {
        availableSkins.clear();
        File skinsDir = SKINS_DIR.toFile();
        if (skinsDir.exists() && skinsDir.isDirectory()) {
            File[] files = skinsDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".png"));
            if (files != null) {
                for (File file : files) {
                    availableSkins.add(file.getName());
                }
            }
        }
    }

    private void applySelectedSkin() {
        if (selectedIndex >= 0 && selectedIndex < availableSkins.size()) {
            String skinName = availableSkins.get(selectedIndex);
            SkinChangerClient.loadSkin(this.client, skinName);
        }
    }

    private void refreshSkins() {
        loadAvailableSkins();
        this.clearChildren();
        this.init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, 
            this.width / 2, 20, 0xFFFFFF);
        
        // Instructions
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("Place your skin PNG files in: config/skinchanger/skins/"), 
            this.width / 2, 35, 0xAAAAAA);
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
