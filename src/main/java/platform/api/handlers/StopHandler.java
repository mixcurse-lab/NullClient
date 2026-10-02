package platform.api.handlers;

import platform.api.handlers.Handler_2;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.handlers.BaseHandler;

import lombok.Generated;

@Handler_2
public class StopHandler extends BaseHandler implements Interface {
    private int b = -1;

    @Generated
    public int c() {
        return this.b;
    }

    public void a(int ticks) {
        this.b = ticks;
    }

    public boolean a() {
        return this.b != -1;
    }

    public boolean b() {
        return this.b == 0;
    }

    @EventTarget
    public void a(TickEvent eventTick) {
        if (this.b > 0) {
            this.b--;
        } else if (this.b == 0) {
            this.b = -1;
        }
    }

    @EventTarget(a = 0)
    public void a(InputEvent eventInput) {
        if (this.b >= 0) {
            eventInput.a(0.0f);
            eventInput.b(0.0f);
            eventInput.b(false);
        }
    }
}


