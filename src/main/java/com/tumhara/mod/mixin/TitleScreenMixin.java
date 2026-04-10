package com.tumhara.mod.mixin;

import com.tumhara.mod.client.gui.SkinDashboardScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addSkinButton(CallbackInfo ci) {
        int buttonWidth = 100;
        int buttonHeight = 20;
        int x = this.width / 2 + 104;
        int y = this.height / 4 + 48 + 72;
        
        // Change Skin Button
        ButtonWidget skinButton = ButtonWidget.builder(
            Text.literal("§a✦ Change Skin"),
            button -> {
                MinecraftClient.getInstance().setScreen(
                    new SkinDashboardScreen(this)
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
                        Text.literal("§aSkin reset requested!"), 
                        false
                    );
                }
            }
        )
        .dimensions(x, y + 24, buttonWidth, buttonHeight)
        .build();
        
        // Now 'this' is a Screen, so addDrawableChild is accessible
        this.addDrawableChild(skinButton);
        this.addDrawableChild(resetButton);
    }
}
