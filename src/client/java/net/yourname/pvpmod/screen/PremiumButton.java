package net.yourname.pvpmod.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import java.util.function.Consumer; // ✅ Use this instead of OnPress

public class PremiumButton extends ButtonWidget {
    
    private final int baseColor, hoverColor, pressColor;
    private final Consumer<PremiumButton> onPress;
    private float scale = 1.0f;
    
    private PremiumButton(int x, int y, int width, int height, Text message, 
                         Consumer<PremiumButton> onPress, int baseColor, int hoverColor, int pressColor) {
        super(x, y, width, height, message, btn -> onPress.accept((PremiumButton) btn), DEFAULT_NARRATION_SUPPLIER);
        this.onPress = onPress;
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;
        this.pressColor = pressColor;
    }
    
    public static Builder builder(Text message, Consumer<PremiumButton> onPress, 
                                  int baseColor, int hoverColor, int pressColor) {
        return new Builder(message, onPress, baseColor, hoverColor, pressColor);
    }
    
    @Override
    public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        
        // Hover detection
        boolean hovered = mouseX >= getX() && mouseX < getX() + width && 
                         mouseY >= getY() && mouseY < getY() + height;
        
        // Smooth scale animation
        float targetScale = hovered ? 1.05f : 1.0f;
        scale = MathHelper.lerp(delta * 0.15f, scale, targetScale);
        
        int renderX = getX() + (int) ((width - width * scale) / 2);
        int renderY = getY() + (int) ((height - height * scale) / 2);
        int renderW = (int) (width * scale);
        int renderH = (int) (height * scale);
        
        // Glass background
        int color = isPressed() ? pressColor : (hovered ? hoverColor : baseColor);
        ctx.fill(renderX - 2, renderY - 2, renderX + renderW + 2, renderY + renderH + 2, 0x40000000);
        ctx.fill(renderX - 1, renderY - 1, renderX + renderW + 1, renderY + renderH + 1, 0x80000000);
        ctx.fill(renderX, renderY, renderX + renderW, renderY + renderH, color | 0x60000000);
        
        // Border glow
        ctx.drawHorizontalLine(renderX - 1, renderX + renderW, renderY - 1, 0x80FFFFFF);
        ctx.drawHorizontalLine(renderX - 1, renderX + renderW, renderY + renderH, 0x80FFFFFF);
        ctx.drawVerticalLine(renderX - 1, renderY - 1, renderY + renderH, 0x80FFFFFF);
        ctx.drawVerticalLine(renderX + renderW, renderY - 1, renderY + renderH, 0x80FFFFFF);
        
        // Text
        ctx.drawCenteredTextWithShadow(mc.textRenderer, getMessage(), 
            renderX + renderW / 2, renderY + (renderH - 8) / 2, 0xFFFFFF);
    }
    
    public static class Builder {
        private final Text message;
        private final Consumer<PremiumButton> onPress;
        private final int baseColor, hoverColor, pressColor;
        private int x, y, width = 200, height = 20;
        
        Builder(Text message, Consumer<PremiumButton> onPress, int baseColor, int hoverColor, int pressColor) {
            this.message = message;
            this.onPress = onPress;
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.pressColor = pressColor;
        }
        
        public Builder dimensions(int x, int y, int width, int height) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            return this;
        }
        
        public PremiumButton build() {
            return new PremiumButton(x, y, width, height, message, onPress, baseColor, hoverColor, pressColor);
        }
    }
}
