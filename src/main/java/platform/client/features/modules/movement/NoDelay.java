package platform.client.features.modules.movement;

import platform.api.module.Interface;

import platform.inject.accessors.LivingEntityAccessor;
import platform.inject.accessors.MinecraftAccessor;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;
import platform.api.module.setting.BooleanSetting;

import platform.api.module.setting.MultiModeSetting;
import net.minecraft.world.item.BlockItem;

@ModuleRegister(a = "No Delay", b = "Убирает задержку у выбранных действий", c = Category.Movement)
public class NoDelay extends Module {
    public final MultiModeSetting b = new MultiModeSetting("Отключить задержку на", new BooleanSetting("Поставку блоков", true), new BooleanSetting("Прыжки", true));

    public NoDelay() {
        a(this.b);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.b.a("Поставку блоков").c().booleanValue() && (aM_.player.getMainHandItem().getItem() instanceof BlockItem) && !Delta.h().d().t().aS().m()) {
            ((MinecraftAccessor) (Object) aM_).setItemUseCooldown(0);
        }
        if (this.b.a("Прыжки").c().booleanValue()) {
            ((LivingEntityAccessor) (Object) aM_.player).setJumpingCooldown(0);
        }
    }
}


