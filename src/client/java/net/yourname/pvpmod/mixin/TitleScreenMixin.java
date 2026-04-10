package net.yourname.pvpmod.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.yourname.pvpmod.screen.DashboardScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    
    @Inject(method = "init", at = @At("TAIL"))
    private void addDashboardButton(CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;
        
        // ✅ Use screen.addDrawableChild properly
        screen.addDrawableChild(ButtonWidget.builder(
                Text.literal("⚔️ PvP Dashboard").formatted(Formatting.GOLD),
                btn -> {
                    if (screen.client != null) {
                        screen.client.setScreen(new DashboardScreen(screen));
                    }
                })
            .dimensions(screen.width / 2 - 100, screen.height / 4 + 72, 200, 20)
            .build());
    }
}
