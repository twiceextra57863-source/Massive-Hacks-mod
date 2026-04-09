package com.yourname.skinchanger.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yourname.skinchanger.SkinChangerMod;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.Path;

public class SkinChangerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
        .getConfigDir().resolve("skinchanger.json");
    
    private static ConfigData config = new ConfigData();

    public static class ConfigData {
        public String currentSkin = "";
    }

    public static void load() {
        if (!CONFIG_FILE.toFile().exists()) {
            save();
            return;
        }

        try (Reader reader = new FileReader(CONFIG_FILE.toFile())) {
            config = GSON.fromJson(reader, ConfigData.class);
            if (config == null) config = new ConfigData();
        } catch (IOException e) {
            SkinChangerMod.LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() {
        try (Writer writer = new FileWriter(CONFIG_FILE.toFile())) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            SkinChangerMod.LOGGER.error("Failed to save config", e);
        }
    }

    public static String getCurrentSkin() {
        return config.currentSkin;
    }

    public static void setCurrentSkin(String skinName) {
        config.currentSkin = skinName;
    }
}
