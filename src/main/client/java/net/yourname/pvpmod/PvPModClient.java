package net.yourname.pvpmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PvPModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // HUD Render Event Register
        HudRenderCallback.EVENT.register(this::onRenderHud);
    }

    private void onRenderHud(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || mc.options.hudHidden) return;

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        
        // Projection matrices for 3D→2D conversion
        Matrix4f proj = mc.gameRenderer.getProjectionMatrix(mc.getTickDelta());
        Matrix4f view = camera.getProjectionMatrix();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player) continue; // Khud ko skip karo
            if (!player.isAlive()) continue;

            // TODO: Yahan opponent filter logic add karna (team/scoreboard)
            if (isOpponent(player, mc.player)) {
                drawHealthBar(context, player, camPos, view, proj, mc);
            }
        }
    }

    private boolean isOpponent(PlayerEntity target, ClientPlayerEntity self) {
        // Abhi sabko dikhayega. Baad me team logic add karenge.
        return true;
    }

    private void drawHealthBar(DrawContext ctx, PlayerEntity player, Vec3d camPos, Matrix4f view, Matrix4f proj, MinecraftClient mc) {
        float health = MathHelper.clamp(player.getHealth(), 0, player.getMaxHealth());
        float maxHealth = player.getMaxHealth();
        if (maxHealth <= 0) return;

        double healthPercent = health / maxHealth;
        int barWidth = 40;
        int barHeight = 3;

        // Head position + offset (1.6 blocks up)
        double px = player.getX();
        double py = player.getY() + 1.9; 
        double pz = player.getZ();

        // 3D to 2D projection
        double[] screen = projectToScreen(px, py, pz, camPos.x, camPos.y, camPos.z, view, proj, mc);
        if (screen == null) return; // Off-screen

        int x = (int) screen[0];
        int y = (int) screen[1];

        // Draw background
        ctx.fill(x - barWidth/2 - 1, y - 1, x + barWidth/2 + 1, y + barHeight + 1, 0x80000000);

        // Health color (Green → Yellow → Red)
        int color;
        if (healthPercent > 0.6) color = 0xFF00CC00;
        else if (healthPercent > 0.3) color = 0xFFCCCC00;
        else color = 0xFFCC0000;

        // Draw filled health
        int filled = (int) (barWidth * healthPercent);
        ctx.fill(x - barWidth/2, y, x - barWidth/2 + filled, y + barHeight, color);

        // Optional: HP Text
        String hp = String.valueOf((int) health);
        ctx.drawText(mc.textRenderer, hp, x - mc.textRenderer.getWidth(hp)/2, y - 10, 0xFFFFFF, true);
    }

    private double[] projectToScreen(double x, double y, double z, double cx, double cy, double cz, Matrix4f view, Matrix4f proj, MinecraftClient mc) {
        // Simple 3D→2D projection logic
        double dx = x - cx, dy = y - cy, dz = z - cz;

        // View matrix
        float[] v = new float[16]; view.writeColumnMajor(v);
        double vx = v[0]*dx + v[4]*dy + v[8]*dz + v[12];
        double vy = v[1]*dx + v[5]*dy + v[9]*dz + v[13];
        double vz = v[2]*dx + v[6]*dy + v[10]*dz + v[14];
        double vw = v[3]*dx + v[7]*dy + v[11]*dz + v[15];

        // Projection matrix
        float[] p = new float[16]; proj.writeColumnMajor(p);
        double px = p[0]*vx + p[4]*vy + p[8]*vz + p[12]*vw;
        double py = p[1]*vx + p[5]*vy + p[9]*vz + p[13]*vw;
        double pz = p[2]*vx + p[6]*vy + p[10]*vz + p[14]*vw;
        double pw = p[3]*vx + p[7]*vy + p[11]*vz + p[15]*vw;

        if (pw <= 0.001) return null; // Behind camera

        double ndcX = px / pw;
        double ndcY = py / pw;

        double scale = mc.getWindow().getScaleFactor();
        double screenX = (ndcX + 1.0) * mc.getWindow().getWidth() / (2.0 * scale);
        double screenY = (1.0 - ndcY) * mc.getWindow().getHeight() / (2.0 * scale);

        // Boundary check
        if (screenX < 0 || screenX > mc.getWindow().getScaledWidth() || 
            screenY < 0 || screenY > mc.getWindow().getScaledHeight()) {
            return null;
        }

        return new double[]{screenX, screenY};
    }
}
