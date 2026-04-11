package net.yourname.pvpmod.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.*;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .registerTypeAdapter(IndicatorStyle.class, new IndicatorStyleSerializer()).create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("pvpmod/indicator.json");
    private static ModConfig instance;
    
    public static ModConfig get() {
        if (instance == null) instance = load();
        return instance;
    }
    
    public static void save() {
        if (instance == null) return;
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(instance));
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private static ModConfig load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                ModConfig cfg = GSON.fromJson(Files.readString(CONFIG_PATH), ModConfig.class);
                cfg.validate(); return cfg;
            }
        } catch (Exception e) { e.printStackTrace(); }
        ModConfig cfg = new ModConfig(); cfg.validate(); return cfg;
    }
    
    static class IndicatorStyleSerializer implements JsonSerializer<IndicatorStyle>, JsonDeserializer<IndicatorStyle> {
        public JsonElement serialize(IndicatorStyle src, java.lang.reflect.Type t, JsonSerializationContext c) {
            return new JsonPrimitive(src.name());
        }
        public IndicatorStyle deserialize(JsonElement json, java.lang.reflect.Type t, JsonDeserializationContext c) {
            try { return IndicatorStyle.valueOf(json.getAsString()); } 
            catch (IllegalArgumentException e) { return IndicatorStyle.STATUS_BAR; }
        }
    }
}
