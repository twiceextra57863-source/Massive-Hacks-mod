package net.yourname.pvpmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f; // ✅ JOML for 1.21.4
import net.yourname.pvpmod.config.ConfigManager;
import net.yourname.pvpmod.indicator.PlayerIndicator;

public class PvPModClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        ConfigManager.get();
        
        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options.hudHidden || mc.world == null || mc.player == null) return;
            
            Camera camera = mc.gameRenderer.getCamera();
            if (camera.isThirdPerson()) return;
            
            // ✅ Get matrices properly for 1.21.4
            Matrix4f view = new Matrix4f();
            camera.getProjectionMatrix().get(view); // ✅ Correct way
            
            Matrix4f proj = new Matrix4f(mc.gameRenderer.getProjectionMatrix());
            
            PlayerIndicator.renderAll(ctx, mc, camera, view, proj);
        });
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null) PlayerIndicator.clearCache();
        });
    }
}
