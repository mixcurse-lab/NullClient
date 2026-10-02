package platform.client.features.modules.movement;

import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.SlowEvent;

import platform.api.module.setting.ModeSetting;

@ModuleRegister(a = "No Slow Down", b = "Убирает замедление при использовании предметов", c = Category.Movement)
public class NoSlowDown extends Module {
    private final ModeSetting b = new ModeSetting("Режим использования", "Vanilla", "Vanilla");

    public NoSlowDown() {
        a(this.b);
    }

    @EventTarget
    public void a(SlowEvent slow) {
        if (this.b.l("Vanilla")) {
            slow.a(true);
        }
    }
}


