package platform.client.ui.element;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.Delta;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.configs.ThemeProcessor;
import platform.client.utils.render.Draw2DProcessor;
import platform.api.module.setting.ModeSetting;
import platform.client.utils.render.AnimationUtil;
import platform.api.annotation.Compile;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector4f;

public class ModeElement extends Element_2<ModeSetting> {
    private final AnimationUtil[] d;

    @Override
    @Compile
    public boolean a(double mouseX, double mouseY, int button) {
        Vector4f vector4f = this.a;
        var setting = this.b;
        if (button != 0) {
            if (button != 2 || !MathUtil.a(mouseX, mouseY, vector4f.x, vector4f.y, vector4f.z, vector4f.w)) {
                return false;
            }
            ((ModeSetting) setting).b();
            return true;
        }
        float f = vector4f.x;
        float fA = vector4f.y + Fonts.c.a(6.5f) + 5.0f;
        ModeSetting modeSetting = (ModeSetting) setting;
        for (String str : modeSetting.k()) {
            float fA2 = Fonts.c.a(str, 6.25f) + 6.0f;
            if (f + fA2 > vector4f.x + vector4f.z) {
                f = vector4f.x;
                fA += 12.0f;
            }
            if (MathUtil.a(mouseX, mouseY, f, fA, fA2, 9.0f)) {
                modeSetting.a(str);
                return true;
            }
            f += fA2 + 3.0f;
        }
        return false;
    }

    static {
        NativeMethodLookup.lookup(ModeElement.class, 11);
    }

    public ModeElement(ModeSetting setting) {
        super(setting);
        this.d = new AnimationUtil[setting.k().size()];
        for (int i = 0; i < this.d.length; i++) {
            this.d[i] = new AnimationUtil();
        }
    }

    @Override
    public void a(GuiGraphicsExtractor context, double mouseX, double mouseY, float delta, float extend) {
        Draw2DProcessor draw = Delta.h().d().i();
        ThemeProcessor theme = Delta.h().d().o();
        Fonts.c.a(context, ((ModeSetting) this.b).i(), this.a.x, this.a.y, 6.5f, ColorUtil.a(theme.a(ThemeInfo.TEXT).a(), extend));
        float x = this.a.x;
        float y = this.a.y + Fonts.c.a(6.5f) + 5.0f;
        int i = 0;
        for (String mode : ((ModeSetting) this.b).k()) {
            float width = Fonts.c.a(mode, 6.25f) + 6.0f;
            if (x + width > this.a.x + this.a.z) {
                x = this.a.x;
                y += 12.0f;
            }
            this.d[i].a(((ModeSetting) this.b).l(mode));
            this.d[i].a(0.0f, 1.0f, 0.3f, EasingList.i, delta);
            float value = this.d[i].c();
            draw.a(context, x, y, width, 9.0f, 2.0f, ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), ((5.0f + (65.0f * value)) / 255.0f) * extend));
            draw.a(context, x, y, width, 9.0f, 2.0f, 0.5f, ColorUtil.a(theme.a(ThemeInfo.OUTLINE_SMALL).a(), theme.a(ThemeInfo.OUTLINE_SMALL).b() * extend));
            int color = ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), theme.a(ThemeInfo.TEXT).a(), value);
            Fonts.c.b(context, mode, x + (width / 2.0f), (y + ((9.0f - Fonts.c.a(6.25f)) / 2.0f)) - 0.75f, 6.25f, ColorUtil.a(color, extend));
            x += width + 3.0f;
            i++;
        }
        this.a.w = (y + 9.0f) - this.a.y;
    }
}



