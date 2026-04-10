package com.tumhara.mod.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import com.mojang.blaze3d.systems.RenderSystem;

public class SkinModelRenderer {
    
    public static void renderPlayerModel(MatrixStack matrices, int x, int y, int size, PlayerEntity player) {
        matrices.push();
        
        // Position the model
        matrices.translate(x, y, 50);
        matrices.scale(size, size, size);
        
        // Rotate to face camera
        matrices.multiply(net.minecraft.client.util.math.Vector3f.POSITIVE_X.getDegreesQuaternion(0));
        matrices.multiply(net.minecraft.client.util.math.Vector3f.POSITIVE_Y.getDegreesQuaternion(180));
        matrices.multiply(net.minecraft.client.util.math.Vector3f.POSITIVE_Z.getDegreesQuaternion(0));
        
        MinecraftClient client = MinecraftClient.getInstance();
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        
        // Setup lighting
        DiffuseLighting.disableGuiDepthLighting();
        
        // Render the player
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        dispatcher.render(player, 0, 0, 0, 0, 1, matrices, immediate, 15728880);
        immediate.draw();
        
        // Reset lighting
        DiffuseLighting.enableGuiDepthLighting();
        
        matrices.pop();
    }
}
