package platform.client.features.modules.misc;

import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;


import platform.api.module.setting.SliderSetting;
import platform.api.module.setting.StringSetting;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.Generated;

@ModuleRegister(a = "Fun Deliver", b = "Автоматическая выдача валюты по заказам FunPay", c = Category.Misc)
public class FunDeliver extends Module implements Interface {
    private final StringSetting b = (StringSetting) new StringSetting("Укажите ваш Golden-Key", "").a();
    private final SliderSetting c = new SliderSetting("Триггер цены обработки товара", 1.0f, 0.5f, 10.0f, 0.01f, true);
    private final SliderSetting d = new SliderSetting("Продавать при сумме от (кк)", 10.0f, 1.0f, 50.0f, 1.0f, true);
    private ScheduledExecutorService e = Executors.newSingleThreadScheduledExecutor();
    private Object f;

    @Override
    public void b() {
    }

    @EventTarget
    public void a(TickEvent event) {
    }

    private void t() {
    }

    @Generated
    public StringSetting q() {
        return this.b;
    }

    @Generated
    public SliderSetting r() {
        return this.c;
    }

    @Generated
    public SliderSetting s() {
        return this.d;
    }

    public FunDeliver() {
        a(this.b, this.c, this.d);
    }

    @Override
    public void c() {
        super.c();
        this.e.shutdownNow();
    }
}
