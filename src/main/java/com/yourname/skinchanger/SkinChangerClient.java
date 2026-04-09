package com.yourname.skinchanger;

import com.yourname.skinchanger.config.SkinChangerConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
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
                
                SkinChangerConfig.setCurrentSkin(skinFileName);
                SkinChangerConfig.save();
                
                // Force skin refresh in 1.21.4
                if (client.player != null) {
                    client.player.clearCape();
                    // Force skin reload by triggering a dummy skin change
                    client.options.getPlayerModelParts().forEach(part -> {
                        client.options.togglePlayerModelPart(part, true);
                    });
                }
                
                SkinChangerMod.LOGGER.info("Skin loaded: " + skinFileName);
            });
        } catch (IOException e) {
            SkinChangerMod.LOGGER.error("Failed to load skin: " + skinFileName, e);
        }
    }
    
    public static Identifier getCustomSkin() {
        return CUSTOM_SKIN;
    }
}
