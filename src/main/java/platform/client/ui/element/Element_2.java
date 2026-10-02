package platform.client.ui.element;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.Font;
import platform.client.utils.render.AnimationUtil;
import platform.api.annotation.Compile;
import platform.api.event.events.render.DrawEvent;
import platform.api.module.setting.Setting;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector4f;

public class Element_2<SettingType extends Setting<?>> {
    private final AnimationUtil d = new AnimationUtil();
    private final AnimationUtil e = new AnimationUtil();
    protected final Vector4f a = new Vector4f();
    protected final SettingType b;
    protected float c;

    @Compile
    public boolean a(double mouseX, double mouseY, int button) {
        return false;
    }

    @Compile
    public boolean b(double mouseX, double mouseY, int button) {
        return false;
    }

    @Compile
    public boolean a(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    @Compile
    public boolean a(double mouseX, double mouseY, double amount) {
        return false;
    }

    @Compile
    public boolean a(char chr, int modifiers) {
        return false;
    }

    @Compile
    public boolean a(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    static {
        NativeMethodLookup.lookup(Element_2.class, 6);
    }

    @Generated
    public void a(float scroll) {
        this.c = scroll;
    }

    @Generated
    public AnimationUtil b() {
        return this.d;
    }

    @Generated
    public AnimationUtil c() {
        return this.e;
    }

    @Generated
    public Vector4f d() {
        return this.a;
    }

    @Generated
    public SettingType e() {
        return this.b;
    }

    @Generated
    public float f() {
        return this.c;
    }

    public Element_2() {
        this.b = null;
    }

    public Element_2(SettingType setting) {
        this.b = setting;
    }

    public boolean a() {
        return this.b != null && this.b.e().get().booleanValue();
    }

    public void a(GuiGraphicsExtractor context, double mouseX, double mouseY, float delta, float extend) {
    }

    protected void a(GuiGraphicsExtractor context, Font font, String text, float x, float y, float height, float size, int color, float maxWidth, boolean hovered, float extend, float delta) {
        font.a(context, this, text, x, (y + ((height - font.a(size)) / 2.0f)) - 0.5f, size, ColorUtil.a(color, extend), maxWidth, hovered, 30.0f, delta);
    }

    public void a(GuiGraphicsExtractor context, double mouseX, double mouseY, float delta) {
    }

    public void a(DrawEvent event, float x, float y, float width, float animation) {
    }
}



