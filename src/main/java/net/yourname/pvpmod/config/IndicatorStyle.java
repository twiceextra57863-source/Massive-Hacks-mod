package net.yourname.pvpmod.config;

public enum IndicatorStyle {
    VANILLA_HEARTS("Vanilla Hearts"),
    STATUS_BAR("Status Bar"),
    SPARK_HEAD("Head + Sparks");
    
    private final String name;
    IndicatorStyle(String name) { this.name = name; }
    public String getName() { return name; }
    @Override public String toString() { return name; }
}
