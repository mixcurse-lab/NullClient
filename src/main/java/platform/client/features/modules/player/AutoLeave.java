package platform.client.features.modules.player;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.player.ServerUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;
import platform.api.module.setting.BooleanSetting;

import platform.api.module.setting.MultiModeSetting;
import platform.api.module.setting.SliderSetting;
import net.minecraft.world.entity.player.Player;

@ModuleRegister(a = "Auto Leave", b = "Автоматически выходит в хаб по триггерам", c = Category.Player)
public class AutoLeave extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Условия срабатывания", new BooleanSetting("Малое ХП", true), new BooleanSetting("Игроки рядом", true));
    private final SliderSetting c = (SliderSetting) new SliderSetting("Минимум ХП", 8.0f, 1.0f, 20.0f, 0.5f).a(() -> {
        return this.b.a("Малое ХП").c();
    });
    private final SliderSetting d = (SliderSetting) new SliderSetting("Дистанция игроков", 8.0f, 8.0f, 128.0f, 1.0f).a(() -> {
        return this.b.a("Игроки рядом").c();
    });

    public AutoLeave() {
        a(this.b, this.c, this.d);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (aM_.level.dimension().identifier().toString().equals("minecraft:lobby")) {
            return;
        }
        if (ServerUtil.a.a() && ServerUtil.a.d() == -1) {
            return;
        }
        Player near = (Player) aM_.level.players().stream().filter(player -> {
            return (player == aM_.player || aM_.player.distanceToSqr(player) > ((double) (this.d.c().floatValue() * this.d.c().floatValue())) || Delta.h().d().e().d(player.getName().getString())) ? false : true;
        }).findFirst().orElse(null);
        if (((this.b.a("Малое ХП").c().booleanValue() && aM_.player.getHealth() <= this.c.c().floatValue()) || (this.b.a("Игроки рядом").c().booleanValue() && near != null)) && !ServerUtil.e()) {
            aM_.player.connection.sendCommand("hub");
            if (near != null) {
                ChatUtil.a((Object) ("Покинул анархию: рядом игрок &c" + near.getName().getString() + "&7 в &c" + Math.round(Math.sqrt(aM_.player.distanceToSqr(near))) + "&7 блоках."));
            } else {
                ChatUtil.a((Object) "Покинул &cанархию: критически мало здоровья&7.");
            }
            a();
        }
    }
}





