package net.yourname.pvpmod.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import org.joml.Matrix4f;  // ✅ 1.21.4: JOML library
import net.minecraft.util.math.Vec3d;

public class RenderUtils {
    
    public static double[] projectToScreen(double x, double y, double z, 
                                          Vec3d camPos, Matrix4f view, Matrix4f proj, 
                                          MinecraftClient mc) {
        double dx = x - camPos.x, dy = y - camPos.y, dz = z - camPos.z;
        
        // View matrix transform (JOML)
        org.joml.Vector4f vec = new org.joml.Vector4f((float)dx, (float)dy, (float)dz, 1.0f);
        vec.mul(view);
        
        // Projection transform
        vec.mul(proj);
        
        if (vec.w <= 0.001f) return null; // Behind camera
        
        double ndcX = vec.x / vec.w;
        double ndcY = vec.y / vec.w;
        
        double scale = mc.getWindow().getScaleFactor();
        double screenX = (ndcX + 1.0) * mc.getWindow().getWidth() / (2.0 * scale);
        double screenY = (1.0 - ndcY) * mc.getWindow().getHeight() / (2.0 * scale);
        
        // Boundary check
        int pad = 20;
        if (screenX < -pad || screenX > mc.getWindow().getScaledWidth() + pad ||
            screenY < -pad || screenY > mc.getWindow().getScaledHeight() + pad) {
            return null;
        }
        return new double[]{screenX, screenY};
    }
}
