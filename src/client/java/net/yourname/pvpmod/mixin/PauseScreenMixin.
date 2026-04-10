package net.yourname.pvpmod.mixin;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.yourname.pvpmod.screen.DashboardScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class PauseScreenMixin {
    
    @Inject(method = "init", at = @At("TAIL"))
    private void addDashboardButton(CallbackInfo ci) {
        GameMenuScreen screen = (GameMenuScreen) (Object) this;
        assert screen.client != null;
        
        // Add button in pause menu
        screen.addDrawableChild(ButtonWidget.builder(
                Text.literal("⚙️ PvP Settings"),
                btn -> screen.client.setScreen(new DashboardScreen(screen)))
            .dimensions(screen.width / 2 - 100, screen.height / 4 + 120, 200, 20)
            .build());
    }
}
