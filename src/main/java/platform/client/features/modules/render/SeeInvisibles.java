package platform.client.features.modules.render;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;
import platform.api.module.setting.SliderSetting;
import lombok.Generated;

@ModuleRegister(a = "See Invisibles", b = "Делает невидимых игроков видимыми", c = Category.Render)
public class SeeInvisibles extends Module {
    private final SliderSetting b = new SliderSetting("Прозрачность", 0.5f, 0.1f, 1.0f, 0.1f);

    @Generated
    public SliderSetting r() {
        return this.b;
    }

    public SeeInvisibles() {
        a(this.b);
    }

    public float q() {
        return this.b.c().floatValue();
    }

    @EventTarget
    public void a(TickEvent event) {
    }
}


