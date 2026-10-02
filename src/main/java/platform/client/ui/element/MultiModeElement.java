package platform.client.ui.element;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.Delta;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.api.module.setting.Setting;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.configs.ThemeProcessor;
import platform.client.utils.render.Draw2DProcessor;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.client.utils.render.AnimationUtil;
import platform.api.annotation.Compile;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector4f;

public class MultiModeElement extends Element_2<MultiModeSetting> {
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
            ((MultiModeSetting) setting).c().forEach((Setting s) -> s.b());
            return true;
        }
        float f = vector4f.x;
        float fA = vector4f.y + Fonts.c.a(6.5f) + 5.0f;
        List<BooleanSetting> listC = ((MultiModeSetting) setting).c();
        for (BooleanSetting booleanSetting2 : listC) {
            float fA2 = Fonts.c.a(booleanSetting2.i(), 6.25f) + 6.0f;
            if (f + fA2 > vector4f.x + vector4f.z) {
                f = vector4f.x;
                fA += 12.0f;
            }
            if (MathUtil.a(mouseX, mouseY, f, fA, fA2, 9.0f)) {
                booleanSetting2.a(Boolean.valueOf(!booleanSetting2.c().booleanValue()));
                return true;
            }
            f += fA2 + 3.0f;
        }
        return false;
    }

    static {
        NativeMethodLookup.lookup(MultiModeElement.class, 12);
    }

    public MultiModeElement(MultiModeSetting setting) {
        super(setting);
        this.d = new AnimationUtil[setting.c().size()];
        for (int i = 0; i < this.d.length; i++) {
            this.d[i] = new AnimationUtil();
        }
    }

    @Override
    public void a(GuiGraphicsExtractor context, double mouseX, double mouseY, float delta, float extend) {
        Draw2DProcessor draw = Delta.h().d().i();
        ThemeProcessor theme = Delta.h().d().o();
        long selectedCount = ((MultiModeSetting) this.b).c().stream().filter(v0 -> v0.c()).count();
        long totalCount = ((MultiModeSetting) this.b).c().size();
        String counter = selectedCount + " \u0438\u0437 " + totalCount;
        float counterWidth = Fonts.c.a(counter, 6.5f);
        boolean hovered = MathUtil.a(mouseX, mouseY, this.a.x, this.a.y, this.a.z, this.a.w) && extend >= 1.0f;
        a(context, Fonts.c, ((MultiModeSetting) this.b).i(), this.a.x, this.a.y, Fonts.c.a(6.5f) + 1.0f, 6.5f, theme.a(ThemeInfo.TEXT).a(), (this.a.z - counterWidth) - 4.0f, hovered, extend, delta);
        Fonts.c.a(context, counter, (this.a.x + this.a.z) - counterWidth, this.a.y + 0.25f, 6.5f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), extend));
        float x = this.a.x;
        float y = this.a.y + Fonts.c.a(6.5f) + 5.0f;
        int i = 0;
        for (BooleanSetting mode : ((MultiModeSetting) this.b).c()) {
            float width = Fonts.c.a(mode.i(), 6.25f) + 6.0f;
            if (x + width > this.a.x + this.a.z) {
                x = this.a.x;
                y += 12.0f;
            }
            this.d[i].a(mode.c().booleanValue());
            this.d[i].a(0.0f, 1.0f, 0.3f, EasingList.i, delta);
            float value = this.d[i].c();
            draw.a(context, x, y, width, 9.0f, 2.0f, ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), ((5.0f + (65.0f * value)) / 255.0f) * extend));
            draw.a(context, x, y, width, 9.0f, 2.0f, 0.5f, ColorUtil.a(theme.a(ThemeInfo.OUTLINE_SMALL).a(), theme.a(ThemeInfo.OUTLINE_SMALL).b() * extend));
            int color = ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), theme.a(ThemeInfo.TEXT).a(), value);
            Fonts.c.b(context, mode.i(), x + (width / 2.0f), (y + ((9.0f - Fonts.c.a(6.25f)) / 2.0f)) - 0.75f, 6.25f, ColorUtil.a(color, extend));
            x += width + 3.0f;
            i++;
        }
        this.a.w = (y + 9.0f) - this.a.y;
    }
}



