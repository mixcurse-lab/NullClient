package platform.client.features.modules.player;

import static platform.api.module.Interface.aM_;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.module.Module;
import platform.client.utils.render.ColorUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.AttackEvent;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.SliderSetting;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import platform.inject.accessors.ConnectionAccessor;

@ModuleRegister(a = "Fake Lags", b = "Задерживает отправку пакетов, имитируя лаги на сервере", c = Category.Player)
public class FakeLags extends Module implements Interface {
    private final SliderSetting b = new SliderSetting("Задержка симуляции", 20.0f, 1.0f, 40.0f, 1.0f);
    private final BooleanSetting c = new BooleanSetting("Отображать серв-позицию", false);
    private final Queue<Packet<?>> d = new ConcurrentLinkedQueue();
    private int e;
    private int f;
    private Vec3 g;

    public FakeLags() {
        a(this.b, this.c);
    }

    @Override
    public void b() {
        super.b();
        this.d.clear();
        this.e = 0;
        this.f = 0;
        this.g = null;
    }

    @Override
    public void c() {
        super.c();
        q();
        this.g = null;
    }

    @EventTarget
    public void a(AttackEvent event) {
        this.f = 2;
        q();
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (aM_.player == null) {
            return;
        }
        if (event.c() && event.d() instanceof ClientboundSetEntityMotionPacket velocity) {
            if (velocity.id() == aM_.player.getId()) {
                q();
                return;
            }
            return;
        }
        if (event.b()) {
            if (this.f > 0 || a(event.d())) {
                q();
            } else {
                this.d.offer(event.d());
                event.a(true);
            }
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.f > 0) {
            this.f--;
        }
        int i = this.e + 1;
        this.e = i;
        if (i >= this.b.c().intValue() && !this.d.isEmpty()) {
            q();
            this.e = 0;
        }
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (event.c() && this.c.c().booleanValue() && this.g != null) {
            event.e().a(event.h(), aM_.player.getBoundingBox().move(this.g.subtract(aM_.player.position())), ColorUtil.a(255, 255, 255, InterfaceC0020Opcode.aN), 0.75f);
        }
    }

    private boolean a(Packet<?> packet) {
        return (packet instanceof ServerboundInteractPacket) || (packet instanceof ServerboundChatPacket) || (packet instanceof ServerboundSetCarriedItemPacket) || (packet instanceof ServerboundSwingPacket) || (packet instanceof ServerboundUseItemOnPacket) || (packet instanceof ServerboundUseItemPacket) || (packet instanceof ServerboundContainerClickPacket);
    }

    private void q() {
        ConnectionAccessor connection = (ConnectionAccessor) aM_.player.connection.getConnection();
        this.d.forEach(packet -> {
            connection.sendWithoutEvent(packet, null, true);
        });
        this.d.clear();
        this.g = aM_.player.position();
    }
}





