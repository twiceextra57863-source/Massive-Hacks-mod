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
        int buttonWidth = 120;
        int buttonHeight = 24;
        
        // Right side buttons positioning
        int x = this.width - buttonWidth - 24;
        int y = this.height / 4 + 48;
        
        // Main Skin Button with Minecraft style
        ButtonWidget skinButton = ButtonWidget.builder(
            Text.literal("§6§l✦ SKIN STUDIO"),
            button -> {
                MinecraftClient.getInstance().setScreen(
                    new SkinDashboardScreen(this)
                );
            }
        )
        .dimensions(x, y, buttonWidth, buttonHeight)
        .build();
        
        // Quick Reset Button
        ButtonWidget resetButton = ButtonWidget.builder(
            Text.literal("§c§l⟳ RESET"),
            button -> {
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("§a✓ Skin reset to default!"), 
                        true
                    );
                }
            }
        )
        .dimensions(x, y + 28, buttonWidth, buttonHeight)
        .build();
        
        this.addDrawableChild(skinButton);
        this.addDrawableChild(resetButton);
    }
}
