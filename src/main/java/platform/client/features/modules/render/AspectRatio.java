package platform.client.features.modules.render;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.other.RatioEvent;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;

@ModuleRegister(a = "Aspect Ratio", b = "Изменяет соотношение сторон экрана", c = Category.Render)
public class AspectRatio extends Module {
    public final ModeSetting b = new ModeSetting("Соотношение сторон", "Пользовательский", "4:3", "16:9", "1:1", "16:10", "Пользовательский");
    public final SliderSetting c = (SliderSetting) new SliderSetting("Соотношение", 1.9f, 0.1f, 5.0f, 0.1f).a(() -> {
        return Boolean.valueOf(this.b.l("Пользовательский"));
    });

    public AspectRatio() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(RatioEvent event) {
        event.a(q());
    }

    public float q() {
        switch (this.b.c()) {
            case "4:3":
                return 1.3333334f;
            case "16:9":
                return 1.7777778f;
            case "1:1":
                return 1.0f;
            case "16:10":
                return 1.6f;
            default:
                return this.c.c().floatValue();
        }
    }
}



