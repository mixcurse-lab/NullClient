package platform.client.ui.element;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.math.MathUtil;
import platform.api.module.Interface;
import platform.client.ui.widget.Widget;
import lombok.Generated;

public class DragInfo implements Interface {
    private Widget b;
    private float c;
    private float d;
    private float e;
    private float f;
    private final String i;
    private double g = 0.0d;
    private double h = 0.0d;
    private int j = 0;
    private final DragSpring k;
    private final DragSpring l;

    @Generated
    public void a(Widget widget) { this.b = widget; }

    public void a(float x) { this.c = x; this.k.a(x); }

    public void b(float y) { this.d = y; this.l.a(y); }

    @Generated
    public void c(float width) { this.e = width; }

    @Generated
    public void d(float height) { this.f = height; }

    @Generated
    public void a(double offsetX) { this.g = offsetX; }

    @Generated
    public void b(double offsetY) { this.h = offsetY; }

    @Generated
    public void a(int status) { this.j = status; }

    @Generated
    public Widget e() { return this.b; }

    @Generated
    public float f() { return this.e; }

    @Generated
    public float g() { return this.f; }

    @Generated
    public double h() { return this.g; }

    @Generated
    public double i() { return this.h; }

    @Generated
    public String j() { return this.i; }

    @Generated
    public int k() { return this.j; }

    public DragInfo(String name, float x, float y, float width, float height) {
        this.i = name;
        this.c = x;
        this.d = y;
        this.e = width;
        this.f = height;
        this.k = new DragSpring(x);
        this.l = new DragSpring(y);
        Delta.h().d().s().e().add(this);
    }

    public void a(long now) {
        this.k.c(now);
        this.l.c(now);
    }

    public void b(long now) {
        this.k.b(this.c);
        this.l.b(this.d);
    }

    public float a() {
        return MathUtil.b(this.k.a(), 0.0f, (float) (aM_.getWindow().getGuiScaledWidth() - this.e));
    }

    public float b() {
        return MathUtil.b(this.l.a(), 0.0f, (float) (aM_.getWindow().getGuiScaledHeight() - this.f));
    }

    public float c() { return this.c; }

    public float d() { return this.d; }
}



