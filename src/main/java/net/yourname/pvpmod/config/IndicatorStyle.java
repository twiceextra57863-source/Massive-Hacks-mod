package net.yourname.pvpmod.config;

public enum IndicatorStyle {
    VANILLA_HEARTS("Vanilla Hearts", "minecraft:textures/gui/icons.png"),
    STATUS_BAR("Status Bar", "pvpmod:textures/gui/status_bar.png"),
    SPARK_HEAD("Head + Sparks", "pvpmod:textures/gui/spark.png");
    
    private final String name;
    private final String texture;
    
    IndicatorStyle(String name, String texture) {
        this.name = name;
        this.texture = texture;
    }
    
    public String getName() { return name; }
    public String getTexture() { return texture; }
    
    @Override
    public String toString() { return name; }
}
