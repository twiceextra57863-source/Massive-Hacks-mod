package com.tumhara.mod.mixin;

import com.tumhara.mod.client.gui.SkinDashboardScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void addSkinButton(CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;
        int buttonWidth = 100;
        int buttonHeight = 20;
        int x = screen.width / 2 + 104;
        int y = screen.height / 4 + 48 + 72;
        
        // Change Skin Button
        ButtonWidget skinButton = ButtonWidget.builder(
            Text.literal("§a✦ Change Skin"),
            button -> {
                MinecraftClient.getInstance().setScreen(
                    new SkinDashboardScreen(screen)
                );
            }
        )
        .dimensions(x, y, buttonWidth, buttonHeight)
        .build();
        
        // Reset Skin Button
        ButtonWidget resetButton = ButtonWidget.builder(
            Text.literal("§c⟳ Reset Skin"),
            button -> {
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§aFeature coming soon!"), 
                        false
                    );
                }
            }
        )
        .dimensions(x, y + 24, buttonWidth, buttonHeight)
        .build();
        
        // addDrawableChild is PUBLIC in Screen class - direct access
        screen.addDrawableChild(skinButton);
        screen.addDrawableChild(resetButton);
    }
}
