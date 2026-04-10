package net.yourname.pvpmod.util;

public class AnimationUtils {
    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }
    
    public static int lerpColor(float t, int c1, int c2) {
        int r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int r = (int) (r1 + t * (r2 - r1));
        int g = (int) (g1 + t * (g2 - g1));
        int b = (int) (b1 + t * (b2 - b1));
        return (r << 16) | (g << 8) | b;
    }
}
