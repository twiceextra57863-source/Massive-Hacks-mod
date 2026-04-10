package net.yourname.pvpmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.joml.Matrix4f;  // ✅ JOML
import net.yourname.pvpmod.config.ConfigManager;
import net.yourname.pvpmod.indicator.PlayerIndicator;

public class PvPModClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        ConfigManager.get();
        
        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options.hudHidden || mc.gameRenderer.getCamera().isThirdPerson()) return;
            
            var camera = mc.gameRenderer.getCamera();
            // ✅ Get JOML matrices properly
            Matrix4f view = new Matrix4f(camera.getProjectionMatrix());
            Matrix4f proj = mc.gameRenderer.getProjectionMatrix(mc.getTickDelta());
            
            PlayerIndicator.renderAll(ctx, mc, camera, view, proj);
        });
        
        ClientWorldEvents.LOAD.register((client, world) -> 
            PlayerIndicator.clearCache());
    }
}
