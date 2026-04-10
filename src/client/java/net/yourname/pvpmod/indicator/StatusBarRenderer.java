package net.yourname.pvpmod.indicator;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.yourname.pvpmod.config.ModConfig;

public class StatusBarRenderer {
    
    public static void render(DrawContext ctx, MinecraftClient mc, int x, int y, 
                             IndicatorState state, ModConfig cfg, PlayerEntity player) {
        int w = cfg.barWidth, h = cfg.barHeight;
        float health = state.getRenderHealth();
        
        // Background
        ctx.fill(x - w/2 - 1, y - 1, x + w/2 + 1, y + h + 1, 
                cfg.backgroundColor | 0x90000000);
        
        // Color selection
        int color = getColorForHealth(health, cfg);
        int filled = (int) (w * health);
        
        // Health fill
        ctx.fill(x - w/2, y, x - w/2 + filled, y + h, color | 0xFF000000);
        
        // Border
        ctx.drawHorizontalLine(x - w/2 - 1, x + w/2 + 1, y - 1, cfg.borderColor | 0xFF000000);
        ctx.drawHorizontalLine(x - w/2 - 1, x + w/2 + 1, y + h, cfg.borderColor | 0xFF000000);
        ctx.drawVerticalLine(x - w/2 - 1, y - 1, y + h, cfg.borderColor | 0xFF000000);
        ctx.drawVerticalLine(x + w/2 + 1, y - 1, y + h, cfg.borderColor | 0xFF000000);
        
        // Health text
        if (cfg.showHealthText) {
            String hp = String.valueOf((int) (health * 20));
            int tw = mc.textRenderer.getWidth(hp);
            ctx.drawText(mc.textRenderer, hp, x - tw/2, y - 11, 0xFFFFFF, true);
        }
    }
    
    private static int getColorForHealth(float percent, ModConfig cfg) {
        if (percent > 0.6f) return cfg.colorFull;
        if (percent > 0.3f) return cfg.colorHalf;
        return cfg.colorLow;
    }
}
