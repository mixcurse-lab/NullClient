package platform.client.features.modules.misc;

import platform.api.handlers.Handler_2;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.math.MathUtil;

import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.handlers.BaseHandler;
import platform.client.utils.rotation.Rotation;

import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;

@Handler_2
public class AFKHandler extends BaseHandler implements Interface {
    private int b = -1;

    @Generated
    public int b() {
        return this.b;
    }

    public void a(int ticks) {
        this.b = ticks;
    }

    public boolean a() {
        return this.b > 0;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.b > 0) {
            this.b--;
        }
    }

    @EventTarget
    public void a(InputEvent e) {
        if (this.b > 0) {
            e.a(ThreadLocalRandom.current().nextBoolean() ? 1.0f : -1.0f);
            e.b(ThreadLocalRandom.current().nextBoolean() ? 1.0f : -1.0f);
            Delta.h().d().k().a(new Rotation(aM_.player.getYRot() + MathUtil.a(-2.0f, 2.0f), MathUtil.b(aM_.player.getXRot() + MathUtil.a(-1.0f, 1.0f), -90.0f, 90.0f)), 150.0f, 10, 1);
        }
    }
}








