package platform.client.features.modules.misc;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;
import platform.client.utils.player.ServerUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.BooleanSetting;
import lombok.Generated;

@ModuleRegister(a = "Chat Helper", b = "Расширяет возможности чата и его настройки", c = Category.Misc)
public class ChatHelper extends Module {
    private final BooleanSetting b = new BooleanSetting("Ширина под сообщение", false);
    private final BooleanSetting c = new BooleanSetting("Автоматическое /event delay", false);
    private int d = -1;

    @Generated
    public BooleanSetting q() {
        return this.b;
    }

    public ChatHelper() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(TickEvent event) {
        int iB;
        if (this.c.c().booleanValue()) {
            if (ServerUtil.a.a()) {
                iB = ServerUtil.a.d();
            } else {
                iB = ServerUtil.d.a() ? ServerUtil.d.b() : -1;
            }
            int currentAnarchy = iB;
            if (currentAnarchy == -1) {
                this.d = 0;
                return;
            }
            if (this.d == -1) {
                this.d = currentAnarchy;
            } else if (currentAnarchy != this.d && aM_.player.tickCount >= 5) {
                aM_.player.connection.sendCommand("event delay");
                this.d = currentAnarchy;
            }
        }
    }
}








