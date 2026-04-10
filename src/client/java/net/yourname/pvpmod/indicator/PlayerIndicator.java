package net.yourname.pvpmod.indicator;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;  // ✅ JOML for 1.21.4
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.yourname.pvpmod.config.ConfigManager;
import net.yourname.pvpmod.config.IndicatorStyle;
import net.yourname.pvpmod.config.ModConfig;
import net.yourname.pvpmod.util.RenderUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerIndicator {
    private static final Map<UUID, IndicatorState> states = new HashMap<>();
    
    public static void renderAll(DrawContext ctx, MinecraftClient mc, Camera camera, 
                                Matrix4f view, Matrix4f proj) {
        if (!ConfigManager.get().enabled || mc.world == null || mc.player == null) return;
        
        ModConfig cfg = ConfigManager.get();
        Vec3d camPos = camera.getPos();
        
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !player.isAlive()) continue;
            if (mc.player.squaredDistanceTo(player) > cfg.maxRenderDistance * cfg.maxRenderDistance) continue;
            
            // Team filter
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
        UUID id = player.getUuid();
        IndicatorState state = states.computeIfAbsent(id, k -> new IndicatorState(player));
        state.update(player, cfg);
        
        double px = player.getX();
        double py = player.getY() + cfg.yOffset + (player.isSneaking() ? -0.3 : 0);
        double pz = player.getZ();
        
        double[] screen = RenderUtils.projectToScreen(px, py, pz, camPos, view, proj, mc);
        if (screen == null) return;
        
        int x = (int) screen[0];
        int y = (int) screen[1];
        
        switch (cfg.style) {
            case VANILLA_HEARTS -> 
                VanillaHeartsRenderer.render(ctx, mc, x, y, state, cfg, player);
            case STATUS_BAR -> 
                StatusBarRenderer.render(ctx, mc, x, y, state, cfg, player);
            case SPARK_HEAD -> 
                SparkHeadRenderer.render(ctx, mc, x, y, state, cfg, player);
        }
    }
    
    public static void clearCache() { states.clear(); }
}
