package com.tumhara.mod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

public class SkinChangerMod implements ModInitializer {
    public static final String MOD_ID = "skinchanger";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final File CONFIG_DIR = new File("config/skinchanger");

    @Override
    public void onInitialize() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
            LOGGER.info("Created config directory: " + CONFIG_DIR.getAbsolutePath());
        }
        LOGGER.info("§a✓ Skin Changer Mod initialized for 1.21.4!");
        LOGGER.info("§e📁 Config folder: " + CONFIG_DIR.getAbsolutePath());
    }
}
