package platform.client.features.modules.player;

import platform.inject.accessors.MinecraftAccessor;
import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;

import net.minecraft.world.item.Items;

@ModuleRegister(a = "Fast EXP", b = "Позволяет очень быстро бросать опыт", c = Category.Player)
public class FastEXP extends Module implements Interface {
    @EventTarget
    public void a(TickEvent event) {
        if (aM_.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE) {
            ((platform.inject.accessors.MinecraftAccessor) (Object) aM_).setItemUseCooldown(0);
        }
    }
}





