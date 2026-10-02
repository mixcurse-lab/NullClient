package platform.client.ui.element;

import platform.client.Delta;
import platform.api.module.InterfaceC0020Opcode;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.client.utils.render.Draw2DProcessor;
import platform.client.utils.render.AnimationUtil;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class Button {
    private final AnimationUtil a = new AnimationUtil();
    private final float b;
    private final float c;
    private final String d;
    private final Runnable e;
    private float f;
    private float g;

    @Generated
    public AnimationUtil a() { return this.a; }

    @Generated
    public float b() { return this.b; }

    @Generated
    public float c() { return this.c; }

    @Generated
    public String d() { return this.d; }

    @Generated
    public Runnable e() { return this.e; }

    @Generated
    public float f() { return this.f; }

    @Generated
    public float g() { return this.g; }

    public Button(float width, float height, String label, Runnable action) {
        this.b = width;
        this.c = height;
        this.d = label;
        this.e = action;
    }

    public void a(float x, float y) {
        this.f = x;
        this.g = y;
    }

    public void a(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, float open) {
        this.a.a(this.e != null && MathUtil.a((double) mouseX, (double) mouseY, this.f, this.g, this.b, this.c));
        this.a.a(0.0f, 1.0f, 0.35f, EasingList.i, delta);
        float hover = Math.min(1.0f, this.a.c() / 0.9f);
        float scale = (0.85f + (0.15f * EasingList.s.ease(open))) * (1.0f + (0.03f * hover));
        float cx = this.f + (this.b / 2.0f);
        float cy = this.g + (this.c / 2.0f);
        context.pose().pushMatrix();
        context.pose().translate(cx, cy);
        context.pose().scale(scale, scale);
        context.pose().translate(-cx, -cy);
        Draw2DProcessor draw = Delta.h().d().i();
        draw.b(context, this.f, this.g, this.b, this.c, 8.0f, ColorUtil.a(11, 11, 13, InterfaceC0020Opcode.bN), open);
        draw.a(context, this.f, this.g, this.b, this.c, 8.0f, 0.5f, ColorUtil.a(255, 255, 255, (int) (hover * 20.0f * open)));
        if (this.d != null) {
            float time = (System.currentTimeMillis() % 3000) / 3000.0f;
            MutableComponent styled = Component.literal("");
            for (int i = 0; i < this.d.length(); i++) {
                float wave = (float) ((Math.sin(((double) (time + ((i * 0.5f) / this.d.length()))) * 3.141592654293742d * 2.0d) * 0.5d) + 0.5d);
                int c = (int) (180.0f + (65.0f * wave * hover));
                styled.append(Component.literal(String.valueOf(this.d.charAt(i))).withStyle(s -> s.withColor((c << 16) | (c << 8) | c)));
            }
            float labelW = Fonts.e.a(this.d, 8.0f);
            Fonts.e.a(context, styled, this.f + ((this.b - labelW) / 2.0f), this.g + ((this.c - 9.0f) / 2.0f), 8.0f, (double) open);
        }
        context.pose().popMatrix();
    }
}



