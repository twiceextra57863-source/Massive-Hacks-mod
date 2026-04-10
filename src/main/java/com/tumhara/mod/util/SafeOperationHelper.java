package com.tumhara.mod.util;

import net.minecraft.client.MinecraftClient;
import com.tumhara.mod.SkinChangerMod;

public class SafeOperationHelper {
    
    public static void runOnRenderThread(Runnable task) {
        var client = MinecraftClient.getInstance();
        if (client != null) {
            client.execute(task);
        } else {
            SkinChangerMod.LOGGER.warn("Client null, running on current thread");
            new Thread(task).start();
        }
    }
    
    public static boolean isGameLoaded() {
        var client = MinecraftClient.getInstance();
        return client != null && client.player != null && client.world != null;
    }
    
    public static void safeSkinChange(Runnable skinChangeTask) {
        try {
            runOnRenderThread(() -> {
                try {
                    if (isGameLoaded()) {
                        skinChangeTask.run();
                    } else {
                        SkinChangerMod.LOGGER.warn("Game not fully loaded, skin change deferred");
                    }
                } catch (Exception e) {
                    SkinChangerMod.LOGGER.error("Skin change failed", e);
                }
            });
        } catch (Exception e) {
            SkinChangerMod.LOGGER.error("Failed to schedule skin change", e);
        }
    }
}
