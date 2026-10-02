package platform.client.features.modules.combat;

import platform.inject.invokers.MinecraftInvoker;
import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.BooleanSetting;
import platform.client.utils.timer.CounterUtil;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;
import lombok.Generated;

@ModuleRegister(a = "Tape Mouse", b = "Автоматически кликает выбранной кнопкой мыши через заданные промежутки времени", c = Category.Combat)
public class TapeMouse extends Module implements Interface {
    private final SliderSetting b = new SliderSetting("Задержка между кликами", 1000.0f, 10.0f, 5000.0f, 10.0f);
    private final BooleanSetting c = new BooleanSetting("Не кликать во время еды", true);
    private final ModeSetting d = new ModeSetting("Кнопка мыши", "Правая", "Правая", "Левая");
    private final CounterUtil e = new CounterUtil();

    @Generated
    public SliderSetting q() {
        return this.b;
    }

    @Generated
    public BooleanSetting r() {
        return this.c;
    }

    @Generated
    public ModeSetting s() {
        return this.d;
    }

    @Generated
    public CounterUtil t() {
        return this.e;
    }

    public TapeMouse() {
        a(this.d, this.c, this.b);
    }

    @EventTarget
    public void a(TickEvent event) {
        if ((!this.c.c().booleanValue() || !aM_.player.isUsingItem()) && this.e.a(this.b.c().intValue())) {
            switch (this.d.c()) {
                case "Правая":
                    ((MinecraftInvoker) aM_).invokeDoItemUse();
                    break;
                case "Левая":
                    ((MinecraftInvoker) aM_).invokeDoAttack();
                    break;
            }
            this.e.b();
        }
    }
}


