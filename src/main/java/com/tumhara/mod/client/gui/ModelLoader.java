package com.tumhara.mod.client.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
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
                LOGGER.info("Model config loaded!");
            } catch (Exception e) {
                createDefaultConfig();
            }
        } else {
            createDefaultConfig();
        }
    }
    
    private static void createDefaultConfig() {
        modelConfig = new JsonObject();
        JsonObject gui = new JsonObject();
        gui.addProperty("backgroundColor", 0xCC1A1A1A);
        gui.addProperty("borderColor", 0xFFD4AF37);
        gui.addProperty("titleText", "SKIN STUDIO");
        modelConfig.add("gui", gui);
    }
    
    public static void renderPlayerModel(MatrixStack matrices, int x, int y, int size, PlayerEntity player, float rotation) {
        if (player == null) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        matrices.push();
        
        // Position the model
        matrices.translate(x, y, 100);
        matrices.scale(size, size, size);
        matrices.multiply(new org.joml.Vector3f(0, 1, 0).rotationDegrees(rotation));
        matrices.multiply(new org.joml.Vector3f(1, 0, 0).rotationDegrees(0));
        
        // Render the player
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        
        try {
            dispatcher.render(player, 0, 0, 0, 0, 1.0f, matrices, immediate, 15728880);
            immediate.draw();
        } catch (Exception e) {
            // Fallback
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
    
    public static String getTitleText() {
        if (modelConfig != null && modelConfig.has("gui") && 
            modelConfig.getAsJsonObject("gui").has("titleText")) {
            return modelConfig.getAsJsonObject("gui").get("titleText").getAsString();
        }
        return "SKIN STUDIO";
    }
}
