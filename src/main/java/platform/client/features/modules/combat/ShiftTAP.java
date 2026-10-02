package platform.client.features.modules.combat;

import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.AttackEvent;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.TickEvent;

import net.minecraft.world.entity.LivingEntity;

@ModuleRegister(a = "Shift TAP", b = "Автоматически приседает в момент удара по игроку", c = Category.Combat)
public class ShiftTAP extends Module {
    private int b;

    @EventTarget
    public void a(AttackEvent event) {
        if (event.b() instanceof LivingEntity) {
            this.b = 4;
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.b > 0) {
            this.b--;
        }
    }

    @EventTarget
    public void a(InputEvent event) {
        if (this.b > 0) {
            event.c(true);
        }
    }
}


