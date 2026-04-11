package net.yourname.pvpmod.indicator;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.yourname.pvpmod.config.ModConfig;

public class StatusBarRenderer {
    public static void render(DrawContext ctx, MinecraftClient mc, int x, int y, 
                             IndicatorState st, ModConfig cfg, PlayerEntity p) {
        int w = cfg.barWidth, h = cfg.barHeight;
        float hp = st.getRenderHealth();
        
        // Glass background
        ctx.fill(x-w/2-2, y-2, x+w/2+2, y+h+2, cfg.backgroundColor|0x90000000);
        ctx.fill(x-w/2-1, y-1, x+w/2+1, y+h+1, cfg.backgroundColor|0x60000000);
        ctx.fill(x-w/2, y, x+w/2, y+h, cfg.backgroundColor|0x30000000);
        
        // Health fill
        int col = hp>0.6f?cfg.colorFull : hp>0.3f?cfg.colorHalf : cfg.colorLow;
        ctx.fill(x-w/2, y, x-w/2+(int)(w*hp), y+h, col|0xFF000000);
        
        // Animated shine
        float shine = (System.currentTimeMillis()%1500)/1500f;
        if (shine < 0.25f) {
            int sx = x-w/2+(int)(shine/0.25f*w);
            ctx.fill(sx, y, Math.min(sx+15, x+w/2), y+h, 0x30FFFFFF);
        }
        
        // Border glow
        ctx.drawHorizontalLine(x-w/2-1, x+w/2+1, y-1, cfg.borderColor|0xFF000000);
        ctx.drawHorizontalLine(x-w/2-1, x+w/2+1, y+h, cfg.borderColor|0xFF000000);
        ctx.drawVerticalLine(x-w/2-1, y-1, y+h, cfg.borderColor|0xFF000000);
        ctx.drawVerticalLine(x+w/2+1, y-1, y+h, cfg.borderColor|0xFF000000);
        
        // Health text
        if (cfg.showHealthText) {
            String t = String.valueOf((int)(hp*20));
            ctx.drawText(mc.textRenderer, t, x-mc.textRenderer.getWidth(t)/2, y-12, 0xFFFFFF, true);
        }
    }
}
