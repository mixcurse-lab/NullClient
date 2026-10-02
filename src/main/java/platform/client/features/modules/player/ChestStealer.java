package platform.client.features.modules.player;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.other.ContainerEvent;
import platform.api.event.events.other.RayTraceEvent;

import platform.api.module.setting.BooleanSetting;
import platform.client.utils.timer.CounterUtil;
import lombok.Generated;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;

@ModuleRegister(a = "Chest Stealer", b = "Автоматически забирает предметы из открытого сундука", c = Category.Player)
public class ChestStealer extends Module implements Interface {
    private final BooleanSetting b = new BooleanSetting("Игнорировать сущностей", true);
    private final BooleanSetting c = new BooleanSetting("Авто-закрытие сундука", true);
    private final CounterUtil d = new CounterUtil();

    @Generated
    public BooleanSetting q() {
        return this.b;
    }

    @Generated
    public BooleanSetting r() {
        return this.c;
    }

    @Generated
    public CounterUtil s() {
        return this.d;
    }

    public ChestStealer() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(RayTraceEvent event) {
        if (this.b.c().booleanValue()) {
            event.a(true);
        }
    }

    @EventTarget
    public void a(ContainerEvent event) {
        Slot target;
        if (event.h() == ContainerEvent.Phase.POST) {
            if (((event.b() instanceof ContainerScreen) || (event.b() instanceof ShulkerBoxScreen)) && (target = event.e().stream().filter(slot -> {
                return slot.container != aM_.player.getInventory();
            }).filter((v0) -> {
                return v0.hasItem();
            }).findFirst().orElse(null)) != null && this.d.a(5L, 5L)) {
                aM_.gameMode.handleContainerInput(event.c().containerId, target.index, 0, ContainerInput.QUICK_MOVE, aM_.player);
                boolean empty = event.e().stream().filter(slot2 -> {
                    return slot2.container != aM_.player.getInventory();
                }).noneMatch(slot3 -> {
                    return slot3.hasItem() && slot3 != target;
                });
                if (empty && this.c.c().booleanValue()) {
                    aM_.player.closeContainer();
                }
                this.d.b();
            }
        }
    }
}





