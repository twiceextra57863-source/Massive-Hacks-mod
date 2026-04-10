package net.yourname.pvpmod.indicator;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.yourname.pvpmod.config.ConfigManager;
import net.yourname.pvpmod.config.IndicatorStyle;
import net.yourname.pvpmod.config.ModConfig;
import net.yourname.pvpmod.util.AnimationUtils;
import net.yourname.pvpmod.util.RenderUtils;

import java.util.*;

public class PlayerIndicator {
    // Animation state per player
    private static final Map<UUID, IndicatorState> states = new HashMap<>();
    
    public static void renderAll(DrawContext ctx, MinecraftClient mc, Camera camera, 
                                Matrix4f view, Matrix4f proj) {
        if (!ConfigManager.get().enabled || mc.world == null || mc.player == null) return;
        
        ModConfig cfg = ConfigManager.get();
        Vec3d camPos = camera.getPos();
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !player.isAlive()) continue;
            if (mc.player.squaredDistanceTo(player) > cfg.maxRenderDistance * cfg.maxRenderDistance) continue;
            
            // Skip if on same team (optional)
            if (player.getScoreboardTeam() != null && 
                player.getScoreboardTeam().equals(mc.player.getScoreboardTeam()) &&
                !player.getScoreboardTeam().isFriendlyFireAllowed()) {
                continue;
            }
            
            renderForPlayer(ctx, mc, player, camPos, view, proj, cfg);
        }
    }
    
    private static void renderForPlayer(DrawContext ctx, MinecraftClient mc, PlayerEntity player,
                                       Vec3d camPos, Matrix4f view, Matrix4f proj, ModConfig cfg) {
        // Get or create animation state
        UUID id = player.getUuid();
        IndicatorState state = states.computeIfAbsent(id, k -> new IndicatorState(player));
        state.update(player, cfg);
        
        // Calculate screen position (above head)
        double px = player.getX();
        double py = player.getY() + cfg.yOffset + (player.isSneaking() ? -0.3 : 0);
        double pz = player.getZ();
        
        double[] screen = RenderUtils.projectToScreen(px, py, pz, camPos, view, proj, mc);
        if (screen == null) return; // Off-screen
        
        int x = (int) screen[0];
        int y = (int) screen[1];
        
        // Render based on style
        switch (cfg.style) {
            case VANILLA_HEARTS -> 
                VanillaHeartsRenderer.render(ctx, mc, x, y, state, cfg, player);
            case STATUS_BAR -> 
                StatusBarRenderer.render(ctx, mc, x, y, state, cfg, player);
            case SPARK_HEAD -> 
                SparkHeadRenderer.render(ctx, mc, x, y, state, cfg, player);
        }
    }
    
    public static void clearCache() {
        states.clear();
    }
    
    // Per-player animation state
    private static class IndicatorState {
        float currentHealth; // Animated value (0.0 - 1.0)
        float targetHealth;  // Actual health percent
        long lastChangeTime; // For animation timing
        boolean isIncreasing; // For eat animation
        boolean isShaking;    // For break animation
        float shakeOffset;
        
        IndicatorState(PlayerEntity player) {
            targetHealth = Math.max(0, player.getHealth()) / Math.max(1, player.getMaxHealth());
            currentHealth = targetHealth;
        }
        
        void update(PlayerEntity player, ModConfig cfg) {
            targetHealth = Math.max(0, player.getHealth()) / Math.max(1, player.getMaxHealth());
            
            if (Math.abs(currentHealth - targetHealth) > 0.001f) {
                isIncreasing = targetHealth > currentHealth;
                lastChangeTime = System.currentTimeMillis();
                
                if (!isIncreasing && cfg.sparkBreakAnimation) {
                    isShaking = true;
                    shakeOffset = 0;
                }
            }
            
            // Smooth animation
            if (cfg.smoothAnimation) {
                float delta = cfg.animationSpeed;
                currentHealth = AnimationUtils.lerp(delta, currentHealth, targetHealth);
            } else {
                currentHealth = targetHealth;
            }
            
            // Shake animation decay
            if (isShaking) {
                shakeOffset += 0.5f;
                if (shakeOffset > 30) {
                    isShaking = false;
                    shakeOffset = 0;
                }
            }
        }
        
        float getRenderHealth() {
            return isShaking ? currentHealth + (float)(Math.sin(shakeOffset * 0.3) * 0.02f) : currentHealth;
        }
    }
}
