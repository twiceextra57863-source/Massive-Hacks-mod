package com.tumhara.mod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkinChangerMod implements ModInitializer {
    public static final String MOD_ID = "skinchanger";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Skin Changer Mod initialized for 1.21.4!");
    }
}
