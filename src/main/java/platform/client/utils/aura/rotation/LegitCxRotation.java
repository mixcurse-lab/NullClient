package platform.client.utils.aura.rotation;

import static platform.api.module.Interface.aM_;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import platform.client.Delta;
import platform.client.features.modules.combat.AuraUtil;
import platform.client.utils.aura.AuraContext;
import platform.client.utils.math.MathUtil;
import platform.client.utils.rotation.Look;
import platform.client.utils.rotation.Rotation;


public class LegitCxRotation extends AuraRotation {

    public LegitCxRotation(AuraContext ctx) {
        super(ctx);
    }

    @Override
    public String name() {
        return "Легит CX";
    }

    @Override
    public void rotate() {
        LivingEntity target = ctx.target;
        float yawToTarget = ctx.yawToTarget;
        float pitchToTarget = ctx.pitchToTarget;

        float smoothW = 0.0f;
        float smoothH = 0.0f;
        float finalPitch = AuraUtil.a(aM_.player.getXRot(), ctx.pitchHistory[Mth.clamp(10 - ctx.ticks, 0, 29)] + (smoothH * 1.5f), MathUtil.a(0.1f, 0.5f));
        float finalYaw = AuraUtil.a(aM_.player.getYRot(), yawToTarget + smoothW, MathUtil.a(0.1f, 0.4f));
        if (ctx.timers[3] >= 0.0f) {
            if (!AuraUtil.a(aM_.player.getYRot(), aM_.player.getXRot(), ctx.reach, target, true) && ctx.timers[8] <= 0.0f) {
                finalYaw = yawToTarget;
            }
            if (!AuraUtil.a(yawToTarget, finalPitch, ctx.reach, target, true) && ctx.timers[8] <= 0.0f) {
                finalPitch = pitchToTarget;
            }
            if (!AuraUtil.a(aM_.player.getYRot() + smoothW, aM_.player.getYRot() + smoothH, ctx.reach, target, true) && AuraUtil.a(aM_.player.getYRot(), aM_.player.getXRot(), ctx.reach, target, true)) {
                smoothW = Mth.clamp(smoothW, -0.05f, 0.05f);
                smoothH = Mth.clamp(smoothH, -0.05f, 0.05f);
            }
        }
        if (ctx.ticks <= 4 && ctx.timers[2] % 2.0f == 0.0f) {
            finalYaw = aM_.player.getYRot();
        }

        float yawDiff = Mth.wrapDegrees(yawToTarget - Look.b());
        boolean needAuto = ctx.targetPos != Vec3.ZERO && Math.abs(yawDiff) > 15.0f;
        float desiredYaw = (needAuto ? finalYaw : Look.b()) + smoothW;
        float desiredPitch = Look.c() + smoothH;

        if (target != null && aM_.player != null) {
            Vec3 eye = aM_.player.getEyePosition();
            AABB box = target.getBoundingBox();

            float minPitch = 90.0f;
            float maxPitch = -90.0f;
            double[] xs = {box.minX, box.maxX};
            double[] ys = {box.minY, box.maxY};
            double[] zs = {box.minZ, box.maxZ};
            for (double x : xs) {
                for (double y : ys) {
                    for (double z : zs) {
                        double dx = x - eye.x;
                        double dy = y - eye.y;
                        double dz = z - eye.z;
                        double horiz = Math.hypot(dx, dz);
                        if (horiz < 1e-6) continue;
                        float p = (float) (-Math.toDegrees(Math.atan2(dy, horiz)));
                        if (p < minPitch) minPitch = p;
                        if (p > maxPitch) maxPitch = p;
                    }
                }
            }
            if (minPitch < maxPitch) {
                float range = maxPitch - minPitch;
                float inset = range * 0.05f;
                float clampedMin = minPitch + inset;
                float clampedMax = maxPitch - inset;
                if (clampedMin > clampedMax) {
                    float mid = (minPitch + maxPitch) * 0.5f;
                    clampedMin = mid - 0.1f;
                    clampedMax = mid + 0.1f;
                }
                desiredPitch = Mth.clamp(desiredPitch, clampedMin, clampedMax);
                desiredPitch = Mth.clamp(desiredPitch, -90.0f, 90.0f);
                float minYaw = 180.0f;
                float maxYaw = -180.0f;
                for (double x : xs) {
                    for (double y : ys) {
                        for (double z : zs) {
                            double dx = x - eye.x;
                            double dz = z - eye.z;
                            float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
                            yaw = Mth.wrapDegrees(yaw);
                            float rel = Mth.wrapDegrees(yaw - desiredYaw);
                            float adj = desiredYaw + rel;
                            if (adj < minYaw) minYaw = adj;
                            if (adj > maxYaw) maxYaw = adj;
                        }
                    }
                }
                if (minYaw < maxYaw) {
                    float yawRange = maxYaw - minYaw;
                    float yawInset = yawRange * 0.05f;
                    desiredYaw = Mth.wrapDegrees(desiredYaw);
                    float relYaw = Mth.wrapDegrees(desiredYaw - (minYaw + maxYaw) * 0.5f);
                    float half = (maxYaw - minYaw) * 0.5f - yawInset;
                    if (half < 0.1f) half = 0.1f;
                    relYaw = Mth.clamp(relYaw, -half, half);
                    desiredYaw = Mth.wrapDegrees((minYaw + maxYaw) * 0.5f + relYaw);
                }
            }
        }
        Delta.h().d().k().a(new Rotation(desiredYaw, desiredPitch), 220.0f, 1, 1);
    }
}
