package platform.client.ui.element;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.Delta;
import platform.api.module.InterfaceC0020Opcode;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.configs.ThemeProcessor;
import platform.client.utils.render.Draw2DProcessor;
import platform.api.module.setting.BooleanSetting;
import platform.api.annotation.Compile;
import platform.api.event.events.render.DrawEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector4f;

public class BooleanElement extends Element_2<BooleanSetting> {
    @Override
    @Compile
    public boolean a(double mouseX, double mouseY, int button) {
        Vector4f vector4f = this.a;
        var setting = this.b;
        if (!MathUtil.a(mouseX, mouseY, vector4f.x, vector4f.y, vector4f.z, vector4f.w)) {
            return false;
        }
        if (button != 0) {
            if (button != 2) return false;
            ((BooleanSetting) setting).b();
            return true;
        }
        BooleanSetting booleanSetting = (BooleanSetting) setting;
        booleanSetting.a(Boolean.valueOf(!booleanSetting.c().booleanValue()));
        return true;
    }

    static {
        NativeMethodLookup.lookup(BooleanElement.class, 8);
    }

    public BooleanElement(BooleanSetting setting) {
        super(setting);
        this.a.w = 11.0f;
    }

    @Override
    public void a(GuiGraphicsExtractor context, double mouseX, double mouseY, float delta, float extend) {
        Draw2DProcessor draw = Delta.h().d().i();
        ThemeProcessor theme = Delta.h().d().o();
        b().a(((BooleanSetting) this.b).c().booleanValue());
        b().a(0.0f, 1.0f, 0.5f, EasingList.i, delta);
        float enabled = b().c();
        float disabled = 1.0f - enabled;
        float centerY = this.a.y + (this.a.w / 2.0f);
        boolean hovered = MathUtil.a(mouseX, mouseY, this.a.x, this.a.y, this.a.z, this.a.w) && extend >= 1.0f;
        a(context, Fonts.c, ((BooleanSetting) this.b).i(), this.a.x, this.a.y, this.a.w, 6.5f, theme.a(ThemeInfo.TEXT).a(), (this.a.z - 11.0f) - 4.0f, hovered, extend, delta);
        float boxX = (this.a.x + this.a.z) - 11.0f;
        float boxY = centerY - 5.5f;
        draw.a(context, boxX, boxY, 11.0f, 11.0f, 3.0f, ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), 0.039215688f * extend));
        draw.a(context, boxX, boxY, 11.0f, 11.0f, 3.0f, 0.5f, ColorUtil.a(theme.a(ThemeInfo.OUTLINE_SMALL).a(), theme.a(ThemeInfo.OUTLINE_SMALL).b() * extend));
        if (disabled > 0.0f) {
            Fonts.a.a(context, "u", boxX + ((11.0f - Fonts.a.b("u", 6.0f)) / 2.0f) + 0.25f, Fonts.a.a("u", 6.0f, centerY), 6.0f, ColorUtil.a(ColorUtil.a(InterfaceC0020Opcode.aN, 25, 25, 255), extend * disabled));
        }
        if (enabled > 0.0f) {
            Fonts.a.a(context, "m", boxX + ((11.0f - Fonts.a.b("m", 9.0f)) / 2.0f), Fonts.a.a("m", 9.0f, centerY), 9.0f, ColorUtil.a(ColorUtil.a(InterfaceC0020Opcode.bW, 220, InterfaceC0020Opcode.ap, 255), extend * enabled));
        }
    }

    @Override
    public void a(DrawEvent event, float x, float y, float width, float animation) {
        GuiGraphicsExtractor context = event.i();
        Draw2DProcessor draw = event.d();
        ThemeProcessor theme = Delta.h().d().o();
        b().a(((BooleanSetting) this.b).c().booleanValue());
        b().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        float textX = x + 19.5f;
        float toggleX = ((x + width) - 11.0f) - 5.0f;
        float toggleY = y + 2.25f;
        int primary = theme.a(ThemeInfo.PRIMARY).a();
        Fonts.a.a(context, "g", x + 5.0f, y + ((12.0f - Fonts.a.a(6.5f)) / 2.0f), 6.5f, ColorUtil.a(primary, animation));
        draw.a(context, x + 15.5f, y + 3.0f, 0.75f, 6.0f, ColorUtil.a(ColorUtil.a(InterfaceC0020Opcode.aN, InterfaceC0020Opcode.aN, InterfaceC0020Opcode.aN, 255), 0.5f * animation));
        Fonts.e.a(context, ((BooleanSetting) this.b).i(), textX, (y + ((12.0f - Fonts.e.a(6.5f)) / 2.0f)) - 0.5f, 6.5f, ColorUtil.a(-1, animation));
        float value = b().c();
        draw.a(context, toggleX, toggleY, 11.0f, 7.5f, 2.5f, ColorUtil.a(primary, value * animation));
        draw.a(context, toggleX, toggleY, 11.0f, 7.5f, 2.5f, 0.3f, ColorUtil.a(theme.a(ThemeInfo.OUTLINE_SMALL).a(), theme.a(ThemeInfo.OUTLINE_SMALL).b() * animation));
        draw.a(context, toggleX + 1.5f + (3.5f * value), toggleY + 1.5f, 4.5f, 4.5f, 1.25f, ColorUtil.a(ColorUtil.a(ColorUtil.a(InterfaceC0020Opcode.ap, InterfaceC0020Opcode.ap, InterfaceC0020Opcode.bk, 255), ColorUtil.a(255, 255, 255, 255), value), animation));
    }
}



