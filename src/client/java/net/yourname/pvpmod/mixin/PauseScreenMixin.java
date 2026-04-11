package net.yourname.pvpmod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.yourname.pvpmod.screen.DashboardScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class PauseScreenMixin {
    
    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        GameMenuScreen screen = (GameMenuScreen) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        
        screen.addDrawableChild(PremiumButton.builder(
                Text.literal("⚙️ PvP Settings").formatted(Formatting.AQUA, Formatting.BOLD),
                btn -> mc.setScreen(new DashboardScreen(screen)),
                0xFF4040FF, 0xFF6060FF, 0xFF202080)
            .dimensions(screen.width / 2 - 100, screen.height / 4 + 120, 200, 24)
            .build());
    }
}
