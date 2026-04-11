package net.yourname.pvpmod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.yourname.pvpmod.screen.DashboardScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class KeyboardMixin {
    
    @Inject(method = "handleInputEvents", at = @At("HEAD"), cancellable = true)
    private void onHandleInput(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient mc = (MinecraftClient) (Object) this;
        if (mc.currentScreen == null && mc.world != null) {
            // F7 to toggle dashboard
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_F7)) {
                mc.setScreen(new DashboardScreen(null));
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }
}
