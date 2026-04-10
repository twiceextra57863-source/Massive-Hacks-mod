package com.yourname.skincustomizer.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class SkinDashboardScreen extends Screen {
    private final Screen parent;
    
    public SkinDashboardScreen(Screen parent) {
        super(Component.literal("Skin Dashboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int cy = this.height / 2;
        
        this.addRenderableWidget(Button.builder(
                Component.literal("Slot 1"),
                b -> {}
        ).bounds(cx - 100, cy - 20, 200, 20).build());
        
        this.addRenderableWidget(Button.builder(
                Component.literal("⬅ Back"),
                b -> this.minecraft.setScreen(parent)
        ).bounds(cx - 100, cy + 40, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, this.width, this.height, 0xFF101010);
        g.fill(this.width/2-120, this.height/2-80, this.width/2+120, this.height/2+80, 0xCC202020);
        g.drawCenteredString(this.font, this.title, this.width/2, this.height/2-70, 0xFFFFFF);
        super.render(g, mx, my, delta);
    }

    @Override
    public boolean keyPressed(int kc, int sc, int mod) {
        if (kc == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(parent);
            return true;
        }
        return super.keyPressed(kc, sc, mod);
    }
}
