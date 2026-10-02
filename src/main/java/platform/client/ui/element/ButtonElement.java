package platform.client.ui.element;

import platform.api.annotation.Compile;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.configs.ThemeProcessor;
import platform.client.Delta;
import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.Draw2DProcessor;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.api.module.setting.ButtonSetting;
import platform.client.utils.math.MathUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector4f;

public class ButtonElement extends Element_2<ButtonSetting> {
    @Override
    @Compile
    public boolean a(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        Vector4f vector4f = this.a;
        var setting = this.b;
        if (!MathUtil.a(mouseX, mouseY, vector4f.x, vector4f.y, vector4f.z, vector4f.w)) {
            return false;
        }
        if (!(setting instanceof ButtonSetting)) {
            throw new ClassCastException();
        }
        ((ButtonSetting) setting).k();
        return true;
    }

    static {
        NativeMethodLookup.lookup(ButtonElement.class, 9);
    }

    public ButtonElement(ButtonSetting setting) {
        super(setting);
        this.a.w = 14.0f;
    }

    @Override
    public void a(GuiGraphicsExtractor context, double mouseX, double mouseY, float delta, float extend) {
        Draw2DProcessor draw = Delta.h().d().i();
        ThemeProcessor theme = Delta.h().d().o();
        b().a(MathUtil.a(mouseX, mouseY, this.a.x, this.a.y, this.a.z, this.a.w) && extend >= 1.0f);
        b().a(0.0f, 1.0f, 0.3f, EasingList.i, delta);
        float hover = b().c();
        int text = ColorUtil.a(255, 255, 255, 255);
        draw.a(context, this.a.x, this.a.y, this.a.z, this.a.w, 4.0f, ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), ((10.0f + (20.0f * hover)) / 255.0f) * extend));
        draw.a(context, this.a.x, this.a.y, this.a.z, this.a.w, 4.0f, 0.5f, ColorUtil.a(theme.a(ThemeInfo.OUTLINE_MEDIUM).a(), theme.a(ThemeInfo.OUTLINE_MEDIUM).b() * extend));
        String label = ((ButtonSetting) this.b).i();
        Fonts.c.a(context, label, this.a.x + (this.a.z / 2.0f) - (Fonts.c.a(label, 7.0f) / 2.0f), (this.a.y + ((this.a.w - Fonts.c.a(7.0f)) / 2.0f)) - 0.5f, 7.0f, ColorUtil.a(text, extend));
    }
}


