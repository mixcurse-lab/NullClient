package platform.client.features.modules.player;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.BooleanSetting;
import lombok.Generated;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;

@ModuleRegister(a = "Wind Hop", b = "Автоматически прыгает после использования заряда ветра", c = Category.Player)
public class WindHop extends Module implements Interface {
    private final BooleanSetting b = new BooleanSetting("Поворачивать голову вниз", true);
    private int c = -1;

    @Generated
    public BooleanSetting q() {
        return this.b;
    }

    public WindHop() {
        a(this.b);
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.b() && event.d() instanceof ServerboundUseItemPacket packet) {
            if (aM_.player.getItemInHand(packet.getHand()).is(Items.WIND_CHARGE)) {
                this.c = 2;
            }
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.c > 0) {
            this.c--;
        }
    }

    @EventTarget
    public void a(InputEvent event) {
        if (this.c == 0) {
            event.b(true);
            this.c = -1;
        }
    }
}





