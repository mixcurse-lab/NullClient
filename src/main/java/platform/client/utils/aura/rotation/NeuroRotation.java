package platform.client.utils.aura.rotation;

import static platform.api.module.Interface.aM_;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import platform.client.Delta;
import platform.client.features.modules.combat.AuraUtil;
import platform.client.utils.aura.AuraContext;
import platform.client.utils.math.MathUtil;
import platform.client.utils.rotation.Rotation;


public class NeuroRotation extends AuraRotation {

    private LivingEntity lastTarget;

    private float px = 0.5f;
    private float py = 0.8f;
    private float pz = 0.5f;

    private float destX = 0.5f;
    private float destY = 0.8f;
    private float destZ = 0.5f;

    private final float phA = MathUtil.a(0.0f, 6.28f);
    private final float phB = MathUtil.a(0.0f, 6.28f);
    private final float phC = MathUtil.a(0.0f, 6.28f);

    private float aimYaw;
    private float aimPitch;

    private int repickIn;
    private int reactIn;
    private int speedIn;
    private int invisTicks;
    private int overCooldown;

    private float lead = 2.0f;

    private float baseYawSpeed = 0.35f;
    private float basePitchSpeed = 0.25f;

    private float overYaw;
    private float overPitch;

    public NeuroRotation(AuraContext ctx) {
        super(ctx);
    }

    @Override
    public String name() {
        return "Нейро";
    }

    @Override
    public void rotate() {
        LivingEntity target = ctx.target;
        if (target == null || aM_.player == null) {
            return;
        }

        if (target != this.lastTarget) {
            this.lastTarget = target;
            this.repickIn = 0;
            this.speedIn = 0;
            this.invisTicks = 0;
            this.reactIn = (int) MathUtil.a(1.0f, 4.0f);
            this.overYaw = 0.0f;
            this.overPitch = 0.0f;
        }
        boolean vis = visible(target);
        this.invisTicks = vis ? 0 : this.invisTicks + 1;
        if (--this.repickIn <= 0 || this.invisTicks >= 3) {
            repick(target);
            this.invisTicks = 0;
            this.repickIn = (int) MathUtil.a(6.0f, 14.0f);
        }
        if (--this.speedIn <= 0) {
            this.baseYawSpeed = MathUtil.a(0.22f, 0.5f);
            this.basePitchSpeed = MathUtil.a(0.16f, 0.38f);
            this.speedIn = (int) MathUtil.a(20.0f, 40.0f);
        }

        float t = aM_.player.tickCount + aM_.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        this.px += (this.destX - this.px) * MathUtil.a(0.10f, 0.24f);
        this.py += (this.destY - this.py) * MathUtil.a(0.12f, 0.26f);
        this.pz += (this.destZ - this.pz) * MathUtil.a(0.10f, 0.24f);

        AABB box = target.getBoundingBox();
        Vec3 vel = target.getDeltaMovement();
        AABB pbox = box.move(vel.x * this.lead * 0.85d, vel.y * this.lead * 0.55d, vel.z * this.lead * 0.85d);

        float wx = (float) (Math.sin(t * 0.11f + this.phA) * 0.045d);
        float wy = (float) (Math.sin(t * 0.085f + this.phB) * 0.055d);
        float wz = (float) (Math.sin(t * 0.125f + this.phC) * 0.045d);

        float fx = Mth.clamp(this.px + wx, 0.02f, 0.98f);
        float fy = Mth.clamp(this.py + wy, 0.03f, 0.97f);
        float fz = Mth.clamp(this.pz + wz, 0.02f, 0.98f);

        Vec3 eye = aM_.player.getEyePosition();
        Vec3 p = new Vec3(Mth.lerp(fx, pbox.minX, pbox.maxX), Mth.lerp(fy, pbox.minY, pbox.maxY), Mth.lerp(fz, pbox.minZ, pbox.maxZ));
        Rotation r = Rotation.a(eye, p);
        this.aimYaw = r.c();
        this.aimPitch = r.d();

        if (this.overCooldown > 0) {
            this.overCooldown--;
        }
        this.overYaw *= attackingNow() ? 0.4f : 0.65f;
        this.overPitch *= attackingNow() ? 0.4f : 0.65f;
        if (Math.abs(this.overYaw) < 0.05f) {
            this.overYaw = 0.0f;
        }
        if (Math.abs(this.overPitch) < 0.05f) {
            this.overPitch = 0.0f;
        }

        float errYaw = Math.abs(Mth.wrapDegrees(this.aimYaw - aM_.player.getYRot()));
        float errPitch = Math.abs(this.aimPitch - aM_.player.getXRot());
        boolean attacking = ctx.timers[3] >= 0.0f;

        if (!attacking && this.reactIn > 0) {
            this.reactIn--;
        }

        float noiseW = ((float) (((Math.sin(t * 0.37f) * 0.6d) + (Math.sin((t * 1.13f) + 1.7f) * 0.35d) + (Math.sin((t * 2.9f) + 4.2f) * 0.1500000098386085d)) * 8.0d)) / 8.0f;
        float noiseH = ((float) (((Math.cos(t * 0.43f) * 0.5d) + (Math.sin((t * 1.7f) + 2.9f) * 0.25d)) * 8.0d)) / 8.0f;

        float wobbleY = errYaw < 1.2f ? 1.0f : MathUtil.a(0.9f, 1.1f);
        float wobbleP = errPitch < 1.2f ? 1.0f : MathUtil.a(0.9f, 1.1f);
        float yawStep = Mth.clamp(this.baseYawSpeed * (0.55f + errYaw * 0.09f) * wobbleY, 0.04f, 1.0f);
        float pitchStep = Mth.clamp(this.basePitchSpeed * (0.55f + errPitch * 0.11f) * wobbleP, 0.03f, 1.0f);
        if (attacking) {
            yawStep = Math.min(yawStep * 1.7f + 0.15f, 1.0f);
            pitchStep = Math.min(pitchStep * 1.7f + 0.15f, 1.0f);
            noiseW *= 0.35f;
            noiseH *= 0.35f;
        }
        if (!attacking && this.reactIn > 0) {
            yawStep *= 0.18f;
            pitchStep *= 0.18f;
            noiseW *= 0.5f;
            noiseH *= 0.5f;
        }

        if (!attacking && this.reactIn <= 0 && this.overCooldown <= 0 && this.overYaw == 0.0f && errYaw > 0.6f && errYaw < 5.0f && Math.random() < 0.1d) {
            this.overYaw = Math.signum(Mth.wrapDegrees(this.aimYaw - aM_.player.getYRot())) * MathUtil.a(0.4f, 1.1f);
            this.overCooldown = (int) MathUtil.a(10.0f, 18.0f);
        }
        if (!attacking && this.reactIn <= 0 && this.overCooldown <= 0 && this.overPitch == 0.0f && errPitch > 0.6f && errPitch < 4.0f && Math.random() < 0.06d) {
            this.overPitch = Math.signum(this.aimPitch - aM_.player.getXRot()) * MathUtil.a(0.25f, 0.8f);
            this.overCooldown = (int) MathUtil.a(10.0f, 18.0f);
        }

        float destYaw = this.aimYaw + this.overYaw + noiseW;
        float destPitch = Mth.clamp(this.aimPitch + this.overPitch + noiseH, -90.0f, 90.0f);
        float finalYaw = AuraUtil.a(aM_.player.getYRot(), destYaw, yawStep);
        float finalPitch = Mth.clamp(AuraUtil.a(aM_.player.getXRot(), destPitch, pitchStep), -90.0f, 90.0f);

        double dist = Math.max(eye.distanceTo(box.getCenter()), 0.5d);
        float allowedYaw = Math.max((float) Math.toDegrees(Math.atan((box.getXsize() * 0.75d) / dist)), 3.0f);
        float relYaw = Mth.wrapDegrees(finalYaw - this.aimYaw);
        if (Math.abs(relYaw) > allowedYaw) {
            finalYaw = Mth.wrapDegrees(this.aimYaw + Mth.clamp(relYaw, -allowedYaw, allowedYaw));
        }
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
                    float pc = (float) (-Math.toDegrees(Math.atan2(dy, horiz)));
                    if (pc < minPitch) minPitch = pc;
                    if (pc > maxPitch) maxPitch = pc;
                }
            }
        }
        if (minPitch < maxPitch) {
            finalPitch = Mth.clamp(finalPitch, minPitch - 2.0f, maxPitch + 2.0f);
        }

        Delta.h().d().k().a(new Rotation(finalYaw, finalPitch), attacking ? 220.0f : 180.0f + MathUtil.a(10.0f, 40.0f), 1, 1);
    }


    private boolean attackingNow() {
        return ctx.timers[3] >= 0.0f;
    }


    private boolean visible(LivingEntity target) {
        AABB box = target.getBoundingBox();
        Vec3 eye = aM_.player.getEyePosition();
        Vec3 p = new Vec3(Mth.lerp(this.px, box.minX, box.maxX), Mth.lerp(this.py, box.minY, box.maxY), Mth.lerp(this.pz, box.minZ, box.maxZ));
        Rotation r = Rotation.a(eye, p);
        return AuraUtil.a(r.c(), r.d(), ctx.reach, target, false);
    }


    private void repick(LivingEntity target) {
        AABB box = target.getBoundingBox();
        Vec3 eye = aM_.player.getEyePosition();
        this.lead = MathUtil.a(1.0f, 3.0f);
        float band = MathUtil.a(0.0f, 1.0f);
        float yMin = band < 0.45f ? 0.72f : (band < 0.8f ? 0.38f : 0.06f);
        float yMax = Math.min(yMin + (band < 0.45f ? 0.28f : 0.34f), 1.0f);
        for (int i = 0; i < 6; i++) {
            float tx = MathUtil.a(0.14f, 0.86f);
            float ty = MathUtil.a(yMin, yMax);
            float tz = MathUtil.a(0.14f, 0.86f);
            Vec3 p = new Vec3(Mth.lerp(tx, box.minX, box.maxX), Mth.lerp(ty, box.minY, box.maxY), Mth.lerp(tz, box.minZ, box.maxZ));
            Rotation rr = Rotation.a(eye, p);
            if (AuraUtil.a(rr.c(), rr.d(), ctx.reach, target, false)) {
                this.destX = tx;
                this.destY = ty;
                this.destZ = tz;
                return;
            }
        }
        this.destX = 0.5f;
        this.destZ = 0.5f;
        this.destY = Mth.clamp(((float) (eye.y - box.minY)) / Math.max((float) box.getYsize(), 1e-4f), 0.1f, 0.9f);
    }
}
