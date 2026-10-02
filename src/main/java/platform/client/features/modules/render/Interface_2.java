package platform.client.features.modules.render;

import platform.client.ui.widget.Widget;
import platform.client.Delta;
import platform.api.module.Module;

import platform.api.system.configs.ThemeInfo;
import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.event.GlobalEvent;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.module.setting.BooleanSetting;
import platform.client.ui.widget.ArmorWidget;
import platform.client.ui.widget.CooldownsWidget;
import platform.client.ui.widget.EnvironmentWidget;
import platform.client.ui.widget.HotkeysWidget;
import platform.client.ui.widget.ItemsWidget;
import platform.client.ui.widget.NotificationWidget;
import platform.client.ui.widget.PotionWidget;
import platform.client.ui.widget.StaffWidget;
import platform.client.ui.widget.TargetWidget;
import platform.client.ui.widget.WatermarkWidget;

import platform.api.module.setting.ColorSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.api.system.configs.ThemeType;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;

@ModuleRegister(a = "Interface", b = "Отображает выбранные элементы интерфейса на экране", c = Category.Render)
public class Interface_2 extends Module {
    private final ColorSetting b = new ColorSetting("Глобальный цвет интерфейса", Integer.valueOf(-2092449));
    private final MultiModeSetting c = new MultiModeSetting("Элементы интерфейса", new BooleanSetting("Клавиши", true), new BooleanSetting("Таргет-худ", true), new BooleanSetting("Задержки", true), new BooleanSetting("Инфо-панель", true), new BooleanSetting("Уведомления", true), new BooleanSetting("Зелья", true), new BooleanSetting("Предметы", true), new BooleanSetting("Броня", true), new BooleanSetting("Стафф", true), new BooleanSetting("Окружение", true));
    private final ModeSetting d = new ModeSetting("Тема", Delta.h().d().o().a() == ThemeType.LIGHT ? "Светлая" : "Тёмная", "Тёмная", "Светлая").a(value -> {
        Delta.h().d().o().a("Светлая".equalsIgnoreCase(value) ? ThemeType.LIGHT : ThemeType.DARK);
    });
    private final List<Widget> h = new ArrayList();

    @Generated
    public List<Widget> q() {
        return this.h;
    }

    public Interface_2() {
        a(this.b, this.c, this.d);
        this.h.add(new ArmorWidget());
        this.h.add(new HotkeysWidget());
        this.h.add(new CooldownsWidget());
        this.h.add(new TargetWidget());
        this.h.add(new WatermarkWidget());
        this.h.add(new PotionWidget());
        this.h.add(new ItemsWidget());
        this.h.add(new NotificationWidget());
        this.h.add(new StaffWidget());
        this.h.add(new EnvironmentWidget());
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (event.b()) {
            Delta.h().d().o().a(ThemeInfo.PRIMARY).a(this.b.c().intValue());
            Delta.h().d().s().c(event);
            for (Widget widget : this.h) {
                BooleanSetting toggle = this.c.a(widget.j().j());
                if (toggle != null && toggle.c().booleanValue()) {
                    widget.a(event);
                }
            }
        }
    }

    @EventTarget
    public void a(GlobalEvent event) {
        for (Widget widget : this.h) {
            BooleanSetting toggle = this.c.a(widget.j().j());
            if (toggle != null && toggle.c().booleanValue()) {
                widget.a(event);
            }
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        for (Widget widget : this.h) {
            BooleanSetting toggle = this.c.a(widget.j().j());
            if (toggle != null && toggle.c().booleanValue()) {
                widget.a(event);
            }
        }
    }
}



