package platform.client.features.modules.movement;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;

import net.minecraft.world.effect.MobEffects;

@ModuleRegister(a = "Sprint", b = "Автоматически включает спринт при движении", c = Category.Movement)
public class Sprint extends Module {
    @EventTarget
    public void a(TickEvent event) {
        aM_.player.setSprinting(aM_.player.input.getMoveVector().y > 0.0f && !aM_.player.hasEffect(MobEffects.BLINDNESS) && (aM_.player.getAbilities().invulnerable || aM_.player.getFoodData().getFoodLevel() > 6));
    }
}


