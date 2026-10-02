package platform.client.ui.element;

import platform.client.utils.input.KeyUtil;
import platform.client.utils.render.ScissorUtil;
import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.Delta;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.configs.ThemeProcessor;
import platform.client.utils.render.Draw2DProcessor;
import platform.api.module.setting.BindSetting;
import platform.api.annotation.Compile;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector4f;

public class BindElement extends Element_2<BindSetting> {
    private boolean d;

    @Override
    @Compile
    public boolean a(double mouseX, double mouseY, int button) {
        Vector4f vector4f = this.a;
        var setting = this.b;
        if (this.d) {
            ((BindSetting) setting).a(-100 + button);
            this.d = false;
            return true;
        }
        if (!MathUtil.a(mouseX, mouseY, vector4f.x, vector4f.y, vector4f.z, vector4f.w)) {
            return false;
        }
        if (button == 0) {
            this.d = true;
            return true;
        }
        if (button != 2) return false;
        ((BindSetting) setting).b();
        return true;
    }

    @Override
    @Compile
    public boolean a(int keyCode, int scanCode, int modifiers) {
        if (!this.d) return false;
        ((BindSetting) this.b).a(Integer.valueOf(keyCode == 256 ? -1 : keyCode));
        this.d = false;
        return true;
    }

    static {
        NativeMethodLookup.lookup(BindElement.class, 7);
    }

    public BindElement(BindSetting setting) {
        super(setting);
        this.a.w = 11.0f;
    }

    @Override
    public void a(GuiGraphicsExtractor context, double mouseX, double mouseY, float delta, float extend) {
        Draw2DProcessor draw = Delta.h().d().i();
        ThemeProcessor theme = Delta.h().d().o();
        b().a(this.d);
        b().a(0.0f, 1.0f, 0.4f, EasingList.p, delta);
        float centerY = this.a.y + (this.a.w / 2.0f) + 0.5f;
        boolean hovered = MathUtil.a(mouseX, mouseY, this.a.x, this.a.y, this.a.z, this.a.w) && extend >= 1.0f;
        float anim = b().c();
        float reverse = 1.0f - anim;
        String value = ((BindSetting) this.b).c().intValue() == -1 ? "None" : KeyUtil.b(((BindSetting) this.b).c().intValue());
        float total = (Fonts.c.a(value, 6.5f) * reverse) + (Fonts.c.a("...", 6.5f) * anim);
        float boxWidth = total + 8.0f;
        float boxHeight = Fonts.c.a(6.5f) + 3.0f;
        float boxX = (this.a.x + this.a.z) - boxWidth;
        float boxY = centerY - (boxHeight / 2.0f);
        float textY = (boxY + ((boxHeight - Fonts.c.a(6.5f)) / 2.0f)) - 0.75f;
        a(context, Fonts.c, ((BindSetting) this.b).i(), this.a.x, this.a.y, this.a.w, 6.5f, theme.a(ThemeInfo.TEXT).a(), (boxX - this.a.x) - 4.0f, hovered, extend, delta);
        draw.a(context, boxX, boxY, boxWidth, boxHeight, 2.0f, ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), 0.039215688f * extend));
        draw.a(context, boxX, boxY, boxWidth, boxHeight, 2.0f, 0.5f, ColorUtil.a(theme.a(ThemeInfo.OUTLINE_MEDIUM).a(), theme.a(ThemeInfo.OUTLINE_MEDIUM).b() * extend));
        context.enableScissor((int) boxX, (int) boxY, (int) (boxX + boxWidth), (int) (boxY + boxHeight));
        if (reverse > 0.0f) {
            Fonts.c.a(context, value, boxX + 4.0f, textY, 6.5f, ColorUtil.a(theme.a(ThemeInfo.TEXT).a(), extend * reverse));
        }
        if (anim > 0.0f) {
            Fonts.c.a(context, "...", boxX + 4.0f, textY, 6.5f, ColorUtil.a(theme.a(ThemeInfo.TEXT).a(), extend * anim));
        }
        context.disableScissor();
    }
}


