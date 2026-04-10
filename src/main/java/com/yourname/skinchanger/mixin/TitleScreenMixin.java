package com.yourname.skinchanger.mixin;

import com.yourname.skinchanger.screen.SkinSelectScreen;
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

    @Inject(method = "init", at = @At("RETURN"))
    private void addSkinButton(CallbackInfo ci) {
        int y = this.height / 4 + 48;
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Change Skin"), 
            button -> {
                if (this.client != null) {
                    this.client.setScreen(new SkinSelectScreen(this));
                }
            })
            .dimensions(this.width / 2 - 100, y + 72, 200, 20)
            .build());
    }
}
