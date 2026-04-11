package net.yourname.pvpmod.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.yourname.pvpmod.config.ConfigManager;
import net.yourname.pvpmod.config.IndicatorStyle;
import net.yourname.pvpmod.config.ModConfig;
import net.yourname.pvpmod.indicator.PlayerIndicator;
import net.yourname.pvpmod.util.RenderUtils;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.*;

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
    private static final int GLASS_HIGHLIGHT = 0x30FFFFFF;
    
    // ===== PARTICLE SYSTEM =====
    private final List<Particle> particles = new ArrayList<>();
    private long particleTimer = 0;
    
    // ===== TAB SYSTEM =====
    private enum Tab { GENERAL, APPEARANCE, ADVANCED, PRESETS }
    private Tab currentTab = Tab.GENERAL;
    private final Map<Tab, List<UIElement>> tabElements = new EnumMap<>(Tab.class);
    
    // ===== UI ELEMENTS =====
    private CyclingButtonWidget<IndicatorStyle> styleButton;
    private CyclingButtonWidget<Boolean> enabledButton, animButton, textButton;
    private PremiumSlider widthSlider, heightSlider, animSpeedSlider, yOffsetSlider;
    private ColorPickerButton colorFullBtn, colorHalfBtn, colorLowBtn;
    private TextFieldWidget hexColorInput;
    private ButtonWidget saveBtn, resetBtn, exportBtn, importBtn, closeBtn;
    private TabButton generalTab, appearanceTab, advancedTab, presetsTab;
    
    // ===== PREVIEW STATE =====
    private float previewHealth = 0.65f;
    private long previewAnimTime = 0;
    private int previewStyleIndex = 0;
    
    // ===== CONFIG =====
    private final Screen parent;
    private ModConfig cfg;
    private boolean needsRefresh = false;
    
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
                rand.nextFloat() * width,
                rand.nextFloat() * height,
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
    
    // ===== TAB BUTTONS =====
    private class TabButton extends ButtonWidget {
        private final Tab tab;
        private boolean active = false;
        
        TabButton(int x, int y, int width, Tab tab, OnPress onPress) {
            super(x, y, width, TAB_HEIGHT, Text.literal(tab.name()), onPress, DEFAULT_NARRATION_SUPPLIER);
            this.tab = tab;
        }
        
        void setActive(boolean active) { this.active = active; }
        
        @Override
        public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
            int color = active ? 0xFF4040FF : (isHovered() ? 0xFF303080 : 0xFF202040);
            ctx.fill(getX(), getY(), getX() + width, getY() + height, color | 0x60000000);
            
            if (active) {
                ctx.drawHorizontalLine(getX(), getX() + width, getY() + height - 1, 0xFF6060FF);
            }
            
            Formatting fmt = active ? Formatting.WHITE : Formatting.GRAY;
            ctx.drawCenteredTextWithShadow(textRenderer, 
                Text.literal(tab.name().charAt(0) + tab.name().substring(1).toLowerCase()).formatted(fmt),
                getX() + width/2, getY() + (height - 8)/2, 0xFFFFFF);
        }
    }
    
    // ===== COLOR PICKER BUTTON =====
    private class ColorPickerButton extends ButtonWidget {
        private int color;
        private final String label;
        private final java.util.function.Consumer<Integer> onColorChange;
        
        ColorPickerButton(int x, int y, int width, int height, String label, int color, 
                         java.util.function.Consumer<Integer> onColorChange) {
            super(x, y, width, height, Text.literal(label), btn -> {}, DEFAULT_NARRATION_SUPPLIER);
            this.color = color;
            this.label = label;
            this.onColorChange = onColorChange;
        }
        
        void setColor(int color) { this.color = color; }
        int getColor() { return color; }
        
        @Override
        public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
            // Color preview box
            ctx.fill(getX(), getY(), getX() + 24, getY() + height, color | 0xFF000000);
            ctx.drawHorizontalLine(getX(), getX() + 24, getY(), 0xFFFFFFFF);
            ctx.drawVerticalLine(getX(), getY(), getY() + height, 0xFFFFFFFF);
            
            // Label
            ctx.drawText(textRenderer, label, getX() + 30, getY() + (height - 8)/2, 0xFFFFFF, false);
            
            // Hover effect
            if (isHovered()) {
                ctx.fill(getX() - 2, getY() - 2, getX() + width + 2, getY() + height + 2, 0x20FFFFFF);
            }
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isHovered() && button == 0) {
                // Open color picker dialog (simplified)
                openColorPicker();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        
        private void openColorPicker() {
            // Simple color cycle for now (can expand to full picker)
            int[] palette = {0x00CC00, 0x00FF00, 0xCCFF00, 0xFFCC00, 0xFF6600, 0xCC0000, 0xFF0066, 0xCC00CC, 0x6600FF, 0x0099FF, 0x00CCFF, 0xFFFFFF};
            int idx = Arrays.asList(palette).indexOf(color);
            color = palette[(idx + 1) % palette.length];
            onColorChange.accept(color);
            ConfigManager.save();
        }
    }
    
    // ✅ REPLACE the abstract PremiumSlider class with this concrete SliderWidget-based version:
private abstract static class PremiumSlider extends SliderWidget {
    protected final String prefix;
    
    PremiumSlider(int x, int y, int width, int height, String prefix, double value) {
        super(x, y, width, height, Text.empty(), value);
        this.prefix = prefix;
    }
    
    @Override
    protected void updateMessage() {
        setMessage(Text.literal(prefix + ": " + getDisplayValue()));
    }
    
    @Override
    protected void applyValue() {
        // Subclasses override this
    }
    
    protected abstract String getDisplayValue();
    
    @Override
    public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Custom glass-style rendering
        boolean hovered = mouseX >= getX() && mouseX < getX() + width && 
                         mouseY >= getY() && mouseY < getY() + height;
        
        // Track background
        ctx.fill(getX(), getY() + height/2 - 2, getX() + width, getY() + height/2 + 2, 0xFF303050);
        
        // Filled portion
        int fillW = (int) (value * width);
        ctx.fill(getX(), getY() + height/2 - 2, getX() + fillW, getY() + height/2 + 2, 0xFF6060FF);
        
        // Thumb
        int thumbX = getX() + (int) (value * width);
        ctx.fill(thumbX - 8, getY() + 2, thumbX + 8, getY() + height - 2, 0xFF4040FF);
        ctx.drawHorizontalLine(thumbX - 8, thumbX + 8, getY() + height/2, 0xFFFFFFFF);
        
        // Hover glow
        if (hovered) {
            ctx.fill(getX() - 2, getY() - 2, getX() + width + 2, getY() + height + 2, 0x20FFFFFF);
        }
        
        // Label
        ctx.drawCenteredTextWithShadow(textRenderer, getMessage(), 
            getX() + width/2, getY() + (height - 8)/2, 0xFFFFFF);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (active && visible && button == 0) {
            value = Math.max(0, Math.min(1, (mouseX - getX()) / width));
            applyValue();
            updateMessage();
            return true;
        }
        return false;
    }
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
    
    // ===== UI ELEMENT BASE =====
    private interface UIElement extends Element, Selectable {
        void render(DrawContext ctx, int mouseX, int mouseY, float delta);
        boolean isHovered(int mouseX, int mouseY);
    }
    
    // ===== INIT =====
    @Override
    protected void init() {
        super.init();
        cfg = ConfigManager.get();
        tabElements.clear();
        
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        
        // ===== TABS =====
        int tabX = panelX + 10;
        generalTab = addDrawableChild(new TabButton(tabX, panelY + 8, 90, Tab.GENERAL, t -> switchTab(Tab.GENERAL)));
        appearanceTab = addDrawableChild(new TabButton(tabX + 95, panelY + 8, 90, Tab.APPEARANCE, t -> switchTab(Tab.APPEARANCE)));
        advancedTab = addDrawableChild(new TabButton(tabX + 190, panelY + 8, 90, Tab.ADVANCED, t -> switchTab(Tab.ADVANCED)));
        presetsTab = addDrawableChild(new TabButton(tabX + 285, panelY + 8, 90, Tab.PRESETS, t -> switchTab(Tab.PRESETS)));
        updateTabButtons();
        
        // ===== CONTENT AREA =====
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
        saveBtn = addDrawableChild(PremiumButton.builder(
                Text.literal("💾 Save").formatted(Formatting.GREEN, Formatting.BOLD),
                b -> { cfg.validate(); ConfigManager.save(); },
                0xFF208020, 0xFF30A030, 0xFF106010)
            .dimensions(panelX + 20, btnY, 80, 24).build());
        
        resetBtn = addDrawableChild(PremiumButton.builder(
                Text.literal("↺ Reset").formatted(Formatting.YELLOW),
                b -> { cfg = new ModConfig(); cfg.validate(); ConfigManager.save(); init(); })
            .dimensions(panelX + 110, btnY, 80, 24).build());
        
        exportBtn = addDrawableChild(PremiumButton.builder(
                Text.literal("📤 Export").formatted(Formatting.AQUA),
                b -> exportConfig())
            .dimensions(panelX + 200, btnY, 90, 24).build());
        
        importBtn = addDrawableChild(PremiumButton.builder(
                Text.literal("📥 Import").formatted(Formatting.LIGHT_PURPLE),
                b -> importConfig())
            .dimensions(panelX + 300, btnY, 90, 24).build());
        
        closeBtn = addDrawableChild(PremiumButton.builder(
                Text.literal("✕ Close").formatted(Formatting.RED, Formatting.BOLD),
                b -> close())
            .dimensions(panelX + PANEL_WIDTH - 100, btnY, 80, 24).build());
    }
    
    private void switchTab(Tab tab) {
        currentTab = tab;
        updateTabButtons();
        init(); // Rebuild content
    }
    
    private void updateTabButtons() {
        generalTab.setActive(currentTab == Tab.GENERAL);
        appearanceTab.setActive(currentTab == Tab.APPEARANCE);
        advancedTab.setActive(currentTab == Tab.ADVANCED);
        presetsTab.setActive(currentTab == Tab.PRESETS);
    }
    
    // ===== TAB CONTENT =====
    private void initGeneralTab(int x, int y) {
        // Enabled Toggle
        enabledButton = addDrawableChild(CyclingButtonWidget.<Boolean>builder(v -> 
                v ? Text.literal("● Enabled").formatted(Formatting.GREEN) 
                  : Text.literal("○ Disabled").formatted(Formatting.RED))
            .values(true, false)
            .initialValue(cfg.enabled)
            .onPress((btn, val) -> { cfg.enabled = val; ConfigManager.save(); })
            .dimensions(x, y, CONTROL_WIDTH, 22)
            .build(Text.literal(cfg.enabled ? "Enabled" : "Disabled")));
        y += ROW_HEIGHT;
        
        // Style Selector
        addDrawableChild(Text.literal("Indicator Style:").asOrderedText(), x, y + 6, 0xDDDDDD);
        styleButton = addDrawableChild(CyclingButtonWidget.<IndicatorStyle>builder(s -> 
                Text.literal(s.getName()).formatted(Formatting.WHITE))
            .values(IndicatorStyle.values())
            .initialValue(cfg.style)
            .onPress((btn, style) -> { cfg.style = style; ConfigManager.save(); init(); })
            .dimensions(x + 120, y, CONTROL_WIDTH, 22)
            .build(Text.literal(cfg.style.getName())));
        y += ROW_HEIGHT;
        
        // Animation Toggle
        animButton = addDrawableChild(CyclingButtonWidget.<Boolean>builder(v ->
                v ? Text.literal("● Smooth").formatted(Formatting.GREEN)
                  : Text.literal("○ Instant").formatted(Formatting.GRAY))
            .values(true, false)
            .initialValue(cfg.smoothAnimation)
            .onPress((btn, val) -> { cfg.smoothAnimation = val; ConfigManager.save(); })
            .dimensions(x, y, CONTROL_WIDTH, 22)
            .build(Text.literal(cfg.smoothAnimation ? "Smooth" : "Instant")));
        y += ROW_HEIGHT;
        
        // Health Text Toggle
        textButton = addDrawableChild(CyclingButtonWidget.<Boolean>builder(v ->
                v ? Text.literal("● Show HP").formatted(Formatting.GREEN)
                  : Text.literal("○ Hide HP").formatted(Formatting.GRAY))
            .values(true, false)
            .initialValue(cfg.showHealthText)
            .onPress((btn, val) -> { cfg.showHealthText = val; ConfigManager.save(); })
            .dimensions(x, y, CONTROL_WIDTH, 22)
            .build(Text.literal(cfg.showHealthText ? "Show" : "Hide")));
    }
    
    private void initAppearanceTab(int x, int y) {
        // Width Slider
        addDrawableChild(Text.literal("Bar Width:").asOrderedText(), x, y + 6, 0xDDDDDD);
        widthSlider = addDrawableChild(new PremiumSlider(x + 120, y, 150, 22, "", (cfg.barWidth - 20) / 180.0) {
            @Override protected String getDisplayValue() { return String.valueOf(cfg.barWidth); }
            @Override protected void applyValue() { cfg.barWidth = (int) (value * 180) + 20; ConfigManager.save(); }
        });
        y += ROW_HEIGHT;
        
        // Height Slider
        addDrawableChild(Text.literal("Bar Height:").asOrderedText(), x, y + 6, 0xDDDDDD);
        heightSlider = addDrawableChild(new PremiumSlider(x + 120, y, 150, 22, "", (cfg.barHeight - 2) / 8.0) {
            @Override protected String getDisplayValue() { return String.valueOf(cfg.barHeight); }
            @Override protected void applyValue() { cfg.barHeight = (int) (value * 8) + 2; ConfigManager.save(); }
        });
        y += ROW_HEIGHT;
        
        // Y Offset Slider
        addDrawableChild(Text.literal("Y Offset:").asOrderedText(), x, y + 6, 0xDDDDDD);
        yOffsetSlider = addDrawableChild(new PremiumSlider(x + 120, y, 150, 22, "", (cfg.yOffset - 1.0) / 2.0) {
            @Override protected String getDisplayValue() { return String.format("%.1f", cfg.yOffset); }
            @Override protected void applyValue() { cfg.yOffset = value * 2.0 + 1.0; ConfigManager.save(); }
        });
        y += ROW_HEIGHT;
        
        // Animation Speed
        addDrawableChild(Text.literal("Anim Speed:").asOrderedText(), x, y + 6, 0xDDDDDD);
        animSpeedSlider = addDrawableChild(new PremiumSlider(x + 120, y, 150, 22, "", cfg.animationSpeed) {
            @Override protected String getDisplayValue() { return (int)(value * 100) + "%"; }
            @Override protected void applyValue() { cfg.animationSpeed = (float) value; ConfigManager.save(); }
        });
        y += ROW_HEIGHT + 10;
        
        // Color Pickers
        addDrawableChild(Text.literal("Colors:").formatted(Formatting.WHITE).asOrderedText(), x, y + 6, 0xFFFFFF);
        colorFullBtn = addDrawableChild(new ColorPickerButton(x, y + 22, 180, 22, "Full Health", cfg.colorFull, c -> { cfg.colorFull = c; ConfigManager.save(); }));
        colorHalfBtn = addDrawableChild(new ColorPickerButton(x, y + 46, 180, 22, "Half Health", cfg.colorHalf, c -> { cfg.colorHalf = c; ConfigManager.save(); }));
        colorLowBtn = addDrawableChild(new ColorPickerButton(x, y + 70, 180, 22, "Low Health", cfg.colorLow, c -> { cfg.colorLow = c; ConfigManager.save(); }));
    }
    
    private void initAdvancedTab(int x, int y) {
        // Max Render Distance
        addDrawableChild(Text.literal("Max Distance:").asOrderedText(), x, y + 6, 0xDDDDDD);
        addDrawableChild(new PremiumSlider(x + 120, y, 150, 22, "", (cfg.maxRenderDistance - 16) / 48.0) {
            @Override protected String getDisplayValue() { return (int)cfg.maxRenderDistance + " blocks"; }
            @Override protected void applyValue() { cfg.maxRenderDistance = value * 48 + 16; ConfigManager.save(); }
        });
        y += ROW_HEIGHT;
        
        // Spark Settings (only show if Spark style)
        if (cfg.style == IndicatorStyle.SPARK_HEAD) {
            addDrawableChild(Text.literal("Spark Size:").asOrderedText(), x, y + 6, 0xDDDDDD);
            addDrawableChild(new PremiumSlider(x + 120, y, 150, 22, "", (cfg.sparkSize - 4) / 12.0) {
                @Override protected String getDisplayValue() { return String.valueOf(cfg.sparkSize); }
                @Override protected void applyValue() { cfg.sparkSize = (int)(value * 12) + 4; ConfigManager.save(); }
            });
            y += ROW_HEIGHT;
            
            CyclingButtonWidget.<Boolean>builder(v -> v ? Text.literal("● Break FX").formatted(Formatting.GREEN) : Text.literal("○ No FX").formatted(Formatting.GRAY))
                .values(true, false)
                .initialValue(cfg.sparkBreakAnimation)
                .onPress((btn, val) -> { cfg.sparkBreakAnimation = val; ConfigManager.save(); })
                .dimensions(x, y, CONTROL_WIDTH, 22)
                .build(Text.literal(cfg.sparkBreakAnimation ? "Enabled" : "Disabled"));
            y += ROW_HEIGHT;
        }
        
        // Team Filter
        CyclingButtonWidget.<Boolean>builder(v -> v ? Text.literal("● Filter Team").formatted(Formatting.GREEN) : Text.literal("○ Show All").formatted(Formatting.GRAY))
            .values(true, false)
            .initialValue(cfg.filterByTeam)
            .onPress((btn, val) -> { cfg.filterByTeam = val; ConfigManager.save(); })
            .dimensions(x, y, CONTROL_WIDTH, 22)
            .build(Text.literal(cfg.filterByTeam ? "Enabled" : "Disabled"));
    }
    
    private void initPresetsTab(int x, int y) {
        // Preset Buttons
        String[] presets = {"Default", "Cyberpunk", "Nature", "Minimal", "Custom"};
        for (int i = 0; i < presets.length; i++) {
            String name = presets[i];
            addDrawableChild(PremiumButton.builder(
                    Text.literal(name).formatted(Formatting.WHITE),
                    b -> applyPreset(name),
                    0xFF303060, 0xFF404080, 0xFF202040)
                .dimensions(x, y + i * 26, CONTROL_WIDTH, 22)
                .build());
        }
        
        // Hex Color Input
        addDrawableChild(Text.literal("Custom Hex:").asOrderedText(), x, y + 150, 0xDDDDDD);
        hexColorInput = new TextFieldWidget(textRenderer, x + 100, y + 148, 100, 20, Text.empty());
        hexColorInput.setMaxLength(7);
        hexColorInput.setText(String.format("#%06X", cfg.colorFull & 0xFFFFFF));
        addDrawableChild(hexColorInput);
    }
    
    private void applyPreset(String name) {
        switch (name) {
            case "Cyberpunk" -> {
                cfg.colorFull = 0x00FFFF; cfg.colorHalf = 0xFF00FF; cfg.colorLow = 0xFF0066;
                cfg.accentColor = 0xFF00FF; cfg.backgroundColor = 0x0A0A1A; cfg.borderColor = 0x00FFFF;
            }
            case "Nature" -> {
                cfg.colorFull = 0x00CC00; cfg.colorHalf = 0x99CC00; cfg.colorLow = 0xCC6600;
                cfg.accentColor = 0x00AA00; cfg.backgroundColor = 0x1A2A1A; cfg.borderColor = 0x00FF00;
            }
            case "Minimal" -> {
                cfg.colorFull = 0xFFFFFF; cfg.colorHalf = 0xAAAAAA; cfg.colorLow = 0x666666;
                cfg.accentColor = 0xFFFFFF; cfg.backgroundColor = 0x000000; cfg.borderColor = 0x404040;
            }
            case "Custom" -> {
                // Keep current
            }
            default -> {
                // Default values
                cfg.colorFull = 0x00CC00; cfg.colorHalf = 0xCCCC00; cfg.colorLow = 0xCC0000;
                cfg.accentColor = 0x4040FF; cfg.backgroundColor = 0x000000; cfg.borderColor = 0xFFFFFF;
            }
        }
        ConfigManager.save();
        init();
    }
    
    private void exportConfig() {
        String json = com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(cfg);
        MinecraftClient.getInstance().keyboard.setClipboard(json);
        // Could show toast notification here
    }
    
    private void importConfig() {
        String json = MinecraftClient.getInstance().keyboard.getClipboard();
        try {
            ModConfig imported = com.google.gson.GsonBuilder().create().fromJson(json, ModConfig.class);
            if (imported != null) {
                cfg = imported;
                cfg.validate();
                ConfigManager.save();
                init();
            }
        } catch (Exception e) {
            // Invalid JSON, ignore
        }
    }
    
    // ===== RENDER =====
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Update particles
        updateParticles(delta);
        
        // Animated background gradient
        renderAnimatedBackground(ctx, delta);
        
        // Particle overlay
        renderParticles(ctx, delta);
        
        // Dark overlay for focus
        ctx.fill(0, 0, width, height, 0x50000000);
        
        // Main panel with glass effect
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;
        
        // Glass background with blur simulation
        ctx.fill(panelX - 3, panelY - 3, panelX + PANEL_WIDTH + 3, panelY + PANEL_HEIGHT + 3, 0x60000000);
        ctx.fill(panelX - 2, panelY - 2, panelX + PANEL_WIDTH + 2, panelY + PANEL_HEIGHT + 2, 0x40000000);
        ctx.fill(panelX - 1, panelY - 1, panelX + PANEL_WIDTH + 1, panelY + PANEL_HEIGHT + 1, 0x20FFFFFF);
        ctx.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, GLASS_BG);
        
        // Animated border glow
        float glowPulse = (float) (Math.sin(System.currentTimeMillis() * 0.003) * 0.3 + 0.7);
        int glowColor = (int) (glowPulse * 128) << 24 | (GLASS_BORDER & 0x00FFFFFF);
        ctx.drawHorizontalLine(panelX - 2, panelX + PANEL_WIDTH + 2, panelY - 2, glowColor);
        ctx.drawHorizontalLine(panelX - 2, panelX + PANEL_WIDTH + 2, panelY + PANEL_HEIGHT + 2, glowColor);
        ctx.drawVerticalLine(panelX - 2, panelY - 2, panelY + PANEL_HEIGHT + 2, glowColor);
        ctx.drawVerticalLine(panelX + PANEL_WIDTH + 2, panelY - 2, panelY + PANEL_HEIGHT + 2, glowColor);
        
        // Title with gradient text effect
        String title = "⚔️ PvP INDICATOR DASHBOARD ⚔️";
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(title).formatted(Formatting.GOLD, Formatting.BOLD), 
            width / 2, panelY + 12, 0xFFFFFF);
        
        // Subtitle
        ctx.drawCenteredTextWithShadow(textRenderer, 
            Text.literal("Premium Customization Suite").formatted(Formatting.GRAY),
            width / 2, panelY + 26, 0xAAAAAA);
        
        // Render all widgets
        super.render(ctx, mouseX, mouseY, delta);
        
        // Live Preview Panel
        renderLivePreview(ctx, mouseX, mouseY, delta);
        
        // Tooltips
        renderTooltips(ctx, mouseX, mouseY);
        
        // Version info
        ctx.drawText(textRenderer, Text.literal("v1.0.0 • F7 to toggle").formatted(Formatting.DARK_GRAY), 
            panelX + 8, panelY + PANEL_HEIGHT - 18, 0x666666, false);
    }
    
    private void renderAnimatedBackground(DrawContext ctx, float delta) {
        // Animated gradient background
        long time = System.currentTimeMillis();
        float t = (time % 10000) / 10000f;
        
        // Base gradient
        int topColor = ColorHelper.fromRgb(
            (int) (0x1A + Math.sin(t * Math.PI * 2) * 20),
            (int) (0x1A + Math.cos(t * Math.PI * 2) * 20),
            (int) (0x2E + Math.sin(t * Math.PI * 4) * 15)
        );
        int bottomColor = ColorHelper.fromRgb(
            (int) (0x0A + Math.cos(t * Math.PI * 2) * 15),
            (int) (0x0A + Math.sin(t * Math.PI * 2) * 15),
            (int) (0x1A + Math.cos(t * Math.PI * 4) * 10)
        );
        
        // Draw gradient
        for (int y = 0; y < height; y++) {
            float progress = (float) y / height;
            int r = (int) (((topColor >> 16) & 0xFF) * (1 - progress) + ((bottomColor >> 16) & 0xFF) * progress);
            int g = (int) (((topColor >> 8) & 0xFF) * (1 - progress) + ((bottomColor >> 8) & 0xFF) * progress);
            int b = (int) (((topColor & 0xFF) * (1 - progress) + (bottomColor & 0xFF) * progress));
            ctx.fill(0, y, width, y + 1, ColorHelper.fromRgb(r, g, b));
        }
    }
    
    private void renderLivePreview(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Animate preview health
        previewAnimTime += delta;
        previewHealth = 0.35f + (float) (Math.sin(previewAnimTime * 0.002) * 0.3 + 0.3);
        
        int px = width - 160, py = height - 110;
        int pw = 145, ph = 90;
        
        // Preview panel
        ctx.fill(px - 4, py - 4, px + pw + 4, py + ph + 4, 0x90101010);
        ctx.fill(px, py, px + pw, py + ph, 0xFF1E1E3A);
        ctx.drawHorizontalLine(px, px + pw, py, cfg.accentColor | 0xFF000000);
        
        // Preview title
        ctx.drawText(textRenderer, Text.literal("◉ LIVE PREVIEW").formatted(Formatting.BOLD, Formatting.AQUA), 
                    px + 8, py + 4, 0xFFFFFF, false);
        
        // Style indicator
        String styleName = cfg.style.getName();
        ctx.drawText(textRenderer, Text.literal(styleName).formatted(Formatting.GRAY), 
                    px + 8, py + ph - 20, 0xAAAAAA, false);
        
        // Render indicator based on style
        int ix = px + 15, iy = py + 28;
        switch (cfg.style) {
            case VANILLA_HEARTS -> renderVanillaPreview(ctx, ix, iy, previewHealth);
            case STATUS_BAR -> renderBarPreview(ctx, ix, iy, previewHealth);
            case SPARK_HEAD -> renderSparkPreview(ctx, ix, iy, previewHealth);
        }
        
        // Health value
        if (cfg.showHealthText) {
            String hp = String.valueOf((int) (previewHealth * 20)) + " HP";
            ctx.drawText(textRenderer, hp, px + pw/2 - textRenderer.getWidth(hp)/2, py + ph - 8, 0xFFFFFF, true);
        }
        
        // Hover interaction: click to cycle preview style
        if (mouseX >= px && mouseX < px + pw && mouseY >= py && mouseY < py + ph && 
            mouseX >= px + 8 && mouseX < px + 100 && mouseY >= py + ph - 22 && mouseY < py + ph - 8) {
            if (mouseX >= px && mouseY >= py) {
                // Could add click handler here
            }
        }
    }
    
    private void renderBarPreview(DrawContext ctx, int x, int y, float percent) {
        int w = Math.max(40, Math.min(100, cfg.barWidth / 2));
        int h = Math.max(2, Math.min(6, cfg.barHeight));
        
        // Background with glass effect
        ctx.fill(x - 2, y - 2, x + w + 2, y + h + 2, cfg.backgroundColor | 0x90000000);
        ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, cfg.backgroundColor | 0x60000000);
        ctx.fill(x, y, x + w, y + h, cfg.backgroundColor | 0x30000000);
        
        // Health fill with gradient
        int color = getColorForHealth(percent);
        ctx.fill(x, y, x + (int) (w * percent), y + h, color | 0xFF000000);
        
        // Animated shine effect
        float shinePos = (System.currentTimeMillis() % 2000) / 2000f;
        if (shinePos < 0.3f) {
            int shineX = x + (int) (shinePos / 0.3f * w);
            ctx.fill(shineX, y, Math.min(shineX + 10, x + w), y + h, 0x40FFFFFF);
        }
        
        // Border glow
        ctx.drawHorizontalLine(x - 1, x + w + 1, y - 1, cfg.borderColor | 0xFF000000);
        ctx.drawHorizontalLine(x - 1, x + w + 1, y + h, cfg.borderColor | 0xFF000000);
        ctx.drawVerticalLine(x - 1, y - 1, y + h, cfg.borderColor | 0xFF000000);
        ctx.drawVerticalLine(x + w + 1, y - 1, y + h, cfg.borderColor | 0xFF000000);
    }
    
    private void renderVanillaPreview(DrawContext ctx, int x, int y, float percent) {
        int hearts = (int) (percent * 10);
        for (int i = 0; i < 10; i++) {
            int hx = x + i * 11;
            if (i < hearts) {
                ctx.drawText(textRenderer, "❤", hx, y, cfg.colorFull, false);
            } else if (i == hearts && percent * 10 - hearts > 0.3f) {
                ctx.drawText(textRenderer, "💔", hx, y, cfg.colorHalf, false);
            } else {
                ctx.drawText(textRenderer, "🖤", hx, y, 0x404040, false);
            }
        }
    }
    
    private void renderSparkPreview(DrawContext ctx, int x, int y, float percent) {
        int headSize = 14, sparkSize = Math.max(4, cfg.sparkSize / 2);
        int gap = Math.max(1, cfg.sparkGap / 2);
        int totalSparks = 10;
        int active = (int) (percent * totalSparks);
        
        // Head with gradient
        ctx.fill(x, y, x + headSize, y + headSize, 0xFFB8956A);
        ctx.drawHorizontalLine(x, x + headSize, y, 0xFFFFFFFF);
        ctx.drawVerticalLine(x, y, y + headSize, 0xFFFFFFFF);
        ctx.drawHorizontalLine(x, x + headSize, y + headSize, 0xFFFFFFFF);
        ctx.drawVerticalLine(x + headSize, y, y + headSize, 0xFFFFFFFF);
        
        // Eyes
        ctx.fill(x + 4, y + 5, x + 6, y + 7, 0x202020);
        ctx.fill(x + 8, y + 5, x + 10, y + 7, 0x202020);
        
        // Sparks with animation
        int sparkX = x + headSize + gap;
        for (int i = 0; i < totalSparks; i++) {
            int sx = sparkX + i * (sparkSize + gap);
            boolean isActive = i < active;
            
            if (isActive) {
                // Active spark with glow
                ctx.fill(sx, y + 2, sx + sparkSize, y + 2 + sparkSize, 0xFFFFAA00);
                ctx.fill(sx + 1, y + 3, sx + sparkSize - 1, y + sparkSize - 1, 0xFFFFFFFF);
                
                // Pulse animation for recently gained
                if (i == active - 1 && cfg.sparkEatAnimation) {
                    float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.01) * 0.2 + 0.8);
                    ctx.fill(sx - 2, y, sx + sparkSize + 2, y + sparkSize + 4, (int) (pulse * 80) << 24 | 0xFFFFAA00);
                }
            } else {
                // Inactive spark
                ctx.fill(sx, y + 2, sx + sparkSize, y + 2 + sparkSize, 0x303030);
                
                // Break animation
                if (i == active && cfg.sparkBreakAnimation) {
                    int shake = (int) (Math.sin(System.currentTimeMillis() * 0.02) * 2);
                    ctx.fill(sx + shake, y + 2, sx + sparkSize + shake, y + 2 + sparkSize, 0x60FF4444);
                    // Crack lines
                    ctx.drawHorizontalLine(sx + 2, sx + sparkSize - 2, y + sparkSize/2 + 2, 0xFFFF6666);
                }
            }
        }
    }
    
    private int getColorForHealth(float percent) {
        if (percent > 0.6f) return cfg.colorFull;
        if (percent > 0.3f) return cfg.colorHalf;
        return cfg.colorLow;
    }
    
    private void renderTooltips(DrawContext ctx, int mouseX, int mouseY) {
        // Simple tooltip system
        if (colorFullBtn != null && colorFullBtn.isHovered() && colorFullBtn.isMouseOver(mouseX, mouseY)) {
            ctx.drawTooltip(textRenderer, Text.literal("Color when health > 60%"), mouseX, mouseY);
        } else if (colorHalfBtn != null && colorHalfBtn.isMouseOver(mouseX, mouseY)) {
            ctx.drawTooltip(textRenderer, Text.literal("Color when health 30-60%"), mouseX, mouseY);
        } else if (colorLowBtn != null && colorLowBtn.isMouseOver(mouseX, mouseY)) {
            ctx.drawTooltip(textRenderer, Text.literal("Color when health < 30%"), mouseX, mouseY);
        } else if (styleButton != null && styleButton.isMouseOver(mouseX, mouseY)) {
            ctx.drawTooltip(textRenderer, Text.literal("Choose indicator display style"), mouseX, mouseY);
        }
    }
    
    @Override
    public boolean shouldCloseOnEsc() { return false; }
    
    @Override
    public void close() {
        if (client != null) {
            cfg.validate();
            ConfigManager.save();
            client.setScreen(parent);
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // F7 to close
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_F7) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
