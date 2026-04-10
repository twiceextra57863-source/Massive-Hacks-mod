public static double[] projectToScreen(double x, double y, double z, 
                                      Vec3d camPos, Matrix4f view, Matrix4f proj, 
                                      MinecraftClient mc) {
    // ✅ Use JOML Vector4f for transforms
    org.joml.Vector4f vec = new org.joml.Vector4f(
        (float)(x - camPos.x), 
        (float)(y - camPos.y), 
        (float)(z - camPos.z), 
        1.0f
    );
    
    // Apply view then projection
    vec.mul(view);
    vec.mul(proj);
    
    if (vec.w <= 0.001f) return null;
    
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
