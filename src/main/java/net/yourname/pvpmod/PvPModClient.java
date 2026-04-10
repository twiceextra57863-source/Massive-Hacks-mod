package net.yourname.pvpmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.Matrix4f;

public class PvPModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(this::onRender);
    }

    private void onRender(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || mc.options.hudHidden) return;

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        Matrix4f proj = mc.gameRenderer.getProjectionMatrix(mc.getTickDelta());
        Matrix4f view = camera.getProjectionMatrix();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player) || player == mc.player) continue;
            if (!player.isAlive()) continue;

            // TODO: Yahan baad me "opponent only" filter lagayenge (scoreboard/team check)
            if (isOpponent(player, mc.player)) {
                drawHealthBarAboveHead(context, player, camPos, view, proj, mc);
            }
        }
    }

    private boolean isOpponent(PlayerEntity target, ClientPlayerEntity self) {
        // Abhi sabko dikhayega. Baad me scoreboard/team logic add kar sakte ho
        return true;
    }

    private void drawHealthBarAboveHead(DrawContext ctx, PlayerEntity player, Vec3d camPos, Matrix4f view, Matrix4f proj, MinecraftClient mc) {
        float health = MathHelper.clamp(player.getHealth(), 0, player.getMaxHealth());
        float maxHealth = player.getMaxHealth();
        if (maxHealth <= 0) return;

        double healthPercent = health / maxHealth;
        int barWidth = 40;
        int barHeight = 3;
        int padding = 2;

        // Head ke upar position (1.6 blocks up)
        double px = player.getX();
        double py = player.getY() + 1.6 + 0.3; // thoda extra upar
        double pz = player.getZ();

        // 3D -> 2D Screen Projection
        double[] screen = projectToScreen(px, py, pz, camPos.x, camPos.y, camPos.z, view, proj, mc);
        if (screen == null) return; // Off-screen

        int x = (int) screen[0];
        int y = (int) screen[1];

        // Draw Background
        ctx.fill(x - barWidth/2 - padding, y - padding, x + barWidth/2 + padding, y + barHeight + padding, 0x80000000);

        // Health Color (Green -> Yellow -> Red)
        int color;
        if (healthPercent > 0.6) color = 0xFF00CC00;
        else if (healthPercent > 0.3) color = 0xFFCCCC00;
        else color = 0xFFCC0000;

        int filled = (int) (barWidth * healthPercent);
        ctx.fill(x - barWidth/2, y, x - barWidth/2 + filled, y + barHeight, color);

        // Optional: HP Text
        String hpText = String.valueOf((int) health);
        ctx.drawText(mc.textRenderer, hpText, x - mc.textRenderer.getWidth(hpText)/2, y - 10, 0xFFFFFF, true);
    }

    private double[] projectToScreen(double x, double y, double z, double cx, double cy, double cz, Matrix4f view, Matrix4f proj, MinecraftClient mc) {
        double dx = x - cx, dy = y - cy, dz = z - cz;

        // View matrix apply
        float[] f = new float[16]; view.writeColumnMajor(f);
        double vx = f[0]*dx + f[4]*dy + f[8]*dz + f[12];
        double vy = f[1]*dx + f[5]*dy + f[9]*dz + f[13];
        double vz = f[2]*dx + f[6]*dy + f[10]*dz + f[14];
        double vw = f[3]*dx + f[7]*dy + f[11]*dz + f[15];

        // Projection matrix apply
        float[] p = new float[16]; proj.writeColumnMajor(p);
        double cx2 = p[0]*vx + p[4]*vy + p[8]*vz + p[12]*vw;
        double cy2 = p[1]*vx + p[5]*vy + p[9]*vz + p[13]*vw;
        double cz2 = p[2]*vx + p[6]*vy + p[10]*vz + p[14]*vw;
        double cw  = p[3]*vx + p[7]*vy + p[11]*vz + p[15]*vw;

        if (cw <= 0) return null; // Behind camera

        double ndcX = cx2 / cw;
        double ndcY = cy2 / cw;

        double scale = mc.getWindow().getScaleFactor();
        double screenX = (ndcX + 1.0) * mc.getWindow().getWidth() / (2.0 * scale);
        double screenY = (1.0 - ndcY) * mc.getWindow().getHeight() / (2.0 * scale);

        // Screen boundary check
        if (screenX < 0 || screenX > mc.getWindow().getScaledWidth() ||
            screenY < 0 || screenY > mc.getWindow().getScaledHeight()) {
            return null;
        }

        return new double[]{screenX, screenY};
    }
}
