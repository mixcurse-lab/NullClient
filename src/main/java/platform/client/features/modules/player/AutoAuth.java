package platform.client.features.modules.player;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;
import platform.client.utils.player.ServerUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.StringSetting;
import lombok.Generated;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

@ModuleRegister(a = "Auto Auth", b = "Автоматически вводит пароль при авторизации и регистрации", c = Category.Player)
public class AutoAuth extends Module implements Interface {
    private final StringSetting b = (StringSetting) new StringSetting("Пароль авторизации", "").a();
    private final StringSetting c = (StringSetting) new StringSetting("Пароль регистрации", "").a();
    private String d;

    @Generated
    public StringSetting q() {
        return this.b;
    }

    @Generated
    public StringSetting r() {
        return this.c;
    }

    @Generated
    public String s() {
        return this.d;
    }

    public AutoAuth() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(PacketEvent eventPacket) {
        if (eventPacket.c() && eventPacket.d() instanceof ClientboundSystemChatPacket packet) {
            String message = packet.content().getString();
            if ((message.contains("Зарегистрируйтесь") || message.contains("/reg") || message.contains("/register")) && !this.c.c().isEmpty()) {
                this.d = "/reg " + this.c.c();
            }
            if ((message.contains("Авторизуйтесь") || message.contains("Войдите в игру") || message.contains("/login")) && !this.b.c().isEmpty()) {
                this.d = "/login " + this.b.c();
            }
        }
    }

    @EventTarget
    public void a(TickEvent tickEvent) {
        if (this.d != null) {
            if (ServerUtil.a.a() && ServerUtil.a.c()) {
                return;
            }
            aM_.player.connection.sendChat(this.d);
            this.d = null;
        }
    }
}





