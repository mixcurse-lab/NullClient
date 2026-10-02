package platform.client.features.modules.render;

import platform.api.module.Category;
import platform.client.Delta;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.system.configs.ThemeInfo;
import platform.api.event.events.player.HandEvent;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.NoiseShader;
import platform.api.module.setting.SliderSetting;

import static platform.api.module.Interface.aM_;

@ModuleRegister(a = "Hands Shader", b = "Накладывает шейдер на руку от первого лица", c = Category.Render)
public class HandsShader extends Module {
    private final SliderSetting b = new SliderSetting("Непрозрачность", 0.6f, 0.0f, 1.0f, 0.05f);

    public HandsShader() {
        a(this.b);
    }

    @EventTarget
    public void a(HandEvent event) {
        NoiseShader shader = NoiseShader.getInstance();
        if (aM_.options.getCameraType().isFirstPerson()) {
            if (event.b()) {
                shader.e();
            }
            if (event.c()) {
                float[] color = ColorUtil.a(Delta.h().d().o().a(ThemeInfo.PRIMARY).a());
                color[3] = this.b.c().floatValue();
                shader.a(color);
            }
        }
    }
}


