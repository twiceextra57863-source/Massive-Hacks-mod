package com.tumhara.mod.mixin;

import com.tumhara.mod.client.gui.SkinDashboardScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuMixin extends Screen {

    protected GameMenuMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void addSkinButtonToPauseMenu(CallbackInfo ci) {
        // 'this' is a Screen object because we extended Screen
        int yPosition = this.height / 4 + 48 + 24;
        int xPosition = this.width / 2 - 100;
        
        ButtonWidget skinButton = ButtonWidget.builder(
            Text.literal("§6§l✨ SKIN STUDIO"),
            button -> {
                MinecraftClient.getInstance().setScreen(
                    new SkinDashboardScreen(this)
                );
            }
        )
        .dimensions(xPosition, yPosition, 200, 20)
        .build();
        
        // Use 'this' instead of screen variable
        this.addDrawableChild(skinButton);
    }
}
