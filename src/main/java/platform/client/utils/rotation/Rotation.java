package platform.client.utils.rotation;

import static platform.api.module.Interface.aM_;
import platform.client.utils.rotation.Look;

import platform.api.module.Interface;

import java.util.Objects;
import lombok.Generated;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import platform.inject.accessors.LocalPlayerAccessor;

public class Rotation implements Interface {
    private float b;
    private float c;

    @Generated
    public void a(float yaw) {
        this.b = yaw;
    }

    @Generated
    public void b(float pitch) {
        this.c = pitch;
    }

    @Generated
    public Rotation() {
    }

    @Generated
    public Rotation(float yaw, float pitch) {
        this.b = yaw;
        this.c = pitch;
    }

    @Generated
    public float c() {
        return this.b;
    }

    @Generated
    public float d() {
        return this.c;
    }

    public Rotation(Entity entity) {
        this.b = entity.getYRot();
        this.c = entity.getXRot();
    }

    public double a(Rotation targetRotation) {
        if (targetRotation == null) {
            return 0.0d;
        }
        double yawDelta = Mth.wrapDegrees(targetRotation.c() - this.b);
        double pitchDelta = Mth.wrapDegrees(targetRotation.d() - this.c);
        return Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
    }

    public static Rotation a() {
        if (aM_.player == null) {
            return new Rotation(Look.b(), Look.c());
        }
        float py = aM_.player.getYRot();
        float fy = Look.b();
        return new Rotation(py + Mth.wrapDegrees(fy - py), Look.c());
    }

    public static Rotation a(Vec3 eye, Vec3 point) {
        Vec3 diff = point.subtract(eye);
        double dist = Math.sqrt((diff.x * diff.x) + (diff.z * diff.z));
        float yaw = ((float) Math.toDegrees(Math.atan2(diff.z, diff.x))) - 90.0f;
        float pitch = (float) (-Math.toDegrees(Math.atan2(diff.y, dist)));
        return new Rotation(Mth.wrapDegrees(yaw), Mth.clamp(pitch, -90.0f, 90.0f));
    }

    public static Rotation b() {
        LocalPlayerAccessor accessor = (LocalPlayerAccessor) (Object) aM_.player;
        return new Rotation(((LocalPlayerAccessor) Objects.requireNonNull(accessor)).getLastYaw(), accessor.getLastPitch());
    }
}



