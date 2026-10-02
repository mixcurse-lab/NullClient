package platform.client.features.modules.misc;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.PacketEvent;

import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.MultiModeSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

@ModuleRegister(a = "Anti AFK", b = "Не даёт серверу кикнуть вас за бездействие", c = Category.Player)
public class AntiAFK extends Module {
    private final ModeSetting b = new ModeSetting("Режим использования", "Обычный", "Обычный", "FunTime");
    private final MultiModeSetting c = (MultiModeSetting) new MultiModeSetting("Выполнять действия", new BooleanSetting("Прыжок", true), new BooleanSetting("Взмах", true), new BooleanSetting("Движение", true)).a(() -> {
        return Boolean.valueOf(this.b.l("Обычный"));
    });
    private final BooleanSetting d = (BooleanSetting) new BooleanSetting("Реагировать на недоступность", false).a(() -> {
        return Boolean.valueOf(this.b.l("FunTime"));
    });

    public AntiAFK() {
        a(this.b, this.c, this.d);
    }

    @EventTarget
    public void a(InputEvent event) {
        if (aM_.player.tickCount % 600 == 0) {
            if (this.b.l("Обычный")) {
                if (this.c.a("Прыжок").c().booleanValue() && aM_.player.onGround()) {
                    event.b(true);
                }
                if (this.c.a("Взмах").c().booleanValue()) {
                    aM_.player.swing(InteractionHand.MAIN_HAND);
                }
                if (this.c.a("Движение").c().booleanValue()) {
                    Delta.h().d().v().g().a(7);
                    return;
                }
                return;
            }
            if (this.b.l("FunTime") && !this.d.c().booleanValue()) {
                Delta.h().d().v().g().a(7);
            }
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (this.b.l("FunTime") && this.d.c().booleanValue() && event.c() && event.d() instanceof ClientboundSystemChatPacket messageS2CPacket) {
            if (messageS2CPacket.content().getString().equals("Данная команда недоступна в режиме AFK")) {
                Delta.h().d().v().g().a(7);
            }
        }
    }
}








