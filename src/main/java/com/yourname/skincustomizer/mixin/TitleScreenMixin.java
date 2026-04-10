package com.yourname.skincustomizer.mixin;

import com.yourname.skincustomizer.screen.SkinDashboardScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void addDashboardButton(CallbackInfo ci) {
        int btnY = this.height / 2 + 48 + 12;
        this.addRenderableWidget(Button.builder(
                Component.literal("🎨 Skin Dashboard"),
                btn -> this.minecraft.setScreen(new SkinDashboardScreen(this))
        ).bounds(this.width / 2 - 100, btnY, 200, 20).build());
    }
}
