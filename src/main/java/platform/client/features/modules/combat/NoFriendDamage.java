package platform.client.features.modules.combat;

import platform.client.Delta;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.AttackEvent;

@ModuleRegister(a = "No Friend Damage", b = "Не позволяет наносить урон вашим друзьям", c = Category.Combat)
public class NoFriendDamage extends Module {
    @EventTarget
    public void a(AttackEvent event) {
        event.a(Delta.h().d().e().d(event.b().getName().getString()));
    }
}


