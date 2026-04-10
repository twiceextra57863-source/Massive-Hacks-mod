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
        
        screen.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a✦ Change Skin"),
            button -> {
                try {
                    MinecraftClient.getInstance().setScreen(
                        new SkinDashboardScreen(screen)
                    );
                } catch (Exception e) {
                    System.err.println("Failed to open skin dashboard: " + e);
                }
            }
        )
        .dimensions(x, y, buttonWidth, buttonHeight)
        .build());
        
        screen.addDrawableChild(ButtonWidget.builder(
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
        .build());
    }
}
