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
public class TitleScreenMixin extends Screen {
    
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addSkinButton(CallbackInfo ci) {
        int buttonWidth = 100;
        int buttonHeight = 20;
        int x = this.width / 2 + 104;
        int y = this.height / 4 + 48 + 72;
        
        // Main Skin Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a✦ Change Skin"),
            button -> {
                try {
                    MinecraftClient.getInstance().setScreen(
                        new SkinDashboardScreen(this)
                    );
                } catch (Exception e) {
                    System.err.println("Failed to open skin dashboard: " + e);
                }
            }
        )
        .dimensions(x, y, buttonWidth, buttonHeight)
        .build());
        
        // Quick Reset Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§c⟳ Reset Skin"),
            button -> {
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.setSkinTextures(null);
                    MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§aSkin reset to default!"), 
                        false
                    );
                }
            }
        )
        .dimensions(x, y + 24, buttonWidth, buttonHeight)
        .build());
    }
}
