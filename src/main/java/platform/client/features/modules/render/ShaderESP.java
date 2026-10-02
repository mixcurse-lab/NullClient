package platform.client.features.modules.render;

import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.client.utils.render.ColorUtil;
import platform.api.module.setting.ColorSetting;
import platform.api.module.setting.SliderSetting;

@ModuleRegister(a = "Shader ESP", b = "Накладывает шейдерную обводку на игроков и предметы", c = Category.Render)
public class ShaderESP extends Module {
    private final SliderSetting a = new SliderSetting("Сила свечения", 0.6f, 0.0f, 1.0f, 0.05f);
    private final ColorSetting b = new ColorSetting("Цвет свечения", ColorUtil.a(255, 0, 0, 255));

    public ShaderESP() {
        a(this.a, this.b);
    }

    public SliderSetting q() {
        return this.a;
    }

    public ColorSetting r() {
        return this.b;
    }
}


