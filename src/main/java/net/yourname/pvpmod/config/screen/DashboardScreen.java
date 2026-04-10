package net.yourname.pvpmod.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.yourname.pvpmod.config.ConfigManager;
import net.yourname.pvpmod.config.IndicatorStyle;
import net.yourname.pvpmod.config.ModConfig;

public class DashboardScreen extends Screen {
    private static final Identifier BACKGROUND = 
        Identifier.of("pvpmod", "textures/gui/dashboard_bg.png");
    
    private final Screen parent;
    private ModConfig cfg;
    
    // UI Elements
    private CyclingButtonWidget<IndicatorStyle> styleButton;
    private SliderWidget widthSlider, heightSlider, distanceSlider, animSlider;
    private ButtonWidget colorFullBtn, colorHalfBtn, colorLowBtn;
    private ButtonWidget saveBtn, resetBtn, closeBtn;
    
    public DashboardScreen(Screen parent) {
        super(Text.literal("PvP Dashboard"));
        this.parent = parent;
        this.cfg = ConfigManager.get();
    }
    
    @Override
    protected void init() {
        super.init();
        cfg = ConfigManager.get(); // Reload in case changed
        
        int centerX = width / 2;
        int startY = 30;
        int rowHeight = 25;
        int labelWidth = 120;
        
        // ===== HEADER =====
        addDrawableChild(ButtonWidget.builder(Text.literal("✦ PvP INDICATOR DASHBOARD ✦")
                .formatted(Formatting.GOLD, Formatting.BOLD), 
                b -> {}).dimensions(centerX - 150, 5, 300, 20).build());
        
        // ===== STYLE SELECTOR =====
        addDrawableChild(Text.literal("Indicator Style:").asOrderedText(), 
                        centerX - labelWidth - 10, startY + 5, 0xFFFFFF);
        styleButton = CyclingButtonWidget.builder(IndicatorStyle::getText)
            .values(IndicatorStyle.values())
            .initialValue(cfg.style)
            .displayOnlyValue()
            .onPress((btn, style) -> {
                cfg.style = style;
                ConfigManager.save();
            })
            .dimensions(centerX - 80, startY, 160, 20)
            .build();
        addDrawableChild(styleButton);
        
        // ===== SIZE SLIDERS =====
        startY += rowHeight + 5;
        addDrawableChild(Text.literal("Bar Width: " + cfg.barWidth).asOrderedText(),
                        centerX - labelWidth - 10, startY + 5, 0xDDDDDD);
        widthSlider = new SliderWidget(centerX - 80, startY, 160, 20, 
            Text.empty(), (cfg.barWidth - 20) / 180.0) {
            @Override
            protected void updateMessage() {
                setMessage(Text.literal(String.valueOf((int)(getValue() * 180) + 20)));
            }
            @Override
            protected void applyValue() {
                cfg.barWidth = (int)(getValue() * 180) + 20;
                ConfigManager.save();
            }
        };
        addDrawableChild(widthSlider);
        
        startY += rowHeight + 3;
        addDrawableChild(Text.literal("Bar Height: " + cfg.barHeight).asOrderedText(),
                        centerX - labelWidth - 10, startY + 5, 0xDDDDDD);
        heightSlider = new SliderWidget(centerX - 80, startY, 160, 20,
            Text.empty(), (cfg.barHeight - 2) / 8.0) {
            @Override protected void updateMessage() {
                setMessage(Text.literal(String.valueOf((int)(getValue() * 8) + 2)));
            }
            @Override protected void applyValue() {
                cfg.barHeight = (int)(getValue() * 8) + 2;
                ConfigManager.save();
            }
        };
        addDrawableChild(heightSlider);
        
        // ===== ANIMATION SPEED =====
        startY += rowHeight + 3;
        addDrawableChild(Text.literal("Animation: " + (int)(cfg.animationSpeed*100) + "%").asOrderedText(),
                        centerX - labelWidth - 10, startY + 5, 0xDDDDDD);
        animSlider = new SliderWidget(centerX - 80, startY, 160, 20,
            Text.empty(), cfg.animationSpeed) {
            @Override protected void updateMessage() {
                setMessage(Text.literal((int)(getValue()*100) + "%"));
            }
            @Override protected void applyValue() {
                cfg.animationSpeed = (float) getValue();
                ConfigManager.save();
            }
        };
        addDrawableChild(animSlider);
        
        // ===== TOGGLES =====
        startY += rowHeight + 8;
        addDrawableChild(CyclingButtonWidget.onOffBuilder()
            .initialValue(cfg.smoothAnimation)
            .onPress((b, val) -> { cfg.smoothAnimation = val; ConfigManager.save(); })
            .dimensions(centerX - 100, startY, 200, 20)
            .build(Text.literal("Smooth Animation")));
        
        startY += rowHeight + 3;
        addDrawableChild(CyclingButtonWidget.onOffBuilder()
            .initialValue(cfg.showHealthText)
            .onPress((b, val) -> { cfg.showHealthText = val; ConfigManager.save(); })
            .dimensions(centerX - 100, startY, 200, 20)
            .build(Text.literal("Show Health Text")));
        
        // ===== COLOR PICKERS (Simple version) =====
        startY += rowHeight + 10;
        addDrawableChild(Text.literal("Colors:").asOrderedText(), 
                        centerX - labelWidth - 10, startY + 5, 0xFFFFFF);
        
        colorFullBtn = ButtonWidget.builder(Text.literal("● Full").setStyle(
                Style.EMPTY.withColor(cfg.colorFull | 0xFF000000)), 
                b -> cycleColor("full")).dimensions(centerX - 100, startY, 60, 20).build();
        addDrawableChild(colorFullBtn);
        
        colorHalfBtn = ButtonWidget.builder(Text.literal("● Half").setStyle(
                Style.EMPTY.withColor(cfg.colorHalf | 0xFF000000)),
                b -> cycleColor("half")).dimensions(centerX - 35, startY, 60, 20).build();
        addDrawableChild(colorHalfBtn);
        
        colorLowBtn = ButtonWidget.builder(Text.literal("● Low").setStyle(
                Style.EMPTY.withColor(cfg.colorLow | 0xFF000000)),
                b -> cycleColor("low")).dimensions(centerX + 30, startY, 60, 20).build();
        addDrawableChild(colorLowBtn);
        
        // ===== ACTION BUTTONS =====
        startY = height - 50;
        saveBtn = ButtonWidget.builder(Text.literal("💾 Save & Apply")
                .formatted(Formatting.GREEN, Formatting.BOLD),
                b -> { cfg.validate(); ConfigManager.save(); close(); })
            .dimensions(centerX - 150, startY, 140, 25).build();
        addDrawableChild(saveBtn);
        
        resetBtn = ButtonWidget.builder(Text.literal("↺ Reset Defaults")
                .formatted(Formatting.YELLOW),
                b -> { cfg = new ModConfig(); cfg.validate(); 
                       ConfigManager.save(); init(); })
            .dimensions(centerX - 10, startY, 140, 25).build();
        addDrawableChild(resetBtn);
        
        closeBtn = ButtonWidget.builder(Text.literal("✕ Close")
                .formatted(Formatting.RED),
                b -> close())
            .dimensions(centerX + 130, startY, 140, 25).build();
        addDrawableChild(closeBtn);
    }
    
    private void cycleColor(String type) {
        int[] colors = {0x00CC00, 0x00FF00, 0xCCFF00, 0xFFCC00, 0xFF6600, 0xCC0000, 0xFF0066, 0xCC00CC, 0x6600FF, 0x0099FF};
        int current = switch(type) {
            case "full" -> cfg.colorFull;
            case "half" -> cfg.colorHalf;
            case "low" -> cfg.colorLow;
            default -> cfg.colorFull;
        };
        int idx = java.util.Arrays.asList(colors).indexOf(current);
        int next = colors[(idx + 1) % colors.length];
        switch(type) {
            case "full" -> cfg.colorFull = next;
            case "half" -> cfg.colorHalf = next;
            case "low" -> cfg.colorLow = next;
        }
        ConfigManager.save();
        init(); // Refresh UI
    }
    
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Background gradient
        renderBackground(ctx, mouseX, mouseY, delta);
        
        // Title
        ctx.drawCenteredTextWithShadow(textRenderer, title, width/2, 10, 0xFFFFFF);
        
        // Subtitle
        ctx.drawCenteredTextWithShadow(textRenderer, 
            Text.literal("Customize your opponent health indicators").formatted(Formatting.GRAY),
            width/2, 22, 0xAAAAAA);
        
        // Render all widgets
        super.render(ctx, mouseX, mouseY, delta);
        
        // Preview box (bottom-right)
        renderPreview(ctx);
    }
    
    private void renderPreview(DrawContext ctx) {
        int px = width - 120, py = height - 90;
        
        // Preview background
        ctx.fill(px - 5, py - 5, px + 115, py + 55, 0x80101010);
        ctx.drawHorizontalLine(px - 5, px + 115, py - 5, cfg.accentColor | 0xFF000000);
        
        ctx.drawText(textRenderer, Text.literal("Preview").formatted(Formatting.BOLD), 
                    px + 5, py + 2, 0xFFFFFF, false);
        
        // Draw sample indicator based on current style
        float sampleHealth = 0.65f; // 65% health for preview
        switch(cfg.style) {
            case VANILLA_HEARTS -> renderVanillaPreview(ctx, px + 5, py + 20, sampleHealth);
            case STATUS_BAR -> renderBarPreview(ctx, px + 5, py + 20, sampleHealth);
            case SPARK_HEAD -> renderSparkPreview(ctx, px + 5, py + 20, sampleHealth);
        }
    }
    
    private void renderBarPreview(DrawContext ctx, int x, int y, float percent) {
        int w = cfg.barWidth, h = cfg.barHeight;
        ctx.fill(x, y, x + w, y + h, cfg.backgroundColor | 0x90000000);
        int color = percent > 0.6f ? cfg.colorFull : percent > 0.3f ? cfg.colorHalf : cfg.colorLow;
        ctx.fill(x, y, x + (int)(w * percent), y + h, color | 0xFF000000);
        ctx.drawHorizontalLine(x - 1, x + w + 1, y - 1, cfg.borderColor | 0xFF000000);
        ctx.drawHorizontalLine(x - 1, x + w + 1, y + h, cfg.borderColor | 0xFF000000);
        ctx.drawVerticalLine(x - 1, y - 1, y + h, cfg.borderColor | 0xFF000000);
        ctx.drawVerticalLine(x + w + 1, y - 1, y + h, cfg.borderColor | 0xFF000000);
        if (cfg.showHealthText) {
            String hp = String.valueOf((int)(percent * 20));
            ctx.drawText(textRenderer, hp, x + w/2 - textRenderer.getWidth(hp)/2, y - 10, 0xFFFFFF, true);
        }
    }
    
    private void renderVanillaPreview(DrawContext ctx, int x, int y, float percent) {
        // Simplified hearts preview
        int hearts = (int)(percent * 10);
        for (int i = 0; i < 10; i++) {
            int hx = x + i * 9;
            ctx.drawText(textRenderer, i < hearts ? "❤" : "🖤", hx, y, 
                        i < hearts ? 0xFF5555 : 0x404040, false);
        }
    }
    
    private void renderSparkPreview(DrawContext ctx, int x, int y, float percent) {
        // Head placeholder + sparks
        ctx.fill(x, y, x + 12, y + 12, 0xFF888888); // Head box
        int sparks = (int)(percent * 10);
        for (int i = 0; i < 10; i++) {
            int sx = x + 16 + i * (cfg.sparkSize + cfg.sparkGap);
            ctx.fill(sx, y + 2, sx + cfg.sparkSize, y + 2 + cfg.sparkSize,
                    i < sparks ? 0xFFFFAA00 : 0x303030);
            if (i < sparks) {
                // Spark shine
                ctx.fill(sx + 1, y + 3, sx + 3, y + 5, 0xFFFFFFFF);
            }
        }
    }
    
    @Override
    public boolean shouldCloseOnEsc() { return false; }
    
    private void close() {
        assert client != null;
        client.setScreen(parent);
    }
}
