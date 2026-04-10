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
    private void addSkinButtonToTitleScreen(CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;
        
        // Button ko top-right corner pe rakhna
        ButtonWidget skinButton = ButtonWidget.builder(
            Text.literal("§6§l✦ SKIN"),
            button -> {
                MinecraftClient.getInstance().setScreen(
                    new SkinDashboardScreen(screen)
                );
            }
        )
        .dimensions(screen.width - 65, 10, 60, 22)
        .build();
        
        screen.addDrawableChild(skinButton);
    }
}
