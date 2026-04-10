package net.yourname.pvpmod.config;

public class ModConfig {
    // ===== INDICATOR SETTINGS =====
    public boolean enabled = true;
    public IndicatorStyle style = IndicatorStyle.STATUS_BAR;
    
    // Position & Size
    public double yOffset = 1.9; // Above head
    public int barWidth = 50;
    public int barHeight = 4;
    public double maxRenderDistance = 64.0;
    
    // Colors (RGB hex without alpha)
    public int colorFull = 0x00CC00; // Green
    public int colorHalf = 0xCCCC00; // Yellow
    public int colorLow = 0xCC0000;  // Red
    public int backgroundColor = 0x000000;
    public int borderColor = 0xFFFFFF;
    
    // Style-specific
    public boolean showHealthText = true;
    public boolean showPlayerName = false;
    public boolean smoothAnimation = true;
    public float animationSpeed = 0.15f; // 0.0-1.0
    
    // Spark Style settings
    public int sparkSize = 8;
    public int sparkGap = 2;
    public boolean sparkBreakAnimation = true;
    public boolean sparkEatAnimation = true;
    
    // Dashboard UI
    public int dashboardTheme = 0; // 0=dark, 1=light, 2=custom
    public int accentColor = 0x4040FF;
    
    // ===== SAVE/LOAD =====
    public void validate() {
        barWidth = Math.max(20, Math.min(200, barWidth));
        barHeight = Math.max(2, Math.min(10, barHeight));
        yOffset = Math.max(1.0, Math.min(3.0, yOffset));
        animationSpeed = Math.max(0.01f, Math.min(1.0f, animationSpeed));
    }
}
