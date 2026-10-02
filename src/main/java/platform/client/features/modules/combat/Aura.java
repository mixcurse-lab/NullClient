package platform.client.features.modules.combat;

import net.minecraft.world.entity.LivingEntity;
import platform.api.event.GlobalEvent;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.player.WillLandEvent;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.api.module.setting.SliderSetting;
import platform.client.utils.aura.AuraProcessor;
import lombok.Generated;


@ModuleRegister(a = "Aura", b = "Автоматически атакует цели рядом с вами", c = Category.Combat)
public class Aura extends Module {

    private final ModeSetting h = new ModeSetting("Выберите тип наведения", "ФанТайм", "ФанТайм", "ФанТайм ФОВ", "Легит", "Легит CX", "Нейро", "Атлантис", "СпукиТайм", "МХ");
    private final MultiModeSetting i = new MultiModeSetting("Цели для атаки", new BooleanSetting("Без брони", true), new BooleanSetting("Враждебные мобы", false), new BooleanSetting("Животные", false), new BooleanSetting("Друзья", false), new BooleanSetting("Игроки", true));
    private final SliderSetting j = new SliderSetting("Дистанция атаки", 3.0f, 0.1f, 6.0f, 0.1f);
    private final SliderSetting k = new SliderSetting("Дополнительная дистанция", 0.5f, 0.1f, 3.0f, 0.1f);
    private final BooleanSetting l = new BooleanSetting("Только критические удары", true);
    private final BooleanSetting m = (BooleanSetting) new BooleanSetting("Адаптивные удары", true).a(() -> {
        return this.l.c();
    });
    private final MultiModeSetting n = new MultiModeSetting("Не бить когда", new BooleanSetting("Используется предмет", true), new BooleanSetting("Открыт контейнер", true), new BooleanSetting("Враг за стеной", true));
    private final BooleanSetting o = new BooleanSetting("Пробитие щита", true);
    private final BooleanSetting p = new BooleanSetting("Умный спринт", false);
    private final ModeSetting q = new ModeSetting("Приоритет цели", "Прицел", "Прицел", "Дистанция", "ХП");
    private final ModeSetting r = new ModeSetting("Коррекция движения", "Фокус", "Фокус", "Таргет", "Свободно");
    private final ModeSetting s = new ModeSetting("Визуализация цели", "Сферы", "Сферы", "Круг", "Тест");

    private final AuraProcessor processor;

    @Generated
    public ModeSetting r() {
        return this.s;
    }

    @Generated
    public LivingEntity s() {
        return this.processor.target();
    }


    public int attackTicks() {
        return this.processor.ticks();
    }


    public void incrementTicks() {
        this.processor.tick();
    }

    public Aura() {
        a(this.j, this.k, this.h, this.r, this.s, this.q, this.i, this.n, this.l, this.m, this.o, this.p);
        this.processor = new AuraProcessor(this.h, this.i, this.j, this.k, this.l, this.m, this.n, this.o, this.p, this.q, this.r);
    }

    @Override
    public void b() {
        super.b();
        this.processor.begin();
    }

    @Override
    public void c() {
        super.c();
        this.processor.suspend();
    }

    @EventTarget
    public void a(InputEvent e) {
        this.processor.handleInput(e);
    }

    @EventTarget
    public void a(GlobalEvent e) {
        this.processor.update();
    }

    @EventTarget
    public void a(WillLandEvent e) {
        this.processor.setWillLand(e);
    }

    @EventTarget
    public void a(DrawEvent event) {

    }
}
