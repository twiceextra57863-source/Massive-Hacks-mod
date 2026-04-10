// ✅ MUST HAVE: Package declaration at VERY TOP (line 1)
package net.yourname.pvpmod.util;

// ✅ Required imports for 1.21.4
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Utility class for 3D → 2D screen projection
 * Used for rendering health bars above player heads
 */
public class RenderUtils {
    
    /**
     * Projects a 3D world position to 2D screen coordinates
     * @param x World X coordinate
     * @param y World Y coordinate  
     * @param z World Z coordinate
     * @param camPos Camera position
     * @param view View matrix (JOML)
     * @param proj Projection matrix (JOML)
     * @param mc MinecraftClient instance
     * @return double[]{screenX, screenY} or null if off-screen
     */
    public static double[] projectToScreen(double x, double y, double z, 
                                          Vec3d camPos, Matrix4f view, Matrix4f proj, 
                                          MinecraftClient mc) {
        // Calculate relative position from camera
        double dx = x - camPos.x;
        double dy = y - camPos.y;
        double dz = z - camPos.z;
        
        // ✅ Use JOML Vector4f for matrix transformations (1.21.4 requirement)
        Vector4f vec = new Vector4f((float) dx, (float) dy, (float) dz, 1.0f);
        
        // Apply view matrix (camera transform)
        vec.mul(view);
        
        // Apply projection matrix (perspective transform)
        vec.mul(proj);
        
        // Check if behind camera or too close (would cause division by zero)
        if (vec.w <= 0.001f) {
            return null;
        }
        
        // Convert to Normalized Device Coordinates (-1 to +1)
        double ndcX = vec.x / vec.w;
        double ndcY = vec.y / vec.w;
        
        // Convert NDC to screen pixels
        double scale = mc.getWindow().getScaleFactor();
        double screenWidth = mc.getWindow().getScaledWidth();
        double screenHeight = mc.getWindow().getScaledHeight();
        
        double screenX = (ndcX + 1.0) * screenWidth / 2.0;
        double screenY = (1.0 - ndcY) * screenHeight / 2.0;
        
        // ✅ Boundary check with padding (don't render if mostly off-screen)
        int pad = 30;
        if (screenX < -pad || screenX > screenWidth + pad ||
            screenY < -pad || screenY > screenHeight + pad) {
            return null;
        }
        
        return new double[]{screenX, screenY};
    }
    
    /**
     * Simple fallback projection (if matrix method fails)
     * Uses basic perspective formula
     */
    public static double[] projectSimple(double x, double y, double z, 
                                        Vec3d camPos, MinecraftClient mc) {
        double dx = x - camPos.x;
        double dy = y - camPos.y;
        double dz = z - camPos.z;
        
        // Don't project if behind camera
        if (dz >= -0.1) return null;
        
        // Basic perspective projection
        double fov = mc.options.getFov().getValue();
        double aspect = (double) mc.getWindow().getWidth() / mc.getWindow().getHeight();
        double scale = screenHeightFromFov(fov, aspect) / Math.abs(dz);
        
        double screenX = mc.getWindow().getScaledWidth() / 2.0 + dx * scale;
        double screenY = mc.getWindow().getScaledHeight() / 2.0 - dy * scale;
        
        // Boundary check
        if (screenX < -50 || screenX > mc.getWindow().getScaledWidth() + 50 ||
            screenY < -50 || screenY > mc.getWindow().getScaledHeight() + 50) {
            return null;
        }
        
        return new double[]{screenX, screenY};
    }
    
    /**
     * Helper: Calculate screen height from FOV and aspect ratio
     */
    private static double screenHeightFromFov(double fovDegrees, double aspect) {
        double fovRad = Math.toRadians(fovDegrees);
        return 1.0 / Math.tan(fovRad / 2.0) * aspect;
    }
    
    /**
     * Lerp helper for smooth animations
     */
    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }
    
    /**
     * Color lerp for smooth health color transitions
     */
    public static int lerpColor(float t, int color1, int color2) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int r = (int) (r1 + t * (r2 - r1));
        int g = (int) (g1 + t * (g2 - g1));
        int b = (int) (b1 + t * (b2 - b1));
        
        return (r << 16) | (g << 8) | b;
    }
}
