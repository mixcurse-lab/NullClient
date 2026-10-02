package platform.client.features.modules.render;

import static platform.api.module.Interface.aM_;
import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.GammaEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

@ModuleRegister(a = "Full Bright", b = "Полностью освещает мир через гамму или ночное зрение", c = Category.Render)
public class FullBright extends Module {
    private final ModeSetting b = new ModeSetting("Режим видения", "Гамма", "Гамма", "Ночное зрение");
    private final SliderSetting c = (SliderSetting) new SliderSetting("Уровень гаммы", 4.0f, 1.0f, 8.0f, 0.5f).a(() -> {
        return Boolean.valueOf(this.b.l("Гамма"));
    });

    public FullBright() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.b.l("Ночное зрение")) {
            aM_.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 1, false, false, false));
        } else {
            aM_.player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    @EventTarget
    public void a(GammaEvent event) {
        if (this.b.l("Гамма")) {
            event.a(this.c.c().floatValue());
        }
    }
}


