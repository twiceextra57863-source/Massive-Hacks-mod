package com.tumhara.mod.client.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileReader;

public class ModelLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("ModelLoader");
    private static JsonObject modelConfig;
    private static final File CONFIG_FILE = new File("config/skinchanger/model_config.json");
    
    public static void loadModelConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                modelConfig = JsonParser.parseReader(reader).getAsJsonObject();
                LOGGER.info("Model config loaded successfully!");
            } catch (Exception e) {
                LOGGER.error("Failed to load model config", e);
                createDefaultConfig();
            }
        } else {
            createDefaultConfig();
        }
    }
    
    private static void createDefaultConfig() {
        modelConfig = new JsonObject();
        
        JsonObject model = new JsonObject();
        model.addProperty("type", "player");
        model.addProperty("scale", 1.0);
        model.addProperty("rotationSpeed", 2.0);
        model.addProperty("bobbing", false);
        modelConfig.add("model", model);
        
        JsonObject gui = new JsonObject();
        gui.addProperty("backgroundColor", 0xCC1A1A1A);
        gui.addProperty("borderColor", 0xFFD4AF37);
        gui.addProperty("accentColor", 0xFFFFAA00);
        gui.addProperty("titleText", "SKIN STUDIO");
        modelConfig.add("gui", gui);
        
        LOGGER.info("Created default model config");
    }
    
    public static void renderCustomModel(MatrixStack matrices, int x, int y, int size, PlayerEntity player, float rotationAngle) {
        if (modelConfig == null) loadModelConfig();
        
        matrices.push();
        
        matrices.translate(x, y + 10, 100);
        
        float scale = size / 80.0f;
        if (modelConfig != null && modelConfig.has("model") && modelConfig.getAsJsonObject("model").has("scale")) {
            scale *= modelConfig.getAsJsonObject("model").get("scale").getAsFloat();
        }
        matrices.scale(scale, scale, scale);
        
        float rotSpeed = 2.0f;
        if (modelConfig != null && modelConfig.has("model") && modelConfig.getAsJsonObject("model").has("rotationSpeed")) {
            rotSpeed = modelConfig.getAsJsonObject("model").get("rotationSpeed").getAsFloat();
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        
        try {
            dispatcher.render(player, 0, 0, 0, rotationAngle * rotSpeed, 1.0f, matrices, 
                client.getBufferBuilders().getEntityVertexConsumers(), 
                net.minecraft.client.render.LightmapTextureManager.MAX_LIGHT_COORDINATE);
            client.getBufferBuilders().getEntityVertexConsumers().draw();
        } catch (Exception e) {
            // Fallback - don't render
        }
        
        matrices.pop();
    }
    
    public static int getBackgroundColor() {
        if (modelConfig != null && modelConfig.has("gui") && 
            modelConfig.getAsJsonObject("gui").has("backgroundColor")) {
            return modelConfig.getAsJsonObject("gui").get("backgroundColor").getAsInt();
        }
        return 0xCC1A1A1A;
    }
    
    public static int getBorderColor() {
        if (modelConfig != null && modelConfig.has("gui") && 
            modelConfig.getAsJsonObject("gui").has("borderColor")) {
            return modelConfig.getAsJsonObject("gui").get("borderColor").getAsInt();
        }
        return 0xFFD4AF37;
    }
    
    public static int getAccentColor() {
        if (modelConfig != null && modelConfig.has("gui") && 
            modelConfig.getAsJsonObject("gui").has("accentColor")) {
            return modelConfig.getAsJsonObject("gui").get("accentColor").getAsInt();
        }
        return 0xFFFFAA00;
    }
    
    public static String getTitleText() {
        if (modelConfig != null && modelConfig.has("gui") && 
            modelConfig.getAsJsonObject("gui").has("titleText")) {
            return modelConfig.getAsJsonObject("gui").get("titleText").getAsString();
        }
        return "SKIN STUDIO";
    }
}
