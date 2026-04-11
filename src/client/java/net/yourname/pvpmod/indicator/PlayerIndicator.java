package net.yourname.pvpmod.indicator;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import net.yourname.pvpmod.config.*;
import net.yourname.pvpmod.util.RenderUtils;
import java.util.*;

public class PlayerIndicator {
    private static final Map<UUID, IndicatorState> states = new HashMap<>();
    
    public static void renderAll(DrawContext ctx, MinecraftClient mc, Camera cam, 
                                Matrix4f view, Matrix4f proj) {
        if (!ConfigManager.get().enabled || mc.world == null || mc.player == null) return;
        ModConfig cfg = ConfigManager.get();
        Vec3d cp = cam.getPos();
        
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            if (mc.player.squaredDistanceTo(p) > cfg.maxRenderDistance * cfg.maxRenderDistance) continue;
            if (cfg.filterByTeam && p.getScoreboardTeam() != null && 
                p.getScoreboardTeam().equals(mc.player.getScoreboardTeam()) && 
                !p.getScoreboardTeam().isFriendlyFireAllowed()) continue;
            
            UUID id = p.getUuid();
            IndicatorState st = states.computeIfAbsent(id, k -> new IndicatorState(p));
            st.update(p, cfg);
            
            double[] scr = RenderUtils.projectToScreen(
                p.getX(), p.getY() + cfg.yOffset + (p.isSneaking() ? -0.3 : 0), p.getZ(), 
                cp, view, proj, mc);
            if (scr == null) continue;
            
            int x = (int)scr[0], y = (int)scr[1];
            switch(cfg.style) {
                case VANILLA_HEARTS -> VanillaHeartsRenderer.render(ctx, mc, x, y, st, cfg, p);
                case STATUS_BAR -> StatusBarRenderer.render(ctx, mc, x, y, st, cfg, p);
                case SPARK_HEAD -> SparkHeadRenderer.render(ctx, mc, x, y, st, cfg, p);
            }
        }
    }
    public static void clearCache() { states.clear(); }
}
