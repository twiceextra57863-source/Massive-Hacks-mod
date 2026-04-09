package com.yourname.skinchanger;

import com.yourname.skinchanger.config.SkinChangerConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkinChangerMod implements ModInitializer {
    public static final String MOD_ID = "skinchanger";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Skin Changer Mod Initialized!");
        SkinChangerConfig.load();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            applyCurrentSkin(client);
        });
    }

    public static void applyCurrentSkin(MinecraftClient client) {
        String currentSkin = SkinChangerConfig.getCurrentSkin();
        if (currentSkin != null && !currentSkin.isEmpty()) {
            SkinChangerClient.loadSkin(client, currentSkin);
        }
    }
}
