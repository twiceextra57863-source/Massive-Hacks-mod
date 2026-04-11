package net.yourname.pvpmod.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.yourname.pvpmod.config.ConfigManager;
import net.yourname.pvpmod.config.IndicatorStyle;
import net.yourname.pvpmod.config.ModConfig;

import java.util.*;
import java.util.function.Consumer;

public class DashboardScreen extends Screen {
    
    // ===== UI CONSTANTS =====
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 380;
    private static final int TAB_HEIGHT = 28;
    private static final int CONTROL_WIDTH = 180;
    private static final int ROW_HEIGHT = 26;
    
    // ===== GLASS EFFECT COLORS =====
    private static final int GLASS_BG = 0x401A1A2E;
    private static final int GLASS_BORDER = 0x804040FF;
    
    // ===== PARTICLE SYSTEM =====
    private final List<Particle> particles = new ArrayList<>();
    private long particleTimer = 0;
    
    // ===== TAB SYSTEM =====
    private enum Tab { GENERAL, APPEARANCE, ADVANCED, PRESETS }
    private Tab currentTab = Tab.GENERAL;
    
    // ===== UI ELEMENTS =====
    private CyclingButtonWidget<IndicatorStyle> styleButton;
    private CyclingButtonWidget<Boolean> enabledButton, animButton, textButton;
    private List<SimpleSlider> sliders = new ArrayList<>();
    private List<ColorPicker> colorPickers = new ArrayList<>();
    private List<TabButton> tabButtons = new ArrayList<>();
    private TextFieldWidget hexColorInput;
    
    // ===== PREVIEW STATE =====
    private float previewHealth = 0.65f;
    private long previewAnimTime = 0;
    
    // ===== CONFIG =====
    private final Screen parent;
    private ModConfig cfg;
    
    public DashboardScreen(Screen parent) {
        super(Text.literal(""));
        this.parent = parent;
        this.cfg = ConfigManager.get();
        initParticles();
    }
    
    // ===== PARTICLE SYSTEM =====
    private void initParticles() {
        Random rand = new Random();
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(
                rand.nextFloat() * 800, rand.nextFloat() * 600,
                (rand.nextFloat() - 0.5f) * 0.5f,
                (rand.nextFloat() - 0.5f) * 0.5f,
                rand.nextInt(3) + 1,
                ColorHelper.fromRgb(rand.nextInt(0x40) + 0x40, rand.nextInt(0x40) + 0x40, rand.nextInt(0x80) + 0x80)
            ));
        }
    }
    
    private void updateParticles(float delta) {
        particleTimer += delta * 16;
        for (Particle p : particles) {
            p.x += p.vx * delta;
            p.y += p.vy * delta;
            p.life -= delta * 0.5f;
            if (p.life <= 0 || p.x < -10 || p.x > width + 10 || p.y < -10 || p.y > height + 10) {
                Random rand = new Random();
                p.x = rand.nextFloat() * width;
                p.y = height + 10;
                p.vx = (rand.nextFloat() - 0.5f) * 0.5f;
                p.vy = -rand.nextFloat() * 0.3f - 0.2f;
                p.life = rand.nextFloat() * 100 + 50;
                p.size = rand.nextInt(3) + 1;
                p.color = ColorHelper.fromRgb(rand.nextInt(0x40) + 0x40, rand.nextInt(0x40) + 0x40, rand.nextInt(0x80) + 0x80);
            }
        }
    }
    
    private void renderParticles(DrawContext ctx, float delta) {
        for (Particle p : particles) {
            float alpha = Math.min(1.0f, p.life / 30f);
            ctx.fill((int)p.x, (int)p.y, (int)p.x + p.size, (int)p.y + p.size, 
                    (p.color & 0x00FFFFFF) | ((int)(alpha * 255) << 24));
        }
    }
    
    // ===== TAB BUTTON =====
    private class TabButton extends ButtonWidget {
        private final Tab tab;
        private boolean active = false;
        
        TabButton(int x, int y, int width, Tab tab, Consumer<ButtonWidget> onPress) {
            super(x, y, width, TAB_HEIGHT, Text.literal(tab.name()), onPress, DEFAULT_NARRATION_SUPPLIER);
            this.tab = tab;
        }
        
        void setActive(boolean active) { this.active = active; }
        
        @Override
        public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
            int color = active ? 0xFF4040FF : (isHovered() ? 0xFF303080 : 0xFF202040);
            ctx.fill(getX(), getY(), getX() + width, getY() + height, color | 0x60000000);
            if (active) ctx.drawHorizontalLine(getX(), getX() + width, getY() + height - 1, 0xFF6060FF);
            
            Formatting fmt = active ? Formatting.WHITE : Formatting.GRAY;
            ctx.drawCenteredTextWithShadow(textRenderer, 
                Text.literal(tab.name().charAt(0) + tab.name().substring(1).toLowerCase()).formatted(fmt),
                getX() + width/2, getY() + (height - 8)/2, 0xFFFFFF);
        }
    }
    
    // ===== COLOR PICKER =====
    private class ColorPicker extends ButtonWidget {
        private int color;
        private final String label;
        private final Consumer<Integer> onColorChange;
        
        ColorPicker(int x, int y, int width, int height, String label, int color, Consumer<Integer> onColorChange) {
            super(x, y, width, height, Text.literal(label), btn -> {}, DEFAULT_NARRATION_SUPPLIER);
            this.color = color;
            this.label = label;
            this.onColorChange = onColorChange;
        }
        
        void setColor(int color) { this.color = color; }
        int getColor() { return color; }
        
        @Override
        public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
            ctx.fill(getX(), getY(), getX() + 24, getY() + height, color | 0xFF000000);
            ctx.drawHorizontalLine(getX(), getX() + 24, getY(), 0xFFFFFFFF);
            ctx.drawVerticalLine(getX(), getY(), getY() + height, 0xFFFFFFFF);
            ctx.drawText(textRenderer, label, getX() + 30, getY() + (height - 8)/2, 0xFFFFFF, false);
            if (isHovered()) ctx.fill(getX() - 2, getY() - 2, getX() + width + 2, getY() + height + 2, 0x20FFFFFF);
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isHovered() && button == 0) {
                int[] palette = {0x00CC00, 0x00FF00, 0xCCFF00, 0xFFCC00, 0xFF6600, 0xCC0000, 0xFF0066, 0xCC00CC, 0x6600FF, 0x0099FF};
                int idx = Arrays.asList(palette).indexOf(color);
                color = palette[(idx + 1) % palette.length];
                onColorChange.accept(color);
                ConfigManager.save();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
    
    // ===== SIMPLE SLIDER (No AbstractSliderWidget) =====
    private static class SimpleSlider implements Element, Selectable {
        final int x, y, width, height;
        final String label;
        double value;
        final Consumer<Double> onChange;
        boolean dragging = false;
        
        SimpleSlider(int x, int y, int width, int height, String label, double value, Consumer<Double> onChange) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.label = label; this.value = value; this.onChange = onChange;
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
                dragging = true;
                updateValue(mouseX);
                return true;
            }
            return false;
        }
        
        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (button == 0) dragging = false;
            return false;
        }
        
        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (dragging && button == 0) { updateValue(mouseX); return true; }
            return false;
        }
        
        private void updateValue(double mx) {
            value = Math.max(0, Math.min(1, (mx - x) / (double)width));
            onChange.accept(value);
        }
        
        void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
            boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
            ctx.fill(x, y + height/2 - 2, x + width, y + height/2 + 2, 0xFF303050);
            int fillW = (int)(value * width);
            ctx.fill(x, y + height/2 - 2, x + fillW, y + height/2 + 2, 0xFF6060FF);
            int thumbX = x + fillW;
            ctx.fill(thumbX - 6, y + 2, thumbX + 6, y + height - 2, 0xFF4040FF);
            if (hovered) ctx.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0x20FFFFFF);
            ctx.drawCenteredTextWithShadow(ctx.getTextRenderer(), label + ": " + (int)(value * 100) + "%", 
                x + width/2, y + (height - 8)/2, 0xFFFFFF);
        }
        
        @Override public boolean isMouseOver(double mouseX, double mouseY) { return false; }
        @Override public SelectionType getType() { return SelectionType.NONE; }
        @Override public boolean isFocused() { return false; }
        @Override public void setFocused(boolean focused) {}
        @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
        @Override public boolean keyReleased(int keyCode, int scanCode, int modifiers) { return false; }
        @Override public boolean charTyped(char chr, int modifiers) { return false; }
    }
    
    // ===== PARTICLE CLASS =====
    private static class Particle {
        float x, y, vx, vy, life, size;
        int color;
        Particle(float x, float y, float vx, float vy, float size, int color) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.size = size; this.color = color; this.life = 100;
        }
    }
    
    // ===== INIT =====
    @Override
    protected void init() {
        super.init();
        cfg = ConfigManager.get();
        sliders.clear();
        colorPickers.clear();
        tabButtons.clear();
        
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        
        // ===== TABS =====
        int tabX = panelX + 10;
        tabButtons.add(addDrawableChild(new TabButton(tabX, panelY + 8, 90, Tab.GENERAL, t -> switchTab(Tab.GENERAL))));
        tabButtons.add(addDrawableChild(new TabButton(tabX + 95, panelY + 8, 90, Tab.APPEARANCE, t -> switchTab(Tab.APPEARANCE))));
        tabButtons.add(addDrawableChild(new TabButton(tabX + 190, panelY + 8, 90, Tab.ADVANCED, t -> switchTab(Tab.ADVANCED))));
        tabButtons.add(addDrawableChild(new TabButton(tabX + 285, panelY + 8, 90, Tab.PRESETS, t -> switchTab(Tab.PRESETS))));
        updateTabButtons();
        
        int contentX = panelX + 20;
        int contentY = panelY + 45;
        
        switch (currentTab) {
            case GENERAL -> initGeneralTab(contentX, contentY);
            case APPEARANCE -> initAppearanceTab(contentX, contentY);
            case ADVANCED -> initAdvancedTab(contentX, contentY);
            case PRESETS -> initPresetsTab(contentX, contentY);
        }
        
        // ===== ACTION BUTTONS =====
        int btnY = panelY + PANEL_HEIGHT - 35;
        addDrawableChild(PremiumButton.builder(
                Text.literal("💾 Save").formatted(Formatting.GREEN, Formatting.BOLD),
                b -> { cfg.validate(); ConfigManager.save(); },
                0xFF208020, 0xFF30A030, 0xFF106010)
            .dimensions(panelX + 20, btnY, 80, 24).build());
        
        addDrawableChild(PremiumButton.builder(
                Text.literal("↺ Reset").formatted(Formatting.YELLOW),
                b -> { cfg = new ModConfig(); cfg.validate(); ConfigManager.save(); init(); },
                0xFF808020, 0xFFA0A030, 0xFF606010)
            .dimensions(panelX + 110, btnY, 80, 24).build());
        
        addDrawableChild(PremiumButton.builder(
                Text.literal("✕ Close").formatted(Formatting.RED, Formatting.BOLD),
                b -> close(),
                0xFF802020, 0xFFA03030, 0xFF601010)
            .dimensions(panelX + PANEL_WIDTH - 100, btnY, 80, 24).build());
    }
    
    private void switchTab(Tab tab) {
        currentTab = tab;
        updateTabButtons();
        init();
    }
    
    private void updateTabButtons() {
        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).setActive(i == currentTab.ordinal());
        }
    }
    
    // ===== TAB CONTENT =====
    private void initGeneralTab(int x, int y) {
        enabledButton = addDrawableChild(CyclingButtonWidget.<Boolean>builder(v -> 
                v ? Text.literal("● Enabled").formatted(Formatting.GREEN) : Text.literal("○ Disabled").formatted(Formatting.RED))
            .values(true, false).initialValue(cfg.enabled)
            .onPress((btn, val) -> { cfg.enabled = val; ConfigManager.save(); })
            .dimensions(x, y, CONTROL_WIDTH, 22).build(Text.literal(cfg.enabled ? "Enabled" : "Disabled")));
        y += ROW_HEIGHT;
        
        addDrawableChild(Text.literal("Indicator Style:").asOrderedText(), x, y + 6, 0xDDDDDD);
        styleButton = addDrawableChild(CyclingButtonWidget.<IndicatorStyle>builder(s -> 
                Text.literal(s.getName()).formatted(Formatting.WHITE))
            .values(IndicatorStyle.values()).initialValue(cfg.style)
            .onPress((btn, style) -> { cfg.style = style; ConfigManager.save(); init(); })
            .dimensions(x + 120, y, CONTROL_WIDTH, 22).build(Text.literal(cfg.style.getName())));
        y += ROW_HEIGHT;
        
        animButton = addDrawableChild(CyclingButtonWidget.<Boolean>builder(v ->
                v ? Text.literal("● Smooth").formatted(Formatting.GREEN) : Text.literal("○ Instant").formatted(Formatting.GRAY))
            .values(true, false).initialValue(cfg.smoothAnimation)
            .onPress((btn, val) -> { cfg.smoothAnimation = val; ConfigManager.save(); })
            .dimensions(x, y, CONTROL_WIDTH, 22).build(Text.literal(cfg.smoothAnimation ? "Smooth" : "Instant")));
        y += ROW_HEIGHT;
        
        textButton = addDrawableChild(CyclingButtonWidget.<Boolean>builder(v ->
                v ? Text.literal("● Show HP").formatted(Formatting.GREEN) : Text.literal("○ Hide HP").formatted(Formatting.GRAY))
            .values(true, false).initialValue(cfg.showHealthText)
            .onPress((btn, val) -> { cfg.showHealthText = val; ConfigManager.save(); })
            .dimensions(x, y, CONTROL_WIDTH, 22).build(Text.literal(cfg.showHealthText ? "Show" : "Hide")));
    }
    
    private void initAppearanceTab(int x, int y) {
        addDrawableChild(Text.literal("Bar Width:").asOrderedText(), x, y + 6, 0xDDDDDD);
        sliders.add(new SimpleSlider(x + 120, y, 150, 22, "", (cfg.barWidth - 20) / 180.0, v -> {
            cfg.barWidth = (int)(v * 180) + 20; ConfigManager.save();
        }));
        y += ROW_HEIGHT;
        
        addDrawableChild(Text.literal("Bar Height:").asOrderedText(), x, y + 6, 0xDDDDDD);
        sliders.add(new SimpleSlider(x + 120, y, 150, 22, "", (cfg.barHeight - 2) / 8.0, v -> {
            cfg.barHeight = (int)(v * 8) + 2; ConfigManager.save();
        }));
        y += ROW_HEIGHT;
        
        addDrawableChild(Text.literal("Y Offset:").asOrderedText(), x, y + 6, 0xDDDDDD);
        sliders.add(new SimpleSlider(x + 120, y, 150, 22, "", (cfg.yOffset - 1.0) / 2.0, v -> {
            cfg.yOffset = v * 2.0 + 1.0; ConfigManager.save();
        }));
        y += ROW_HEIGHT;
        
        addDrawableChild(Text.literal("Anim Speed:").asOrderedText(), x, y + 6, 0xDDDDDD);
        sliders.add(new SimpleSlider(x + 120, y, 150, 22, "", cfg.animationSpeed, v -> {
            cfg.animationSpeed = (float)v; ConfigManager.save();
        }));
        y += ROW_HEIGHT + 10;
        
        addDrawableChild(Text.literal("Colors:").formatted(Formatting.WHITE).asOrderedText(), x, y + 6, 0xFFFFFF);
        colorPickers.add(addDrawableChild(new ColorPicker(x, y + 22, 180, 22, "Full Health", cfg.colorFull, c -> { cfg.colorFull = c; ConfigManager.save(); })));
        colorPickers.add(addDrawableChild(new ColorPicker(x, y + 46, 180, 22, "Half Health", cfg.colorHalf, c -> { cfg.colorHalf = c; ConfigManager.save(); })));
        colorPickers.add(addDrawableChild(new ColorPicker(x, y + 70, 180, 22, "Low Health", cfg.colorLow, c -> { cfg.colorLow = c; ConfigManager.save(); })));
    }
    
    private void initAdvancedTab(int x, int y) {
        addDrawableChild(Text.literal("Max Distance:").asOrderedText(), x, y + 6, 0xDDDDDD);
        sliders.add(new SimpleSlider(x + 120, y, 150, 22, "", (cfg.maxRenderDistance - 16) / 48.0, v -> {
            cfg.maxRenderDistance = v * 48 + 16; ConfigManager.save();
        }));
        y += ROW_HEIGHT;
        
        if (cfg.style == IndicatorStyle.SPARK_HEAD) {
            addDrawableChild(Text.literal("Spark Size:").asOrderedText(), x, y + 6, 0xDDDDDD);
            sliders.add(new SimpleSlider(x + 120, y, 150, 22, "", (cfg.sparkSize - 4) / 12.0, v -> {
                cfg.sparkSize = (int)(v * 12) + 4; ConfigManager.save();
            }));
            y += ROW_HEIGHT;
            
            addDrawableChild(CyclingButtonWidget.<Boolean>builder(v -> v ? Text.literal("● Break FX").formatted(Formatting.GREEN) : Text.literal("○ No FX").formatted(Formatting.GRAY))
                .values(true, false).initialValue(cfg.sparkBreakAnimation)
                .onPress((btn, val) -> { cfg.sparkBreakAnimation = val; ConfigManager.save(); })
                .dimensions(x, y, CONTROL_WIDTH, 22).build(Text.literal(cfg.sparkBreakAnimation ? "Enabled" : "Disabled")));
            y += ROW_HEIGHT;
        }
        
        addDrawableChild(CyclingButtonWidget.<Boolean>builder(v -> v ? Text.literal("● Filter Team").formatted(Formatting.GREEN) : Text.literal("○ Show All").formatted(Formatting.GRAY))
            .values(true, false).initialValue(cfg.filterByTeam)
            .onPress((btn, val) -> { cfg.filterByTeam = val; ConfigManager.save(); })
            .dimensions(x, y, CONTROL_WIDTH, 22).build(Text.literal(cfg.filterByTeam ? "Enabled" : "Disabled")));
    }
    
    private void initPresetsTab(int x, int y) {
        String[] presets = {"Default", "Cyberpunk", "Nature", "Minimal", "Custom"};
        int[] presetColors = {0xFF4040FF, 0xFF00FFFF, 0xFF00AA00, 0xFFFFFFFF, 0xFF8040FF};
        for (int i = 0; i < presets.length; i++) {
            String name = presets[i];
            int color = presetColors[i];
            addDrawableChild(PremiumButton.builder(Text.literal(name).formatted(Formatting.WHITE), btn -> applyPreset(name), color - 0x202020, color, color - 0x404040)
                .dimensions(x, y + i * 26, CONTROL_WIDTH, 22).build());
        }
        
        addDrawableChild(Text.literal("Custom Hex:").asOrderedText(), x, y + 150, 0xDDDDDD);
        hexColorInput = new TextFieldWidget(textRenderer, x + 100, y + 148, 100, 20, Text.empty());
        hexColorInput.setMaxLength(7);
        hexColorInput.setText(String.format("#%06X", cfg.colorFull & 0xFFFFFF));
        addDrawableChild(hexColorInput);
    }
    
    private void applyPreset(String name) {
        switch (name) {
            case "Cyberpunk" -> { cfg.colorFull = 0x00FFFF; cfg.colorHalf = 0xFF00FF; cfg.colorLow = 0xFF0066; cfg.accentColor = 0xFF00FF; cfg.backgroundColor = 0x0A0A1A; cfg.borderColor = 0x00FFFF; }
            case "Nature" -> { cfg.colorFull = 0x00CC00; cfg.colorHalf = 0x99CC00; cfg.colorLow = 0xCC6600; cfg.accentColor = 0x00AA00; cfg.backgroundColor = 0x1A2A1A; cfg.borderColor = 0x00FF00; }
            case "Minimal" -> { cfg.colorFull = 0xFFFFFF; cfg.colorHalf = 0xAAAAAA; cfg.colorLow = 0x666666; cfg.accentColor = 0xFFFFFF; cfg.backgroundColor = 0x000000; cfg.borderColor = 0x404040; }
            case "Custom" -> {}
            default -> { cfg.colorFull = 0x00CC00; cfg.colorHalf = 0xCCCC00; cfg.colorLow = 0xCC0000; cfg.accentColor = 0x4040FF; cfg.backgroundColor = 0x000000; cfg.borderColor = 0xFFFFFF; }
        }
        ConfigManager.save();
        init();
    }
    
    // ===== RENDER =====
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        updateParticles(delta);
        renderAnimatedBackground(ctx, delta);
        renderParticles(ctx, delta);
        ctx.fill(0, 0, width, height, 0x50000000);
        
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        
        // Glass panel
        ctx.fill(panelX - 3, panelY - 3, panelX + PANEL_WIDTH + 3, panelY + PANEL_HEIGHT + 3, 0x60000000);
        ctx.fill(panelX - 2, panelY - 2, panelX + PANEL_WIDTH + 2, panelY + PANEL_HEIGHT + 2, 0x40000000);
        ctx.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelY + PANEL_HEIGHT + 1, 0x20FFFFFF);
        ctx.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, GLASS_BG);
        
        // Animated border
        float glowPulse = (float)(Math.sin(System.currentTimeMillis() * 0.003) * 0.3 + 0.7);
        int glowColor = (int)(glowPulse * 128) << 24 | (GLASS_BORDER & 0x00FFFFFF);
        ctx.drawHorizontalLine(panelX - 2, panelX + PANEL_WIDTH + 2, panelY - 2, glowColor);
        ctx.drawHorizontalLine(panelX - 2, panelX + PANEL_WIDTH + 2, panelY + PANEL_HEIGHT + 2, glowColor);
        ctx.drawVerticalLine(panelX - 2, panelY - 2, panelY + PANEL_HEIGHT + 2, glowColor);
        ctx.drawVerticalLine(panelX + PANEL_WIDTH + 2, panelY - 2, panelY + PANEL_HEIGHT + 2, glowColor);
        
        // Title
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("⚔️ PvP INDICATOR DASHBOARD ⚔️").formatted(Formatting.GOLD, Formatting.BOLD), width/2, panelY + 12, 0xFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("Premium Customization Suite").formatted(Formatting.GRAY), width/2, panelY + 26, 0xAAAAAA);
        
        // Render widgets
        super.render(ctx, mouseX, mouseY, delta);
        
        // Render custom sliders
        for (SimpleSlider slider : sliders) slider.render(ctx, mouseX, mouseY, delta);
        
        // Live preview
        renderLivePreview(ctx, mouseX, mouseY, delta);
        renderTooltips(ctx, mouseX, mouseY);
        
        // Version info
        ctx.drawText(textRenderer, Text.literal("v1.0.0 • F7 to toggle").formatted(Formatting.DARK_GRAY), panelX + 8, panelY + PANEL_HEIGHT - 18, 0x666666, false);
    }
    
    private void renderAnimatedBackground(DrawContext ctx, float delta) {
        long time = System.currentTimeMillis();
        float t = (time % 10000) / 10000f;
        int topColor = ColorHelper.fromRgb((int)(0x1A + Math.sin(t * Math.PI * 2) * 20), (int)(0x1A + Math.cos(t * Math.PI * 2) * 20), (int)(0x2E + Math.sin(t * Math.PI * 4) * 15));
        int bottomColor = ColorHelper.fromRgb((int)(0x0A + Math.cos(t * Math.PI * 2) * 15), (int)(0x0A + Math.sin(t * Math.PI * 2) * 15), (int)(0x1A + Math.cos(t * Math.PI * 4) * 10));
        for (int y = 0; y < height; y++) {
            float progress = (float)y / height;
            int r = (int)(((topColor >> 16) & 0xFF) * (1 - progress) + ((bottomColor >> 16) & 0xFF) * progress);
            int g = (int)(((topColor >> 8) & 0xFF) * (1 - progress) + ((bottomColor >> 8) & 0xFF) * progress);
            int b = (int)(((topColor & 0xFF) * (1 - progress) + (bottomColor & 0xFF) * progress));
            ctx.fill(0, y, width, y + 1, ColorHelper.fromRgb(r, g, b));
        }
    }
    
    private void renderLivePreview(DrawContext ctx, int mouseX, int mouseY, float delta) {
        previewAnimTime += delta;
        previewHealth = 0.35f + (float)(Math.sin(previewAnimTime * 0.002) * 0.3 + 0.3);
        
        int px = width - 160, py = height - 110;
        int pw = 145, ph = 90;
        
        ctx.fill(px - 4, py - 4, px + pw + 4, py + ph + 4, 0x90101010);
        ctx.fill(px, py, px + pw, py + ph, 0xFF1E1E3A);
        ctx.drawHorizontalLine(px, px + pw, py, cfg.accentColor | 0xFF000000);
        ctx.drawText(textRenderer, Text.literal("◉ LIVE PREVIEW").formatted(Formatting.BOLD, Formatting.AQUA), px + 8, py + 4, 0xFFFFFF, false);
        ctx.drawText(textRenderer, Text.literal(cfg.style.getName()).formatted(Formatting.GRAY), px + 8, py + ph - 20, 0xAAAAAA, false);
        
        int ix = px + 15, iy = py + 28;
        switch (cfg.style) {
            case VANILLA_HEARTS -> renderVanillaPreview(ctx, ix, iy, previewHealth);
            case STATUS_BAR -> renderBarPreview(ctx, ix, iy, previewHealth);
            case SPARK_HEAD -> renderSparkPreview(ctx, ix, iy, previewHealth);
        }
        
        if (cfg.showHealthText) {
            String hp = String.valueOf((int)(previewHealth * 20)) + " HP";
            ctx.drawText(textRenderer, hp, px + pw/2 - textRenderer.getWidth(hp)/2, py + ph - 8, 0xFFFFFF, true);
        }
    }
    
    private void renderBarPreview(DrawContext ctx, int x, int y, float percent) {
        int w = Math.max(40, Math.min(100, cfg.barWidth / 2)), h = Math.max(2, Math.min(6, cfg.barHeight));
        ctx.fill(x - 2, y - 2, x + w + 2, y + h + 2, cfg.backgroundColor | 0x90000000);
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, cfg.backgroundColor | 0x60000000);
        ctx.fill(x, y, x + w, y + h, cfg.backgroundColor | 0x30000000);
        int color = percent > 0.6f ? cfg.colorFull : percent > 0.3f ? cfg.colorHalf : cfg.colorLow;
        ctx.fill(x, y, x + (int)(w * percent), y + h, color | 0xFF000000);
        float shinePos = (System.currentTimeMillis() % 2000) / 2000f;
        if (shinePos < 0.3f) {
            int shineX = x + (int)(shinePos / 0.3f * w);
            ctx.fill(shineX, y, Math.min(shineX + 10, x + w), y + h, 0x40FFFFFF);
        }
        ctx.drawHorizontalLine(x - 1, x + w + 1, y - 1, cfg.borderColor | 0xFF000000);
        ctx.drawHorizontalLine(x - 1, x + w + 1, y + h, cfg.borderColor | 0xFF000000);
        ctx.drawVerticalLine(x - 1, y - 1, y + h, cfg.borderColor | 0xFF000000);
        ctx.drawVerticalLine(x + w + 1, y - 1, y + h, cfg.borderColor | 0xFF000000);
    }
    
    private void renderVanillaPreview(DrawContext ctx, int x, int y, float percent) {
        int hearts = (int)(percent * 10);
        for (int i = 0; i < 10; i++) {
            int hx = x + i * 11;
            if (i < hearts) ctx.drawText(textRenderer, "❤", hx, y, cfg.colorFull, false);
            else if (i == hearts && percent * 10 - hearts > 0.3f) ctx.drawText(textRenderer, "💔", hx, y, cfg.colorHalf, false);
            else ctx.drawText(textRenderer, "🖤", hx, y, 0x404040, false);
        }
    }
    
    private void renderSparkPreview(DrawContext ctx, int x, int y, float percent) {
        int headSize = 14, sparkSize = Math.max(4, cfg.sparkSize / 2), gap = Math.max(1, cfg.sparkGap / 2);
        int totalSparks = 10, active = (int)(percent * totalSparks);
        ctx.fill(x, y, x + headSize, y + headSize, 0xFFB8956A);
        ctx.drawHorizontalLine(x, x + headSize, y, 0xFFFFFFFF);
        ctx.drawVerticalLine(x, y, y + headSize, 0xFFFFFFFF);
        ctx.drawHorizontalLine(x, x + headSize, y + headSize, 0xFFFFFFFF);
        ctx.drawVerticalLine(x + headSize, y, y + headSize, 0xFFFFFFFF);
        ctx.fill(x + 4, y + 5, x + 6, y + 7, 0x202020);
        ctx.fill(x + 8, y + 5, x + 10, y + 7, 0x202020);
        int sparkX = x + headSize + gap;
        for (int i = 0; i < totalSparks; i++) {
            int sx = sparkX + i * (sparkSize + gap);
            if (i < active) {
                ctx.fill(sx, y + 2, sx + sparkSize, y + 2 + sparkSize, 0xFFFFAA00);
                ctx.fill(sx + 1, y + 3, sx + sparkSize - 1, y + sparkSize - 1, 0xFFFFFFFF);
                if (i == active - 1 && cfg.sparkEatAnimation) {
                    float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.01) * 0.2 + 0.8);
                    ctx.fill(sx - 2, y, sx + sparkSize + 2, y + sparkSize + 4, (int)(pulse * 80) << 24 | 0xFFFFAA00);
                }
            } else {
                ctx.fill(sx, y + 2, sx + sparkSize, y + 2 + sparkSize, 0x303030);
                if (i == active && cfg.sparkBreakAnimation) {
                    int shake = (int)(Math.sin(System.currentTimeMillis() * 0.02) * 2);
                    ctx.fill(sx + shake, y + 2, sx + sparkSize + shake, y + 2 + sparkSize, 0x60FF4444);
                    ctx.drawHorizontalLine(sx + 2, sx + sparkSize - 2, y + sparkSize/2 + 2, 0xFFFF6666);
                }
            }
        }
    }
    
    private void renderTooltips(DrawContext ctx, int mouseX, int mouseY) {
        for (ColorPicker cp : colorPickers) {
            if (cp.isMouseOver(mouseX, mouseY)) {
                String tip = cp == colorPickers.get(0) ? "Color when health > 60%" : cp == colorPickers.get(1) ? "Color when health 30-60%" : "Color when health < 30%";
                ctx.drawTooltip(textRenderer, Text.literal(tip), mouseX, mouseY);
            }
        }
        if (styleButton != null && styleButton.isMouseOver(mouseX, mouseY)) {
            ctx.drawTooltip(textRenderer, Text.literal("Choose indicator display style"), mouseX, mouseY);
        }
    }
    
    @Override
    public boolean shouldCloseOnEsc() { return false; }
    
    @Override
    public void close() {
        if (client != null) { cfg.validate(); ConfigManager.save(); client.setScreen(parent); }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_F7) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (SimpleSlider slider : sliders) { if (slider.mouseClicked(mouseX, mouseY, button)) return true; }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (SimpleSlider slider : sliders) { slider.mouseReleased(mouseX, mouseY, button); }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (SimpleSlider slider : sliders) { if (slider.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true; }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
