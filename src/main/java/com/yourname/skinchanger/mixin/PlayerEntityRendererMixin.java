package com.yourname.skinchanger.mixin;

import com.yourname.skinchanger.SkinChangerClient;
import com.yourname.skinchanger.config.SkinChangerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    
    @Inject(method = "getTexture(Lnet/minecraft/client/network/AbstractClientPlayerEntity;)Lnet/minecraft/util/Identifier;", 
            at = @At("HEAD"), cancellable = true)
    private void getCustomTexture(AbstractClientPlayerEntity player, CallbackInfoReturnable<Identifier> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Only apply custom skin to local player
        if (client.player != null && player.getUuid().equals(client.player.getUuid())) {
            String currentSkin = SkinChangerConfig.getCurrentSkin();
            if (currentSkin != null && !currentSkin.isEmpty()) {
                Identifier customSkin = SkinChangerClient.getCustomSkin();
                if (client.getTextureManager().getOrDefault(customSkin, null) != null) {
                    cir.setReturnValue(customSkin);
                }
            }
        }
    }
}
