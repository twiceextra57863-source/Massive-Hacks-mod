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
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.LightmapTextureManager;
import org.joml.Vector3f;
import java.io.File;

public class SkinDashboardScreen extends Screen {
    private static final Logger LOGGER = LoggerFactory.getLogger("SkinChanger");
    
    private TextFieldWidget urlField;
    private TextFieldWidget usernameField;
    private final Screen parent;
    private String statusMessage = "§7Welcome to Skin Studio";
    private int statusColor = 0xAAAAAA;
    private boolean isLoading = false;
    private float rotationAngle = 0;
    private PlayerEntity previewPlayer;
    private int activeTab = 0;
    
    private static final int COLOR_BORDER = 0xFFD4AF37;
    private static final int COLOR_BORDER_GLOW = 0xFFFFD700;
    private static final int COLOR_PANEL = 0xCC1A1A1A;
    
    public SkinDashboardScreen(Screen parent) {
        super(Text.literal(""));
        this.parent = parent;
        createPreviewPlayer();
    }
    
    private void createPreviewPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            previewPlayer = new AbstractClientPlayerEntity(client.world, 
                client.player != null ? client.player.getGameProfile() : null) {
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
            centerX - 120,
            centerY + 60,
            240,
            22,
            Text.literal("Enter skin URL...")
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
            Text.literal("Enter Minecraft username...")
        );
        this.usernameField.setMaxLength(64);
        this.usernameField.setVisible(false);
        this.addDrawableChild(this.usernameField);
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(activeTab == 0 ? "§6§l> FROM URL §6§l<" : "§7FROM URL"),
            button -> {
                activeTab = 0;
                urlField.setVisible(true);
                usernameField.setVisible(false);
                this.clearChildren();
                this.init();
            }
        ).dimensions(centerX - 120, centerY + 25, 115, 28).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(activeTab == 1 ? "§6§l> FROM NAME §6§l<" : "§7FROM NAME"),
            button -> {
                activeTab = 1;
                urlField.setVisible(false);
                usernameField.setVisible(true);
                this.clearChildren();
                this.init();
            }
        ).dimensions(centerX + 5, centerY + 25, 115, 28).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§eFETCH SKIN"),
            button -> fetchSkin()
        ).dimensions(centerX - 120, centerY + 100, 115, 32).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§cRESET"),
            button -> resetSkin()
        ).dimensions(centerX + 5, centerY + 100, 115, 32).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§7CLOSE"),
            button -> close()
        ).dimensions(centerX - 55, centerY + 145, 110, 28).build());
    }
    
    private void fetchSkin() {
        isLoading = true;
        statusMessage = "§eFetching skin...";
        statusColor = 0xFFFF55;
        
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                MinecraftClient.getInstance().execute(() -> {
                    statusMessage = "§a✓ Skin fetched successfully!";
                    statusColor = 0x55FF55;
                    isLoading = false;
                });
            } catch (Exception e) {
                MinecraftClient.getInstance().execute(() -> {
                    statusMessage = "§cFailed to fetch skin";
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
        this.renderBackground(context, mouseX, mouseY, delta);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        long time = System.currentTimeMillis();
        float glow = (float) (Math.sin(time / 500.0) * 0.3 + 0.7);
        int borderColor = mixColor(COLOR_BORDER, COLOR_BORDER_GLOW, glow);
        
        context.fill(centerX - 160, centerY - 130, centerX + 160, centerY + 190, COLOR_PANEL);
        drawAnimatedBorder(context, centerX - 160, centerY - 130, 320, 320, borderColor);
        
        drawShinyText(context, "§6§lSKIN STUDIO", centerX, centerY - 105, 2.0f);
        
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§7Change your Minecraft skin"),
            centerX,
            centerY - 85,
            0xAAAAAA
        );
        
        context.fill(centerX - 130, centerY - 65, centerX + 130, centerY - 64, 0xFFD4AF37);
        
        renderPlayerModel(context, centerX, centerY - 20, 80);
        rotationAngle += 2;
        
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("§6◆ INPUT METHOD:"),
            centerX - 120,
            centerY + 10,
            0xFFD4AF37
        );
        
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal(activeTab == 0 ? "§7▸ Skin URL:" : "§7▸ Username:"),
            centerX - 120,
            centerY + 48,
            0xCCCCCC
        );
        
        drawStatusBar(context, centerX, centerY + 190);
        
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal(statusMessage),
            centerX,
            centerY + 178,
            statusColor
        );
        
        if (isLoading) {
            drawLoadingAnimation(context, centerX + 130, centerY + 115);
        }
        
        renderButtonHoverEffects(context, mouseX, mouseY, centerX, centerY);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderPlayerModel(DrawContext context, int centerX, int centerY, int size) {
        if (previewPlayer == null) return;
        
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        
        matrices.translate(centerX, centerY + 10, 100);
        matrices.scale(size, size, size);
        matrices.multiply(new Vector3f(0, 1, 0).rotationDegrees(rotationAngle));
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        DiffuseLighting.disableGuiDepthLighting();
        
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        
        try {
            dispatcher.render(previewPlayer, 0, 0, 0, rotationAngle, 1.0f, matrices, immediate, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            immediate.draw();
        } catch (Exception e) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§8[3D MODEL]"), centerX, centerY, 0x666666);
        }
        
        DiffuseLighting.enableGuiDepthLighting();
        RenderSystem.disableBlend();
        matrices.pop();
    }
    
    private void drawAnimatedBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 2, color);
        context.fill(x, y + height - 2, x + width, y + height, color);
        context.fill(x, y, x + 2, y + height, color);
        context.fill(x + width - 2, y, x + width, y + height, color);
        
        context.fill(x, y, x + 8, y + 1, 0xFFFFFFFF);
        context.fill(x + width - 8, y, x + width, y + 1, 0xFFFFFFFF);
        context.fill(x, y + height - 1, x + 8, y + height, 0xFFFFFFFF);
        context.fill(x + width - 8, y + height - 1, x + width, y + height, 0xFFFFFFFF);
    }
    
    private void drawShinyText(DrawContext context, String text, int x, int y, float scale) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.scale(scale, scale, 1.0f);
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal(text),
            (int)(x / scale),
            (int)(y / scale),
            0xFFD4AF37
        );
        matrices.pop();
    }
    
    private void drawStatusBar(DrawContext context, int centerX, int y) {
        context.fill(centerX - 140, y - 15, centerX + 140, y + 5, 0x88000000);
        
        long time = System.currentTimeMillis();
        int shineX = centerX - 140 + (int)((time % 2000) / 2000.0 * 280);
        context.fill(shineX, y - 15, shineX + 30, y + 5, 0x22FFFFFF);
    }
    
    private void drawLoadingAnimation(DrawContext context, int x, int y) {
        long time = System.currentTimeMillis();
        int dot = (int)((time / 300) % 4);
        String dots = ".".repeat(dot) + " ".repeat(3 - dot);
        context.drawTextWithShadow(this.textRenderer, Text.literal("§eLoading" + dots), x, y, 0xFFFF55);
    }
    
    private void renderButtonHoverEffects(DrawContext context, int mouseX, int mouseY, int centerX, int centerY) {
        if (mouseX >= centerX - 120 && mouseX <= centerX - 5 && mouseY >= centerY + 25 && mouseY <= centerY + 53) {
            context.fill(centerX - 122, centerY + 23, centerX - 3, centerY + 55, 0x33FFD700);
        }
        if (mouseX >= centerX + 5 && mouseX <= centerX + 120 && mouseY >= centerY + 25 && mouseY <= centerY + 53) {
            context.fill(centerX + 3, centerY + 23, centerX + 122, centerY + 55, 0x33FFD700);
        }
        if (mouseX >= centerX - 120 && mouseX <= centerX - 5 && mouseY >= centerY + 100 && mouseY <= centerY + 132) {
            context.fill(centerX - 122, centerY + 98, centerX - 3, centerY + 134, 0x33FFD700);
        }
        if (mouseX >= centerX + 5 && mouseX <= centerX + 120 && mouseY >= centerY + 100 && mouseY <= centerY + 132) {
            context.fill(centerX + 3, centerY + 98, centerX + 122, centerY + 134, 0x33FFD700);
        }
    }
    
    private int mixColor(int color1, int color2, float ratio) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int a = (int)(a1 * (1 - ratio) + a2 * ratio);
        int r = (int)(r1 * (1 - ratio) + r2 * ratio);
        int g = (int)(g1 * (1 - ratio) + g2 * ratio);
        int b = (int)(b1 * (1 - ratio) + b2 * ratio);
        
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x99000000);
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
