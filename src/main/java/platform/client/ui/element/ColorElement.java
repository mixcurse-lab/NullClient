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
import platform.client.utils.render.pipeline.DeltaRenderUtil;
import platform.api.module.setting.ColorSetting;
import platform.api.annotation.Compile;
import java.awt.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;

public class ColorElement extends Element_2<ColorSetting> {
    private final Vector4f d;
    private final Vector4f e;
    private final Vector4f f;
    private final Vector4f g;
    private float h;
    private float i;
    private float j;
    private float k;
    private DragMode l;
    private boolean m;

    enum DragMode {
        NONE,
        AREA,
        HUE,
        ALPHA
    }

    @Override
    @Compile
    public boolean a(double mouseX, double mouseY, int button) {
        Vector4f vector4f = this.a;
        Vector4f vector4f2 = this.e;
        Vector4f vector4f3 = this.f;
        Vector4f vector4f4 = this.g;
        if (MathUtil.a(mouseX, mouseY, (vector4f.x + vector4f.z) - 11.0f, (vector4f.y + (vector4f.w / 2.0f)) - 5.0f, 11.0f, 11.0f)) {
            this.m = !this.m;
            return true;
        }
        if (!this.m || button != 0) return false;
        if (MathUtil.a(mouseX, mouseY, vector4f2.x, vector4f2.y, vector4f2.z, vector4f2.w)) {
            this.l = DragMode.AREA;
            a(mouseX, mouseY);
            return true;
        }
        if (MathUtil.a(mouseX, mouseY, vector4f3.x, vector4f3.y, vector4f3.z, vector4f3.w)) {
            this.l = DragMode.HUE;
            a(mouseX, mouseY);
            return true;
        }
        if (!MathUtil.a(mouseX, mouseY, vector4f4.x, vector4f4.y, vector4f4.z, vector4f4.w)) return false;
        this.l = DragMode.ALPHA;
        a(mouseX, mouseY);
        return true;
    }

    @Override
    @Compile
    public boolean b(double mouseX, double mouseY, int button) {
        this.l = DragMode.NONE;
        return false;
    }

    static {
        NativeMethodLookup.lookup(ColorElement.class, 10);
    }

    public ColorElement(ColorSetting setting) {
        super(setting);
        this.d = new Vector4f();
        this.e = new Vector4f();
        this.f = new Vector4f();
        this.g = new Vector4f();
        this.l = DragMode.NONE;
        this.a.w = 11.0f;
        g();
    }

    @Override
    public void a(GuiGraphicsExtractor context, double mouseX, double mouseY, float delta, float extend) {
        Draw2DProcessor draw = Delta.h().d().i();
        ThemeProcessor theme = Delta.h().d().o();
        float centerY = this.a.y + (this.a.w / 2.0f) + 0.5f;
        float boxX = (this.a.x + this.a.z) - 11.0f;
        float boxY = centerY - 5.5f;
        this.e.set(this.a.x + this.a.z + 6.0f + 5.0f, boxY, 56.0f, 56.0f);
        this.f.set(this.e.x + 56.0f + 5.0f, this.e.y, 4.0f, 56.0f);
        this.g.set(this.f.x + 4.0f + 5.0f, this.e.y, 4.0f, 56.0f);
        this.d.set(this.e.x - 5.0f, this.e.y - 5.0f, 84.0f, 66.0f);
        boolean hovered = MathUtil.a(mouseX, mouseY, this.a.x, this.a.y, this.a.z, this.a.w) && extend >= 1.0f;
        if (extend < 1.0f) {
            this.m = false;
        }
        a(context, Fonts.c, ((ColorSetting) this.b).i(), this.a.x, this.a.y, this.a.w, 6.5f, theme.a(ThemeInfo.TEXT).a(), (boxX - this.a.x) - 4.0f, hovered, extend, delta);
        draw.a(context, boxX, boxY, 11.0f, 11.0f, 2.0f, ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), 0.039215688f * extend));
        draw.a(context, boxX, boxY, 11.0f, 11.0f, 2.0f, 0.5f, ColorUtil.a(theme.a(ThemeInfo.OUTLINE_MEDIUM).a(), theme.a(ThemeInfo.OUTLINE_MEDIUM).b() * extend));
        Fonts.a.a(context, "J", boxX + ((11.0f - Fonts.a.b("J", 6.5f)) / 2.0f), Fonts.a.a("J", 6.5f, centerY), 6.5f, ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), extend));
        draw.a(context, ((boxX + 11.0f) - 3.0f) - 1.25f, ((boxY + 11.0f) - 3.0f) - 1.25f, 3.0f, 3.0f, 0.5f, ColorUtil.a(((ColorSetting) this.b).c().intValue(), extend));
    }

    @Override
    public void a(GuiGraphicsExtractor context, double mouseX, double mouseY, float delta) {
        b().a(this.m);
        b().a(0.0f, 1.0f, 0.25f, EasingList.p, delta);
        float anim = EasingList.p.ease(b().c());
        if (anim > 0.0f) {
            Draw2DProcessor draw = Delta.h().d().i();
            ThemeProcessor theme = Delta.h().d().o();
            a(mouseX, mouseY);
            int hueColor = Color.HSBtoRGB(this.h, 1.0f, 1.0f);
            int rgb = ((ColorSetting) this.b).c().intValue() & 16777215;
            int handle = ColorUtil.a(16777215, anim);
            int background = ColorUtil.a(ColorUtil.a(theme.a(ThemeInfo.BACKGROUND_GUI).a(), theme.a(ThemeInfo.PRIMARY).a(), 0.05f), 0.65f * anim);
            float scale = 0.85f + (0.15f * EasingList.s.ease(b().c()));
            float centerX = this.d.x + (this.d.z / 2.0f);
            float centerY = this.d.y + (this.d.w / 2.0f);
            context.pose().pushMatrix();
            context.pose().translate(centerX, centerY + ((1.0f - anim) * 6.0f));
            context.pose().scale(scale, scale);
            context.pose().translate(-centerX, -centerY);
            draw.a(context, this.d.x, this.d.y, this.d.z, this.d.w, 4.0f, background, anim);
            draw.a(context, this.d.x, this.d.y, this.d.z, this.d.w, 4.0f, 0.5f, ColorUtil.a(theme.a(ThemeInfo.OUTLINE_MEDIUM).a(), theme.a(ThemeInfo.OUTLINE_MEDIUM).b() * anim));
            draw.a(context, this.e.x, this.e.y, this.e.z, this.e.w, 2.0f, ColorUtil.a(16777215, anim), ColorUtil.a(hueColor, anim), ColorUtil.a(0, anim), ColorUtil.a(0, anim));
            draw.a(context, this.e.x, this.e.y, this.e.z, this.e.w, 2.0f, 0.5f, ColorUtil.a(theme.a(ThemeInfo.OUTLINE_SMALL).a(), theme.a(ThemeInfo.OUTLINE_SMALL).b() * anim));
            float cursorX = MathUtil.b(this.e.x + (this.i * this.e.z), this.e.x + 2.0f, (this.e.x + this.e.z) - 2.0f);
            float cursorY = MathUtil.b(this.e.y + ((1.0f - this.j) * this.e.w), this.e.y + 2.0f, (this.e.y + this.e.w) - 2.0f);
            draw.a(context, cursorX - 2.0f, cursorY - 2.0f, 4.0f, 4.0f, 2.0f, 0.5f, handle);
            float knob = this.f.z + 2.0f;
            DeltaRenderUtil.queueTexture(context, Identifier.fromNamespaceAndPath("delta", "pictures/color.png"), this.f.x, this.f.y, this.f.z, this.f.w, 0.0f, 0.0f, 1.0f, 1.0f, ColorUtil.a(16777215, anim), this.f.z / 4.0f, null);
            draw.a(context, this.f.x - 1.0f, (this.f.y + (this.h * this.f.w)) - 0.5f, knob, 1.0f, handle);
            DeltaRenderUtil.queueTexture(context, Identifier.fromNamespaceAndPath("delta", "pictures/opacity.png"), this.g.x, this.g.y, this.g.z, this.g.w, 0.0f, 0.0f, 1.0f, 1.0f, ColorUtil.a(16777215, 0.019607844f * anim), this.g.z / 4.0f, null);
            draw.a(context, this.g.x, this.g.y, this.g.z, this.g.w, this.g.z / 4.0f, ColorUtil.a(rgb, anim), ColorUtil.a(rgb, anim), ColorUtil.a(rgb, 0.0f), ColorUtil.a(rgb, 0.0f));
            draw.a(context, this.g.x - 1.0f, (this.g.y + ((1.0f - this.k) * this.g.w)) - 0.5f, knob, 1.0f, handle);
            context.pose().popMatrix();
        }
    }

    private void a(double mouseX, double mouseY) {
        switch (this.l) {
            case DragMode.NONE:
                return;
            case DragMode.AREA:
                this.i = MathUtil.b(((float) (mouseX - ((double) this.e.x))) / this.e.z, 0.0f, 1.0f);
                this.j = 1.0f - MathUtil.b(((float) (mouseY - ((double) this.e.y))) / this.e.w, 0.0f, 1.0f);
                break;
            case DragMode.HUE:
                this.h = MathUtil.b(((float) (mouseY - ((double) this.f.y))) / this.f.w, 0.0f, 1.0f);
                break;
            case DragMode.ALPHA:
                this.k = 1.0f - MathUtil.b(((float) (mouseY - ((double) this.g.y))) / this.g.w, 0.0f, 1.0f);
                break;
        }
        ((ColorSetting) this.b).a(Integer.valueOf(ColorUtil.a(Color.HSBtoRGB(this.h, this.i, this.j), this.k)));
    }

    private void g() {
        int color = ((ColorSetting) this.b).c().intValue();
        float[] hsb = Color.RGBtoHSB((color >> 16) & 255, (color >> 8) & 255, color & 255, (float[]) null);
        this.h = hsb[0];
        this.i = hsb[1];
        this.j = hsb[2];
        this.k = ((color >> 24) & 255) / 255.0f;
    }
}



