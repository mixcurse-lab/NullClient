package platform.client.features.modules.render;

import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.client.utils.render.colorplus.ColorPlusPreset;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;

@ModuleRegister(a = "ColorPlus", b = "Цветокоррекция мира — пресеты и тонкая настройка", c = Category.Render)
public class ColorPlus extends Module {
    public final ModeSetting preset = new ModeSetting("Пресет", "Cinematic", ColorPlusPreset.resolve2());
    public final SliderSetting silaEffekta = new SliderSetting("Сила эффекта", 1.0F, 0.0F, 1.0F, 0.01F);
    public final BooleanSetting rezkostCas = new BooleanSetting("Резкость (CAS)", true);
    public final SliderSetting ekspozitsiya = new SliderSetting("Экспозиция", 0.0F, -0.5F, 0.5F, 0.01F);
    public final SliderSetting kontrast = new SliderSetting("Контраст", 0.0F, -0.5F, 0.5F, 0.01F);
    public final SliderSetting nasyschennost = new SliderSetting("Насыщенность", 0.0F, -0.5F, 0.5F, 0.01F);
    public final SliderSetting vibrance = new SliderSetting("Vibrance", 0.0F, -0.5F, 0.5F, 0.01F);
    public final SliderSetting gamma = new SliderSetting("Гамма", 0.0F, -0.5F, 0.5F, 0.01F);
    public final SliderSetting temperatura = new SliderSetting("Температура", 0.0F, -0.5F, 0.5F, 0.01F);
    public final SliderSetting ottenokZelyonyyMadzhenta = new SliderSetting("Оттенок (зелёный/маджента)", 0.0F, -0.5F, 0.5F, 0.01F);
    public final SliderSetting intensivnostBloom = new SliderSetting("Интенсивность Bloom", 0.0F, -0.3F, 0.3F, 0.01F);
    public final SliderSetting silaRezkosti = new SliderSetting("Сила резкости", 0.0F, -0.3F, 0.3F, 0.01F);
    public final SliderSetting vinetka = new SliderSetting("Виньетка", 0.0F, -0.3F, 0.3F, 0.01F);

    public ColorPlus() {
        a(this.preset, this.silaEffekta, this.rezkostCas, this.ekspozitsiya, this.kontrast, this.nasyschennost, this.vibrance, this.gamma, this.temperatura, this.ottenokZelyonyyMadzhenta, this.intensivnostBloom, this.silaRezkosti, this.vinetka);
    }

    public ColorPlusPreset resolve() {
        return ColorPlusPreset.resolve(this.preset.c());
    }
}
