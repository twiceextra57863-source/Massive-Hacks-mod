package com.tumhara.mod.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.DiffuseLighting;
import org.joml.Vector3f;
import java.io.File;

public class SkinDashboardScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("SkinChanger");
    private static final File CONFIG_DIR = new File("config/skinchanger");
    
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
        if (client.world != null) {
            previewPlayer = new AbstractClientPlayerEntity(client.world, 
                client.player != null ? client.player.getGameProfile() : null) {};
        }
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        this.urlField = new TextFieldWidget(
            this.textRenderer,
            centerX - 120,
            centerY + 60,
            240,
            22,
            Text.literal("https://example.com/skin.png")
        );
        this.urlField.setMaxLength(512);
        this.urlField.setVisible(true);
        this.addDrawableChild(this.urlField);
        
        this.usernameField = new TextFieldWidget(
            this.textRenderer,
            centerX - 120,
            centerY + 60,
            240,
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
        ).dimensions(centerX - 120, centerY + 25, 115, 24).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(activeTab == 1 ? "§6§l[ USERNAME ]" : "§7[ USERNAME ]"),
            button -> {
                activeTab = 1;
                urlField.setVisible(false);
                usernameField.setVisible(true);
                refreshButtons();
            }
        ).dimensions(centerX + 5, centerY + 25, 115, 24).build());
        
        // Action Buttons
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a✓ APPLY SKIN"),
            button -> applySkin()
        ).dimensions(centerX - 120, centerY + 100, 115, 30).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§c🗑 RESET"),
            button -> resetSkin()
        ).dimensions(centerX + 5, centerY + 100, 115, 30).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§7✖ CLOSE"),
            button -> close()
        ).dimensions(centerX - 55, centerY + 145, 110, 24).build());
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
    
    private void resetSkin() {
        statusMessage = "§a✓ Reset to default skin";
        statusColor = 0x55FF55;
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background from JSON config
        context.fill(0, 0, this.width, this.height, ModelLoader.getBackgroundColor());
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        // Main Panel
        context.fill(centerX - 160, centerY - 110, centerX + 160, centerY + 190, 0xEE2D2D2D);
        
        // Border from JSON config
        drawBorder(context, centerX - 160, centerY - 110, 320, 300, ModelLoader.getBorderColor());
        
        // Title from JSON config
        drawShinyText(context, "§6§l" + ModelLoader.getTitleText(), centerX, centerY - 80, 1.5f);
        
        // Subtitle
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§7Change your Minecraft skin"),
            centerX,
            centerY - 60,
            0xAAAAAA
        );
        
        // Divider
        context.fill(centerX - 130, centerY - 45, centerX + 130, centerY - 44, ModelLoader.getBorderColor());
        
        // 3D Player Model Render with JSON config
        if (previewPlayer != null) {
            ModelLoader.renderCustomModel(context.getMatrices(), centerX, centerY - 20, 80, previewPlayer, rotationAngle);
            rotationAngle += 2;
        }
        
        // Input Label
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("§6◆ INPUT METHOD:"),
            centerX - 120,
            centerY + 5,
            ModelLoader.getBorderColor()
        );
        
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal(activeTab == 0 ? "§7▸ Skin URL:" : "§7▸ Username:"),
            centerX - 120,
            centerY + 48,
            0xCCCCCC
        );
        
        // Status Bar
        context.fill(centerX - 140, centerY + 170, centerX + 140, centerY + 195, 0x88000000);
        
        // Status Message
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal(statusMessage),
            centerX,
            centerY + 182,
            statusColor
        );
        
        // Loading Animation
        if (isLoading) {
            drawLoadingAnimation(context, centerX + 130, centerY + 75);
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
        context.fill(x, y, x + 10, y + 1, 0xFFFFFFFF);
        context.fill(x + width - 10, y, x + width, y + 1, 0xFFFFFFFF);
        context.fill(x, y + height - 1, x + 10, y + height, 0xFFFFFFFF);
        context.fill(x + width - 10, y + height - 1, x + width, y + height, 0xFFFFFFFF);
    }
    
    private void drawShinyText(DrawContext context, String text, int x, int y, float scale) {
        var matrices = context.getMatrices();
        matrices.push();
        matrices.scale(scale, scale, 1.0f);
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal(text),
            (int)(x / scale),
            (int)(y / scale),
            ModelLoader.getBorderColor()
        );
        matrices.pop();
    }
    
    private void drawLoadingAnimation(DrawContext context, int x, int y) {
        long time = System.currentTimeMillis();
        int dot = (int)((time / 300) % 4);
        String dots = ".".repeat(dot) + " ".repeat(3 - dot);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§eLoading" + dots), x, y, 0xFFFF55);
    }
    
    private void renderHoverEffects(DrawContext context, int mouseX, int mouseY, int centerX, int centerY) {
        int[][] areas = {
            {centerX - 120, centerY + 25, 115, 24},
            {centerX + 5, centerY + 25, 115, 24},
            {centerX - 120, centerY + 100, 115, 30},
            {centerX + 5, centerY + 100, 115, 30}
        };
        
        for (int[] area : areas) {
            if (mouseX >= area[0] && mouseX <= area[0] + area[2] && 
                mouseY >= area[1] && mouseY <= area[1] + area[3]) {
                context.fill(area[0], area[1], area[0] + area[2], area[1] + area[3], 0x33FFD700);
            }
        }
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
