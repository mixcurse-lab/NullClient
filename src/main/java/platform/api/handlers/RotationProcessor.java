package platform.api.handlers;

import platform.api.handlers.UseableHandler;
import platform.client.features.modules.combat.AuraUtil;
import platform.api.system.interfaces.NativeMethodLookup;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.math.MathUtil;
import platform.client.utils.player.MoveUtil;

import platform.api.system.configs.BaseProcessor;
import platform.api.event.interfaces.EventTarget;
import platform.api.event.GlobalEvent;
import platform.api.module.Interface;
import platform.api.event.events.player.InputEvent;

import platform.client.utils.rotation.Look;
import platform.client.utils.rotation.Rotation;
import platform.api.annotation.Compile;
import java.util.List;
import lombok.Generated;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.util.Mth;

public class RotationProcessor extends BaseProcessor implements Interface {
    private final Look b = new Look();
    private static a c;
    private static float d;
    private static int e;
    private static int f;
    private static int g;
    private static int h;
    private static int i;

    public enum a {
        AIM,
        RESET,
        IDLE
    }

    @Override
    @Compile
    public void setup() {
    }

    @Generated
    public Look a() {
        return this.b;
    }

    @Generated
    public static a b() {
        return c;
    }

    static {
        NativeMethodLookup.lookup(RotationProcessor.class, 36);
        c = a.IDLE;
    }

    @Generated
    public static int c() {
        return i;
    }

    @Override
    public void unSetup() {
    }

    @EventTarget
    private void a(InputEvent e2) {
        if (d()) {
            MoveUtil.a(e2, Look.b(), 10);
        }
    }

    @EventTarget
    private void a(GlobalEvent e2) {
        h++;
        if (d()) {
            if (e()) {
                a(f(), d, false);
            } else {
                a(a(f, h), d, false);
            }
        }
        if (c == a.AIM && h > g) {
            c = a.RESET;
        }
        if (c == a.RESET && a(Rotation.a(), d, true)) {
            this.b.a(false);
            c = a.IDLE;
            e = 0;
        }
    }

    private boolean d() {
        return h >= 2 && h <= g && c != a.IDLE;
    }

    public void a(int ticks) {
        i = Math.max(ticks, 0);
    }

    public void a(Rotation rotation, float turnSpeed, int lookMode, int priority) {
        a(rotation, turnSpeed, turnSpeed, lookMode, priority);
    }

    public void a(Rotation rotation, float aimSpeed, float resetSpeed, int lookMode, int priority) {
        if (e > priority) {
            return;
        }
        if (c != a.IDLE && e()) {
            rotation = f();
        }
        if (c == a.IDLE) {
            this.b.a(true);
        }
        d = resetSpeed;
        f = lookMode;
        g = b(lookMode);
        e = priority;
        c = a.AIM;
        h = 0;
        a(rotation, aimSpeed, true);
    }

    private static boolean e() {
        List<UseableHandler.a> tasks = Delta.h().d().v().b().a();
        if (tasks.isEmpty() || ((UseableHandler.a) tasks.getFirst()).d() >= 1) {
            return false;
        }
        Item item = ((UseableHandler.a) tasks.getFirst()).a().getItem();
        return item == Items.WIND_CHARGE || item == Items.ENDER_PEARL || item == Items.SNOWBALL || item == Items.SPLASH_POTION || item == Items.DRIED_KELP;
    }

    private static Rotation f() {
        return a(new Rotation(Look.b(), Look.c()));
    }

    public static Rotation a(Rotation rotation) {
        float t = aM_.player.tickCount + aM_.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float sw = ((float) ((((Math.sin(t * 0.8f) * 11.0d) + (Math.sin((((double) t) * 0.04000001502137623d) + 17.200010267039897d) * 1.5d)) + (Math.sin((((double) t) * 0.10999997113093289d) + 5.8000000238651515d) * 3.0d)) + (Math.sin((((double) t) * 0.07000004685868849d) + 12.300000009313816d) * 1.0d))) / 6.0f;
        float sh = ((float) (Math.sin(((double) t) * 0.09999998815548458d) + (Math.sin((((double) t) * 0.029999993539464892d) + 54.10000012300467d) * 0.5d))) / 4.0f;
        return new Rotation(rotation.c() + Mth.clamp(sw, -0.15f, 0.15f), rotation.d() + Mth.clamp(sh, -0.15f, 0.15f));
    }

    private static int b(int mode) {
        switch (mode) {
            case 0:
                return 1;
            case 1:
                return 9;
            case 7:
                return 30;
            default:
                return 10;
        }
    }

    private static Rotation a(int mode, int idleTicks) {
        float baseYaw = Look.b();
        float basePitch = Look.c();
        switch (mode) {
            case 1:
                float baseYaw2 = AuraUtil.a(aM_.player.getYRot(), Look.b(), MathUtil.a(0.1f, 0.45f));
                float basePitch2 = AuraUtil.a(aM_.player.getXRot(), Look.c(), MathUtil.a(0.1f, 0.45f));
                return new Rotation(baseYaw2, Mth.clamp(basePitch2, -90.0f, 90.0f));
            case 7:
                if (!Delta.h().d().t().aS().m()) {
                    idleTicks = 25;
                }
                if (idleTicks <= 20) {
                    return new Rotation(aM_.player.getYRot(), Mth.clamp(aM_.player.getXRot(), -90.0f, 90.0f));
                }
                float baseYaw3 = AuraUtil.a(aM_.player.getYRot(), Look.b(), MathUtil.a(0.2f, 0.35f));
                float basePitch3 = AuraUtil.a(aM_.player.getXRot(), Look.c(), MathUtil.a(0.2f, 0.35f));
                return new Rotation(baseYaw3, Mth.clamp(basePitch3, -90.0f, 90.0f));
            default:
                return new Rotation(baseYaw, basePitch);
        }
    }

    private boolean a(Rotation rotation, float turnSpeed, boolean bait) {
        Rotation currentRotation = new Rotation(aM_.player);
        float yawDelta = Mth.wrapDegrees(rotation.c() - currentRotation.c());
        float pitchDelta = rotation.d() - currentRotation.d();
        float totalDelta = Math.abs(yawDelta) + Math.abs(pitchDelta);
        float yawSpeed = totalDelta == 0.0f ? 0.0f : Math.abs(yawDelta / totalDelta) * turnSpeed;
        float pitchSpeed = totalDelta == 0.0f ? 0.0f : Math.abs(pitchDelta / totalDelta) * turnSpeed;
        float newYaw = aM_.player.getYRot() + Mth.clamp(yawDelta, -yawSpeed, yawSpeed);
        float newPitch = aM_.player.getXRot() + Mth.clamp(pitchDelta, -pitchSpeed, pitchSpeed);
        float newYaw2 = a(aM_.player.getYRot(), newYaw);
        float newPitch2 = Mth.clamp(a(aM_.player.getXRot(), newPitch), -90.0f, 90.0f);
        if (i > 0) {
            newPitch2 = aM_.player.getXRot();
            newYaw2 = aM_.player.getYRot();
            i--;
        }
        aM_.player.setYRot(newYaw2);
        aM_.player.setXRot(newPitch2);
        Rotation finalRotation = new Rotation(aM_.player);
        if (bait) {
            h = 0;
        }
        return finalRotation.a(rotation) < ((double) turnSpeed);
    }

    public static float a(float lastYaw, float current) {
        double sens = (((Double) aM_.options.sensitivity().get()).doubleValue() * 0.6000000498956214d) + 0.19999998556632664d;
        double gcd = sens * sens * sens * 8.0d;
        return (float) (((double) lastYaw) + (Math.ceil((((double) (current - lastYaw)) / gcd) / 0.15000006556510925d) * gcd * 0.15000006556510925d));
    }
}


