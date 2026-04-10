package com.yourname.skincustomizer.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class SkinDashboardScreen extends Screen {
    private final Screen parent;
    private String selectedSlot = "None";

    public SkinDashboardScreen(Screen parent) {
        super(Component.literal("Skin Dashboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int startY = this.height / 2 - 40;

        // 3 Slot Buttons
        for (int i = 1; i <= 3; i++) {
            final String slot = "Slot " + i;
            Button btn = Button.builder(
                    Component.literal("📦 " + slot),
                    b -> {
                        this.selectedSlot = slot;
                        b.setMessage(Component.literal("✅ " + slot + " Active"));
                    }
            ).bounds(centerX - 100, startY + (i * 35), 200, 25).build();
            this.addRenderableWidget(btn);
        }

        // Back Button
        this.addRenderableWidget(Button.builder(
                Component.literal("⬅ Back"),
                btn -> this.minecraft.setScreen(this.parent)
        ).bounds(centerX - 100, this.height - 60, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // 1. Pure Code Background (Dark)
        g.fill(0, 0, this.width, this.height, 0xFF0A0A0A);

        // 2. Dashboard Panel
        int px = this.width / 2 - 180;
        int py = this.height / 2 - 90;
        int pw = 360;
        int ph = 240;

        // Panel Base
        g.fill(px, py, px + pw, py + ph, 0xCC151515);
        // Panel Border (Top, Bottom, Left, Right)
        g.fill(px, py, px + pw, py + 3, 0xFF00B3FF);
        g.fill(px, py + ph - 3, px + pw, py + ph, 0xFF00B3FF);
        g.fill(px, py, px + 3, py + ph, 0xFF00B3FF);
        g.fill(px + pw - 3, py, px + pw, py + ph, 0xFF00B3FF);

        // 3. Title
        g.drawCenteredString(this.font, this.title, this.width / 2, py + 12, 0x00B3FF);
        g.drawString(this.font, "Select a skin slot below:", px + 20, py + 35, 0xAAAAAA);

        // 4. Preview Box (Code drawn)
        int bx = px + 20;
        int by = py + 60;
        g.fill(bx, by, bx + 80, by + 80, 0xFF202020);
        g.drawString(this.font, "PREVIEW", bx + 15, by - 10, 0x888888);
        
        // Fake Player Head (Code only)
        g.fill(bx + 20, by + 20, bx + 60, by + 60, 0xFF333333);
        g.drawString(this.font, "👤", bx + 30, by + 45, 0xFFFFFF);

        // 5. Info Text
        g.drawString(this.font, "🔹 Selected: " + selectedSlot, bx + 100, by + 20, 0xFFFFFF);
        g.drawString(this.font, "🔹 Status: Client-Side Only", bx + 100, by + 40, 0x88FF88);
        g.drawString(this.font, "🔹 Other players will not see changes", bx + 100, by + 60, 0xFF8888);

        super.render(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
