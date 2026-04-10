package net.yourname.pvpmod.indicator;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.yourname.pvpmod.config.ModConfig;
import net.yourname.pvpmod.indicator.PlayerIndicator.IndicatorState;

public class SparkHeadRenderer {
    private static final Identifier SPARK_TEXTURE = Identifier.of("pvpmod", "spark");
    
    public static void render(DrawContext ctx, MinecraftClient mc, int x, int y, 
                             IndicatorState state, ModConfig cfg, PlayerEntity player) {
        float health = state.getRenderHealth();
        int totalSparks = 10; // 10 sparks = 20 HP = full health
        int activeSparks = (int) (health * totalSparks);
        
        int sparkSize = cfg.sparkSize;
        int gap = cfg.sparkGap;
        int headSize = 14;
        
        // Calculate total width
        int totalWidth = headSize + (totalSparks * (sparkSize + gap));
        int startX = x - totalWidth / 2;
        
        // ===== DRAW HEAD =====
        // Head background (player skin color approximation)
        ctx.fill(startX, y, startX + headSize, y + headSize, 0xFFC0A080);
        // Head outline
        ctx.drawHorizontalLine(startX, startX + headSize, y, 0xFFFFFFFF);
        ctx.drawVerticalLine(startX, y, y + headSize, 0xFFFFFFFF);
        ctx.drawHorizontalLine(startX, startX + headSize, y + headSize, 0xFFFFFFFF);
        ctx.drawVerticalLine(startX + headSize, y, y + headSize, 0xFFFFFFFF);
        // Eyes
        ctx.fill(startX + 3, y + 4, startX + 5, y + 6, 0x202020);
        ctx.fill(startX + 9, y + 4, startX + 11, y + 6, 0x202020);
        
        // ===== DRAW SPARKS =====
        int sparkX = startX + headSize + gap;
        for (int i = 0; i < totalSparks; i++) {
            boolean active = i < activeSparks;
            boolean justBroke = !active && state.isShaking && i == activeSparks;
            boolean justGained = active && state.isIncreasing && i == activeSparks - 1;
            
            int sx = sparkX + i * (sparkSize + gap);
            int sy = y + 2;
            
            if (active) {
                // Active spark (golden)
                ctx.fill(sx, sy, sx + sparkSize, sy + sparkSize, 0xFFFFAA00);
                // Shine effect
                ctx.fill(sx + 1, sy + 1, sx + 3, sy + 3, 0xFFFFFFFF);
                
                // Eat animation: pulse
                if (justGained && cfg.sparkEatAnimation) {
                    float pulse = (float) Math.sin(System.currentTimeMillis() * 0.01) * 0.3f + 0.7f;
                    ctx.fill(sx, sy, sx + (int)(sparkSize * pulse), sy + (int)(sparkSize * pulse), 0x80FFFFFF);
                }
            } else {
                // Inactive spark (dark)
                ctx.fill(sx, sy, sx + sparkSize, sy + sparkSize, 0x303030);
                
                // Break animation: shake + fade
                if (justBroke && cfg.sparkBreakAnimation) {
                    int shake = (int) (Math.sin(state.shakeOffset * 0.5) * 3);
                    ctx.fill(sx + shake, sy, sx + sparkSize + shake, sy + sparkSize, 0x60FF4444);
                    // Crack lines
                    ctx.drawHorizontalLine(sx + 2, sx + sparkSize - 2, sy + sparkSize/2, 0xFFFF6666);
                    ctx.drawVerticalLine(sx + sparkSize/2, sy + 2, sy + sparkSize - 2, 0xFFFF6666);
                }
            }
            
            // Spark border
            ctx.drawHorizontalLine(sx, sx + sparkSize, sy, 0x80FFFFFF);
            ctx.drawVerticalLine(sx, sy, sy + sparkSize, 0x80FFFFFF);
        }
        
        // ===== HEALTH TEXT =====
        if (cfg.showHealthText) {
            String hp = String.valueOf((int) (health * 20));
            int tw = mc.textRenderer.getWidth(hp);
            ctx.drawText(mc.textRenderer, hp, x - tw/2, y - 12, 0xFFFFFF, true);
        }
    }
}
