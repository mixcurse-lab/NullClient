package platform.client.features.modules.movement;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.InputEvent;

@ModuleRegister(a = "Safe Walk", b = "Не даёт упасть с края блоков", c = Category.Movement)
public class SafeWalk extends Module {
    @EventTarget
    public void a(InputEvent event) {
        b(event);
    }

    public void b(InputEvent event) {
        event.c(event.e() || (aM_.level.getBlockState(aM_.player.getBlockPosBelowThatAffectsMyMovement()).getCollisionShape(aM_.level, aM_.player.getBlockPosBelowThatAffectsMyMovement()).isEmpty() && aM_.player.onGround()));
    }
}


