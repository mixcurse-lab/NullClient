package platform.client.features.modules.movement;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.event.GlobalEvent;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.ModeSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import platform.client.utils.inject.EntityInvoker;

@ModuleRegister(a = "Air Stuck", b = "Позволяет зависнуть в воздухе на месте", c = Category.Movement)
public class AirStuck extends Module implements Interface {
    private final ModeSetting b = new ModeSetting("Режим зависания", "Обычный", "Обычный", "Удаляющий игрока");
    private Vec3 c;

    public AirStuck() {
        a(this.b);
    }

    @Override
    public void b() {
        super.b();
        if (aM_.player != null) {
            this.c = aM_.player.position();
            if (this.b.l("Удаляющий игрока")) {
                aM_.player.setRemoved(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    public void c() {
        super.c();
        if (aM_.player != null && this.b.l("Удаляющий игрока")) {
            ((EntityInvoker) (Object) aM_.player).unset();
            aM_.level.addEntity(aM_.player);
            aM_.player.setPosRaw(this.c.x, this.c.y, this.c.z);
        }
        this.c = null;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.c != null) {
            aM_.player.setDeltaMovement(0.0d, 0.0d, 0.0d);
            aM_.player.setPos(this.c.x, this.c.y, this.c.z);
        }
    }

    @EventTarget
    public void a(GlobalEvent event) {
        if (aM_.player == null || !aM_.player.isRemoved()) {
            return;
        }
        ((EntityInvoker) (Object) aM_.player).baseTickInvoker();
    }

    @EventTarget
    public void a(InputEvent event) {
        if (this.b.l("Обычный")) {
            event.a(true);
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.b()) {
            if ((event.d() instanceof ServerboundMovePlayerPacket) || (event.d() instanceof ServerboundPlayerInputPacket) || (event.d() instanceof ServerboundPlayerActionPacket)) {
                event.a(true);
            }
        }
    }
}


