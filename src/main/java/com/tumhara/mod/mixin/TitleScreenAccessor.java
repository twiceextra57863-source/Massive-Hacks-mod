package com.tumhara.mod.mixin;

import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.client.gui.screen.Screen.class)
public interface TitleScreenAccessor {
    @Invoker("addDrawableChild")
    <T extends ClickableWidget> T invokeAddDrawableChild(T widget);
}
