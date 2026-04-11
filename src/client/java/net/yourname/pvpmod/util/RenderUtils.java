package net.yourname.pvpmod.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class RenderUtils {
    public static double[] projectToScreen(double x, double y, double z, Vec3d camPos, 
                                          Matrix4f view, Matrix4f proj, MinecraftClient mc) {
        Vector4f vec = new Vector4f((float)(x-camPos.x), (float)(y-camPos.y), (float)(z-camPos.z), 1f);
        vec.mul(view).mul(proj);
        if (vec.w <= 0.001f) return null;
        double ndcX = vec.x/vec.w, ndcY = vec.y/vec.w;
        double scale = mc.getWindow().getScaleFactor();
        double sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
        double sx = (ndcX+1)*sw/2, sy = (1-ndcY)*sh/2;
        if (sx<-30 || sx>sw+30 || sy<-30 || sy>sh+30) return null;
        return new double[]{sx, sy};
    }
    public static float lerp(float t, float a, float b) { return a + t*(b-a); }
}
