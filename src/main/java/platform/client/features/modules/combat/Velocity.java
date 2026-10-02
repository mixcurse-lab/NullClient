package platform.client.features.modules.combat;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;
import platform.client.utils.rotation.Look;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.PacketEvent;

import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ModeSetting;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.util.Mth;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;

@ModuleRegister(a = "Velocity", b = "Не позволяет игрокам откидывать вас", c = Category.Combat)
public class Velocity extends Module {
    private final ModeSetting b = new ModeSetting("Режим анти-отбрасывания", "Легитный", "Обычный", "Легитный");
    private final BooleanSetting c = (BooleanSetting) new BooleanSetting("Прыгать в легит", true).a(() -> {
        return Boolean.valueOf(this.b.l("Легитный"));
    });
    private final BooleanSetting d = (BooleanSetting) new BooleanSetting("Легитный", true).a(() -> {
        return Boolean.valueOf(this.b.l("Легитный"));
    });
    private Vec3 e = Vec3.ZERO;
    private int f;

    public Velocity() {
        a(this.b, this.c, this.d);
    }

    @Override
    public void c() {
        super.c();
        this.e = Vec3.ZERO;
        this.f = 0;
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (!event.c() || aM_.player == null) {
            return;
        }
        if (event.d() instanceof ClientboundDamageEventPacket damage && damage.entityId() == aM_.player.getId()) {
            DamageSource source = damage.getSource(aM_.level);
            boolean player = source.getEntity() instanceof Player;
            this.f = player ? aM_.player.tickCount : 0;
            if (!player) {
                this.e = Vec3.ZERO;
            }
        }
        if (event.d() instanceof ClientboundSetEntityMotionPacket packet && packet.id() == aM_.player.getId()) {
            if (!this.b.l("Обычный")) {
                this.e = new Vec3(packet.movement().x, 0.0d, packet.movement().z);
            } else {
                event.a(true);
            }
        }
    }

    @EventTarget
    public void a(InputEvent event) {
        float f;
        float f2;
        if (aM_.player.hurtTime <= 0) {
            this.e = Vec3.ZERO;
            return;
        }
        if (!this.b.l("Легитный") || this.e.lengthSqr() == 0.0d || aM_.player.tickCount - this.f > 10) {
            return;
        }
        double angle = Mth.wrapDegrees((Math.toDegrees(Math.atan2(-this.e.z, -this.e.x)) - 90.0d) - ((double) Look.b()));
        if (this.d.c().booleanValue() && aM_.options.keyUp.isDown() && Math.abs(angle) >= 140.0d) {
            return;
        }
        if (angle <= -45.0d || angle >= 45.0d) {
            f = (angle > 135.0d || angle < -135.0d) ? -1.0f : 0.0f;
        } else {
            f = 1.0f;
        }
        event.a(f);
        if (angle < 45.0d || angle > 135.0d) {
            f2 = (angle > -45.0d || angle < -135.0d) ? 0.0f : 1.0f;
        } else {
            f2 = -1.0f;
        }
        event.b(f2);
        event.b(this.c.c().booleanValue() && aM_.player.onGround());
    }
}


