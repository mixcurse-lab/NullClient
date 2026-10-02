package platform.client.utils.rotation;

import static platform.api.module.Interface.aM_;
import platform.api.event.EventManager;

import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.event.events.player.LookEvent;
import platform.api.event.events.player.RotationEvent;

import lombok.Generated;
import net.minecraft.util.Mth;

public class Look implements Interface {
    private boolean b;
    private static float c;
    private static float d;

    @Generated
    public boolean a() {
        return this.b;
    }

    @Generated
    public static float b() {
        return c;
    }

    @Generated
    public static float c() {
        return d;
    }

    @Generated
    public static void a(float freeYaw) {
        c = freeYaw;
    }

    @Generated
    public static void b(float freePitch) {
        d = freePitch;
    }

    public Look() {
        EventManager.a(this);
    }

    @EventTarget
    private void a(LookEvent e) {
        if (this.b) {
            a(e.a, e.b);
            e.a(true);
        }
    }

    @EventTarget
    private void a(RotationEvent e) {
        if (this.b) {
            e.a(c);
            e.b(d);
        } else {
            c = e.b();
            d = e.c();
        }
    }

    public void a(boolean state) {
        if (this.b != state) {
            this.b = state;
            d();
        }
    }

    private void a(double yaw, double pitch) {
        double d0 = pitch * 0.15000001238751678d;
        double d1 = yaw * 0.15000001238751678d;
        d = (float) (((double) d) + d0);
        c = (float) (((double) c) + d1);
        d = Mth.clamp(d, -90.0f, 90.0f);
    }

    private static void d() {
        if (aM_.player != null) {
            float py = aM_.player.getYRot();
            float fy = c;
            aM_.player.setYRot(py + Mth.wrapDegrees(fy - py));
            aM_.player.setXRot(d);
        }
    }
}



