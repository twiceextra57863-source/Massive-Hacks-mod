package net.yourname.pvpmod.config;

public class ModConfig {
    // Core
    public boolean enabled = true;
    public IndicatorStyle style = IndicatorStyle.STATUS_BAR;
    
    // Position & Size
    public double yOffset = 1.9;
    public int barWidth = 50, barHeight = 4;
    public double maxRenderDistance = 64.0;
    
    // Colors (RGB hex)
    public int colorFull = 0x00CC00, colorHalf = 0xCCCC00, colorLow = 0xCC0000;
    public int backgroundColor = 0x000000, borderColor = 0xFFFFFF, accentColor = 0x4040FF;
    
    // Toggles
    public boolean showHealthText = true, showPlayerName = false, smoothAnimation = true;
    public boolean sparkBreakAnimation = true, sparkEatAnimation = true, filterByTeam = true;
    
    // Spark Style
    public int sparkSize = 8, sparkGap = 2;
    
    // Animation
    public float animationSpeed = 0.15f;
    
    // UI
    public int dashboardTheme = 0;
    
    public void validate() {
        barWidth = Math.max(20, Math.min(200, barWidth));
        barHeight = Math.max(2, Math.min(10, barHeight));
        yOffset = Math.max(1.0, Math.min(3.0, yOffset));
        animationSpeed = Math.max(0.01f, Math.min(1.0f, animationSpeed));
        maxRenderDistance = Math.max(16, Math.min(128, maxRenderDistance));
    }
}
