package platform.client.ui.element;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.Delta;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.configs.ThemeProcessor;
import platform.client.utils.render.Draw2DProcessor;
import platform.api.module.setting.SliderSetting;
import platform.api.annotation.Compile;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector4f;

public class SliderElement extends Element_2<SliderSetting> {
    private boolean d;

    @Override
    @Compile
    public boolean a(double mouseX, double mouseY, int button) {
        Vector4f vector4f = this.a;
        var setting = this.b;
        if (!MathUtil.a(mouseX, mouseY, vector4f.x, vector4f.y + Fonts.c.a(6.5f), vector4f.z, 11.0f)) {
            return false;
        }
        if (button == 0) {
            this.d = true;
            a(mouseX);
            return true;
        }
        if (button != 2) return false;
        ((SliderSetting) setting).b();
        return true;
    }

    @Override
    @Compile
    public boolean b(double mouseX, double mouseY, int button) {
        this.d = false;
        return false;
    }

    @Override
    @Compile
    public boolean a(double mouseX, double mouseY, double amount) {
        SliderSetting sliderSetting = (SliderSetting) this.b;
        Vector4f vector4f = this.a;
        if (!sliderSetting.e || !MathUtil.a(mouseX, mouseY, vector4f.x, vector4f.y, vector4f.z, ((vector4f.y + Fonts.c.a(6.5f)) + 9.5f) - vector4f.y)) {
            return false;
        }
        Float fC = sliderSetting.c();
        sliderSetting.a(Float.valueOf(MathUtil.b(Math.round((fC.floatValue() + (((float) Math.signum(amount)) * sliderSetting.c)) / sliderSetting.c) * sliderSetting.c, sliderSetting.a, sliderSetting.b)));
        return true;
    }

    static {
        NativeMethodLookup.lookup(SliderElement.class, 13);
    }

    public SliderElement(SliderSetting setting) {
        super(setting);
        this.a.w = 22.0f;
    }

    @Override
    public void a(GuiGraphicsExtractor context, double mouseX, double mouseY, float delta, float extend) {
        Draw2DProcessor draw = Delta.h().d().i();
        ThemeProcessor theme = Delta.h().d().o();
        this.a.w = 18.0f;
        if (this.d) {
            a(mouseX);
        }
        b().c(MathUtil.c(b().a(), (((SliderSetting) this.b).c().floatValue() - ((SliderSetting) this.b).a) / (((SliderSetting) this.b).b - ((SliderSetting) this.b).a), 1.0f));
        float progress = b().a();
        boolean hovered = MathUtil.a(mouseX, mouseY, this.a.x, this.a.y, this.a.z, this.a.w) && extend >= 1.0f;
        float current = ((SliderSetting) this.b).a + ((((SliderSetting) this.b).b - ((SliderSetting) this.b).a) * progress);
        String value = ((SliderSetting) this.b).c % 1.0f == 0.0f ? String.valueOf(Math.round(current)) : String.valueOf(Math.round(current * 100.0f) / 100.0f);
        float boxWidth = Fonts.c.a(value, 6.25f) + 6.0f;
        float boxHeight = Fonts.c.a(6.25f) + 2.0f;
        float boxX = (this.a.x + this.a.z) - boxWidth;
        a(context, Fonts.c, ((SliderSetting) this.b).i(), this.a.x, this.a.y + 0.5f, Fonts.c.a(6.5f), 6.5f, theme.a(ThemeInfo.TEXT).a(), (boxX - this.a.x) - 4.0f, hovered, extend, delta);
        draw.a(context, boxX, this.a.y, boxWidth, boxHeight, 2.0f, ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), 0.03137255f * extend));
        draw.a(context, boxX, this.a.y, boxWidth, boxHeight, 2.0f, 0.5f, ColorUtil.a(theme.a(ThemeInfo.OUTLINE_SMALL).a(), theme.a(ThemeInfo.OUTLINE_SMALL).b() * extend));
        Fonts.c.b(context, value, boxX + (boxWidth / 2.0f), (this.a.y + ((boxHeight - Fonts.c.a(6.25f)) / 2.0f)) - 0.5f, 6.25f, ColorUtil.a(theme.a(ThemeInfo.TEXT).a(), extend));
        float trackY = this.a.y + Fonts.c.a(6.5f) + 6.5f;
        draw.a(context, this.a.x, trackY, this.a.z, 3.0f, 0.75f, ColorUtil.a(ColorUtil.a(50, 52, 60, 255), extend * 0.35f));
        draw.a(context, this.a.x, trackY, this.a.z * progress, 3.0f, 0.75f, ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), extend));
        draw.a(context, this.a.x + ((this.a.z - 6.0f) * progress), (trackY + 1.5f) - 3.0f, 6.0f, 6.0f, 2.0f, ColorUtil.a(ColorUtil.a(255, 255, 255, 255), extend));
    }

    private void a(double mouseX) {
        float progress = MathUtil.b(((float) (mouseX - ((double) this.a.x))) / this.a.z, 0.0f, 1.0f);
        float value = ((SliderSetting) this.b).a + ((((SliderSetting) this.b).b - ((SliderSetting) this.b).a) * progress);
        ((SliderSetting) this.b).a(Float.valueOf(MathUtil.b(Math.round(value / ((SliderSetting) this.b).c) * ((SliderSetting) this.b).c, ((SliderSetting) this.b).a, ((SliderSetting) this.b).b)));
    }
}



