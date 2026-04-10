package com.yourname.skincustomizer.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class SkinDashboardScreen extends Screen {
    
    private final Screen parent;
    private int selectedSlot = -1;
    
    // Dashboard colors (pure code - no textures)
    private static final int BG_DARK = 0xFF0A0A0A;
    private static final int PANEL_BG = 0xCC151515;
    private static final int BORDER_COLOR = 0xFF00B3FF;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFAAAAAA;
    private static final int TEXT_SUCCESS = 0xFF88FF88;
    private static final int TEXT_WARNING = 0xFFFF8888;
    
    public SkinDashboardScreen(Screen parent) {
        super(Component.literal("Skin Dashboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int startY = this.height / 2 - 30;

        // 3 Skin Slot Buttons
        for (int i = 0; i < 3; i++) {
            final int slotIndex = i;
            String slotName = "Slot " + (i + 1);
            
            Button btn = Button.builder(
                    Component.literal((selectedSlot == i ? "§a✓ " : "§7○ ") + slotName),
                    b -> {
                        selectedSlot = slotIndex;
                        // Button text update karne ke liye screen re-init karo
                        this.init();
                    }
            )
            .bounds(centerX - 100, startY + (i * 30), 200, 20)
            .build();
            this.addRenderableWidget(btn);
        }

        // Apply Button (Visual Only - Client Side Demo)
        Button applyBtn = Button.builder(
                Component.literal("§e§l✨ Apply Skin"),
                btn -> {
                    if (selectedSlot >= 0) {
                        // Yahan tum apni skin apply logic daal sakte ho
                        // Example: SkinManager.call() ya config save
                    }
                }
        )
        .bounds(centerX - 100, this.height / 2 + 70, 200, 20)
        .build();
        this.addRenderableWidget(applyBtn);

        // Back Button
        this.addRenderableWidget(Button.builder(
                Component.literal("§f⬅ Back"),
                btn -> this.minecraft.setScreen(this.parent)
        )
        .bounds(centerX - 100, this.height - 50, 200, 20)
        .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // 1. Full Screen Dark Background (Code Drawn)
        graphics.fill(0, 0, this.width, this.height, BG_DARK);

        // 2. Dashboard Panel Container
        int panelX = this.width / 2 - 160;
        int panelY = this.height / 2 - 100;
        int panelW = 320;
        int panelH = 260;

        // Panel Background
        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_BG);
        
        // Panel Border (4 sides)
        graphics.fill(panelX, panelY, panelX + panelW, panelY + 2, BORDER_COLOR); // Top
        graphics.fill(panelX, panelY + panelH - 2, panelX + panelW, panelY + panelH, BORDER_COLOR); // Bottom
        graphics.fill(panelX, panelY, panelX + 2, panelY + panelH, BORDER_COLOR); // Left
        graphics.fill(panelX + panelW - 2, panelY, panelX + panelW, panelY + panelH, BORDER_COLOR); // Right

        // 3. Title Text
        graphics.drawCenteredString(this.font, this.title, this.width / 2, panelY + 10, BORDER_COLOR);
        graphics.drawCenteredString(this.font, "§7Select a skin slot below", this.width / 2, panelY + 28, TEXT_SECONDARY);

        // 4. Preview Box (Pure Code - No Texture)
        int previewX = panelX + 20;
        int previewY = panelY + 55;
        int previewSize = 70;
        
        // Preview Background
        graphics.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF202020);
        graphics.fill(previewX + 1, previewY + 1, previewX + previewSize - 1, previewY + previewSize - 1, 0xFF2A2A2A);
        
        // Fake Player Head (ASCII Art Style - Code Only)
        graphics.drawString(this.font, "§f👤", previewX + 25, previewY + 25, TEXT_PRIMARY);
        graphics.drawCenteredString(this.font, "§7Preview", previewX + previewSize / 2, previewY - 12, TEXT_SECONDARY);

        // 5. Info Panel (Right Side)
        int infoX = previewX + previewSize + 25;
        int infoY = previewY;
        
        graphics.drawString(this.font, "§f📦 Selected:", infoX, infoY, TEXT_PRIMARY);
        graphics.drawString(this.font, "§b" + (selectedSlot >= 0 ? "Slot " + (selectedSlot + 1) : "None"), infoX, infoY + 12, TEXT_PRIMARY);
        
        graphics.drawString(this.font, "§f🔹 Status:", infoX, infoY + 35, TEXT_PRIMARY);
        graphics.drawString(this.font, "§aClient-Side Only", infoX, infoY + 47, TEXT_SUCCESS);
        
        graphics.drawString(this.font, "§f🔹 Note:", infoX, infoY + 70, TEXT_PRIMARY);
        graphics.drawString(this.font, "§eOthers won't see", infoX, infoY + 82, TEXT_WARNING);

        // 6. Render Buttons (Super call)
        super.render(graphics, mouseX, mouseY, delta);
        
        // 7. Tooltip for Apply Button (Optional)
        if (selectedSlot < 0) {
            graphics.drawTooltip(this.font, 
                java.util.List.of(Component.literal("§cSelect a slot first!")), 
                mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
