package com.yourname.skinchanger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SkinChangerClient implements ClientModInitializer {
    private static final Identifier CUSTOM_SKIN = Identifier.of(SkinChangerMod.MOD_ID, "custom_skin");
    private static final Path SKINS_DIR = Paths.get("config", "skinchanger", "skins");

    @Override
    public void onInitializeClient() {
        SkinChangerMod.LOGGER.info("Skin Changer Client Initialized!");
        createSkinsDirectory();
    }

    private static void createSkinsDirectory() {
        File skinsDir = SKINS_DIR.toFile();
        if (!skinsDir.exists()) {
            skinsDir.mkdirs();
        }
    }

    public static void loadSkin(MinecraftClient client, String skinFileName) {
        File skinFile = SKINS_DIR.resolve(skinFileName).toFile();
        if (!skinFile.exists()) {
            SkinChangerMod.LOGGER.error("Skin file not found: " + skinFileName);
            return;
        }

        try (FileInputStream fis = new FileInputStream(skinFile)) {
            NativeImage image = NativeImage.read(fis);
            
            client.execute(() -> {
                client.getTextureManager().registerTexture(CUSTOM_SKIN, 
                    new NativeImageBackedTexture(image));
                
                PlayerSkinProvider skinProvider = client.getSkinProvider();
                
                // Skin apply karna (1.21.4 compatible way)
                applyPlayerSkin(client);
                
                SkinChangerConfig.setCurrentSkin(skinFileName);
                SkinChangerConfig.save();
            });
        } catch (IOException e) {
            SkinChangerMod.LOGGER.error("Failed to load skin: " + skinFileName, e);
        }
    }

    private static void applyPlayerSkin(MinecraftClient client) {
        if (client.player != null) {
            // Reload player skin
            client.player.reloadSkin();
        }
    }
}
