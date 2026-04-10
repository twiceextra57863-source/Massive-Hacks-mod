package net.yourname.pvpmod.indicator;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.yourname.pvpmod.config.ModConfig;
import net.yourname.pvpmod.indicator.PlayerIndicator.IndicatorState;

public class VanillaHeartsRenderer {
    
    public static void render(DrawContext ctx, MinecraftClient mc, int x, int y, 
                             IndicatorState state, ModConfig cfg, PlayerEntity player) {
        float health = state.getRenderHealth();
        int fullHearts = (int) (health * 10);
        float partial = (health * 10) - fullHearts;
        
        int heartSize = 9;
        int gap = 1;
        int totalWidth = 10 * (heartSize + gap) - gap;
        int startX = x - totalWidth / 2;
        
        for (int i = 0; i < 10; i++) {
            int hx = startX + i * (heartSize + gap);
            
            if (i < fullHearts) {
                // Full heart
                drawHeart(ctx, hx, y, cfg.colorFull, true);
            } else if (i == fullHearts && partial > 0.3f) {
                // Half heart
                drawHeart(ctx, hx, y, cfg.colorHalf, false);
            } else {
                // Empty heart
                drawHeart(ctx, hx, y, 0x404040, false);
            }
        }
        
        // Health text
        if (cfg.showHealthText) {
            String hp = String.valueOf((int) (health * 20));
            int tw = mc.textRenderer.getWidth(hp);
            ctx.drawText(mc.textRenderer, hp, x - tw/2, y - 11, 0xFFFFFF, true);
        }
    }
    
    private static void drawHeart(DrawContext ctx, int x, int y, int color, boolean full) {
        // Simplified heart shape using rectangles
        if (full) {
            // Top bumps
            ctx.fill(x + 2, y, x + 4, y + 2, color);
            ctx.fill(x + 5, y, x + 7, y + 2, color);
            // Body
            ctx.fill(x + 1, y + 2, x + 8, y + 7, color);
            // Bottom point
            ctx.fill(x + 3, y + 7, x + 6, y + 9, color);
        } else {
            // Outline only
            ctx.drawHorizontalLine(x + 2, x + 7, y, color);
            ctx.drawVerticalLine(x + 1, y + 2, y + 7, color);
            ctx.drawVerticalLine(x + 8, y + 2, y + 7, color);
            ctx.drawHorizontalLine(x + 2, x + 7, y + 8, color);
            ctx.fill(x + 4, y + 8, x + 5, y + 9, color);
        }
    }
}
