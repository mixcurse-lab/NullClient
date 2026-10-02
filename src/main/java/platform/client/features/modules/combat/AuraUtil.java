package platform.client.features.modules.combat;

import static platform.api.module.Interface.aM_;

import platform.api.handlers.RotationProcessor;
import platform.api.module.Interface;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.Generated;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.ClipContext;

public class AuraUtil implements Interface {
    @Generated
    private AuraUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static double a(Vec3 eye, Entity entity) {
        AABB box = entity.getBoundingBox();
        double cx = Mth.clamp(eye.x, box.minX, box.maxX);
        double cy = Mth.clamp(eye.y, box.minY, box.maxY);
        double cz = Mth.clamp(eye.z, box.minZ, box.maxZ);
        double dx = cx - eye.x;
        double dy = cy - eye.y;
        double dz = cz - eye.z;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    public static double a(Entity entity) {
        if (aM_.player == null) {
            return Double.POSITIVE_INFINITY;
        }
        return a(aM_.player.getEyePosition(), entity);
    }

    public static boolean a(Entity entity, double maxReach) {
        return a(entity) <= maxReach * maxReach;
    }

    public static boolean a(LivingEntity entity, double distance) {
        Vec3 eye = aM_.player.getEyePosition();
        AABB box = entity.getBoundingBox();
        double cx = Mth.clamp(eye.x, box.minX, box.maxX);
        double cy = Mth.clamp(eye.y, box.minY, box.maxY);
        double cz = Mth.clamp(eye.z, box.minZ, box.maxZ);
        Vec3 d = new Vec3(cx - eye.x, cy - eye.y, cz - eye.z);
        float yaw = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0d);
        float pitch = (float) (-Math.toDegrees(Math.atan2(d.y, Math.hypot(d.x, d.z))));
        return a(yaw, pitch, distance, entity, true);
    }

    public static boolean a(float yaw, float pitch, double distance, Entity entity, boolean throwalls) {
        if (aM_.player == null || aM_.level == null) {
            return false;
        }
        return a(aM_.player.getEyePosition(), yaw, pitch, distance, entity, throwalls);
    }

    public static boolean a(Vec3 rayOrigin, float yaw, float pitch, double distance, Entity entity, boolean throwalls) {
        if (aM_.player == null || aM_.level == null) {
            return false;
        }
        Vec3 dir = Vec3.directionFromRotation(new net.minecraft.world.phys.Vec2(pitch, yaw)).scale(distance);
        Optional<Vec3> opt = entity.getBoundingBox().contains(rayOrigin) ? Optional.of(rayOrigin) : entity.getBoundingBox().clip(rayOrigin, rayOrigin.add(dir));
        if (opt.isEmpty()) {
            return false;
        }
        if (!throwalls && aM_.level.clip(new ClipContext(rayOrigin, opt.get(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, aM_.player)).getType() != HitResult.Type.MISS) {
            return false;
        }
        return true;
    }

    public static boolean a(Vec3 from, LivingEntity entity, double reach) {
        AABB bb = entity.getBoundingBox();
        double[] t = {0.0d, 0.125d, 0.25d, 0.375d, 0.5d, 0.625d, 0.75d, 0.875d, 1.0d};
        int last = t.length - 1;
        double reachSq = reach * reach;
        for (int a = 0; a <= last; a++) {
            for (int b = 0; b <= last; b++) {
                for (int c = 0; c <= last; c++) {
                    if (a <= 0 || a >= last || b <= 0 || b >= last || c <= 0 || c >= last) {
                        Vec3 point = new Vec3(Mth.lerp(t[a], bb.minX, bb.maxX), Mth.lerp(t[b], bb.minY, bb.maxY), Mth.lerp(t[c], bb.minZ, bb.maxZ));
                        double distSq = from.distanceToSqr(point);
                        if (distSq > reachSq) {
                            continue;
                        } else {
                            Vec3 end = point.add(from.subtract(point).scale(0.05000000993895991d / Math.sqrt(distSq)));
                            if (aM_.level.clip(new ClipContext(from, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, aM_.player)).getType() == HitResult.Type.MISS) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean a(int ticks, LivingEntity target, boolean checks) {
        if (!checks && ticks >= 7 && a(target, 3.0d) && aM_.player.getAttackStrengthScale(0.5f) > 0.7f) {
            return a();
        }
        return false;
    }

    public static boolean a() {
        if (aM_.player == null || aM_.level == null) {
            return false;
        }
        double dy = (aM_.player.getDeltaMovement().y - 0.08000000049877275d) * 0.9799995837206814d;
        if (dy >= 0.0d) {
            return false;
        }
        AABB moved = aM_.player.getBoundingBox().move(0.0d, dy, 0.0d);
        AABB feet = new AABB(moved.minX, moved.minY - 0.010000001417203743d, moved.minZ, moved.maxX, moved.minY, moved.maxZ);
        return aM_.level.getBlockCollisions(aM_.player, feet).iterator().hasNext() == false;
    }

    public static Vec3 a(Vec3 eye, LivingEntity target, double reach, boolean throughWalls) {
        AABB bb = target.getBoundingBox();
        boolean mace = MaceUtil.a();
        Vec3 aimEye = (!mace || aM_.player == null) ? eye : eye.add(aM_.player.getDeltaMovement());
        double mx = (bb.minX + bb.maxX) * 0.5d;
        double mz = (bb.minZ + bb.maxZ) * 0.5d;
        Vec3 targetEye = target.getEyePosition();
        double distToTargetEye = aimEye.distanceTo(targetEye);
        Vec3 aimOrigin = aimEye;
        if (mace && distToTargetEye > 3.0d) {
            aimOrigin = new Vec3(aimEye.x, targetEye.y, aimEye.z);
        }
        double blendDist = mace ? Math.min(distToTargetEye, 3.0d) : distToTargetEye;
        double aimHeight = aimEye.y;
        if (mace && distToTargetEye > 3.0d) {
            aimHeight = targetEye.y;
        }
        double ay = Mth.lerp(Mth.clamp(blendDist / 3.0d, 0.0d, 1.0d), bb.minY, Mth.clamp(aimHeight, bb.minY, bb.maxY));
        Vec3 ideal = new Vec3(mx, ay, mz);
        List<Vec3> pts = new ArrayList<>();
        pts.add(ideal);
        double[] t = {0.0d, 0.125d, 0.25d, 0.375d, 0.5d, 0.625d, 0.75d, 0.875d, 1.0d};
        int last = t.length - 1;
        for (int a = 0; a < t.length; a++) {
            for (int b = 0; b < t.length; b++) {
                for (int c = 0; c < t.length; c++) {
                    if (a == 0 || a == last || b == 0 || b == last || c == 0 || c == last) {
                        pts.add(new Vec3(Mth.lerp(t[a], bb.minX, bb.maxX), Mth.lerp(t[b], bb.minY, bb.maxY), Mth.lerp(t[c], bb.minZ, bb.maxZ)));
                    }
                }
            }
        }
        for (double pad : new double[]{0.0d, 0.20000001551382535d}) {
            List<Vec3> visible = new ArrayList<>();
            for (Vec3 p : pts) {
                Vec3 d = p.subtract(aimOrigin);
                double len = d.length();
                double limit = reach + pad;
                if (mace || len <= limit) {
                    float traceDist = (float) (mace ? len + pad + 0.010000001417203743d : limit);
                    if (a(aimOrigin, (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0d), (float) (-Math.toDegrees(Math.atan2(d.y, Math.hypot(d.x, d.z)))), traceDist, target, false)) {
                        visible.add(p);
                    }
                }
            }
            if (!visible.isEmpty()) {
                Vec3 centroid = visible.stream().reduce(Vec3.ZERO, (v0, v1) -> {
                    return v0.add(v1);
                }).scale(1.0d / ((double) visible.size()));
                return visible.stream().min(Comparator.comparingDouble(pt -> {
                    return pt.distanceToSqr(centroid);
                })).get().subtract(aimOrigin);
            }
            if (throughWalls) {
                List<Vec3> through = new ArrayList<>();
                for (Vec3 p2 : pts) {
                    Vec3 d2 = p2.subtract(aimOrigin);
                    double len2 = d2.length();
                    double limit2 = reach + pad;
                    if (mace || len2 <= limit2) {
                        float traceDist2 = (float) (mace ? len2 + pad + 0.010000001417203743d : limit2);
                        if (a(aimOrigin, (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(d2.z, d2.x)) - 90.0d), (float) (-Math.toDegrees(Math.atan2(d2.y, Math.hypot(d2.x, d2.z)))), traceDist2, target, true)) {
                            through.add(p2);
                        }
                    }
                }
                if (!through.isEmpty()) {
                    Vec3 centroid2 = through.stream().reduce(Vec3.ZERO, (v0, v1) -> {
                        return v0.add(v1);
                    }).scale(1.0d / ((double) through.size()));
                    return through.stream().min(Comparator.comparingDouble(pt2 -> {
                        return pt2.distanceToSqr(centroid2);
                    })).get().subtract(aimOrigin);
                }
            }
        }
        return Vec3.ZERO;
    }

    public static boolean b() {
        if (aM_.player == null || aM_.player.level() == null) {
            return false;
        }
        Level world = aM_.player.level();
        BlockPos eye = BlockPos.containing(aM_.player.getEyePosition());
        FluidState fluid = world.getFluidState(eye);
        return (aM_.player.hasEffect(MobEffects.LEVITATION) || aM_.player.hasEffect(MobEffects.BLINDNESS) || fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA) || aM_.player.getAbilities().flying || aM_.player.isFallFlying() || aM_.player.onClimbable() || aM_.player.isPassenger()) ? false : true;
    }

    public static boolean c() {
        return aM_.player != null && b() && aM_.player.fallDistance > 0.0f && !aM_.player.onGround();
    }

    public static float a(float start, float end, float amount) {
        float a = Mth.clamp(amount, 0.0f, 1.0f);
        float d = Mth.wrapDegrees(end - start);
        if (Math.abs(d) < 0.5f) {
            return end;
        }
        float stepped = Mth.wrapDegrees(start + (d * a));
        float patched = RotationProcessor.a(start, stepped);
        float remaining = Mth.wrapDegrees(end - patched);
        return Math.abs(remaining) < 0.5f ? end : patched;
    }
}


