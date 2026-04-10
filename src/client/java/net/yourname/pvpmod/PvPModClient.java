package net.yourname.pvpmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.yourname.pvpmod.config.ConfigManager;
import net.yourname.pvpmod.indicator.PlayerIndicator;

public class PvPModClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        // Load config
        ConfigManager.get();
        
        // Register HUD renderer for indicators
        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.options.hudHidden || mc.gameRenderer.getCamera().isThirdPerson()) return;
            
            var camera = mc.gameRenderer.getCamera();
            var view = camera.getProjectionMatrix();
            var proj = mc.gameRenderer.getProjectionMatrix(mc.getTickDelta());
            
            PlayerIndicator.renderAll(ctx, mc, camera, view, proj);
        });
        
        // Clear cache on world change
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents.LOAD.register((client, world) -> 
            PlayerIndicator.clearCache());
    }
}
