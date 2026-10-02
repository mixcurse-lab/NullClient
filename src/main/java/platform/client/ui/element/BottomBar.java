package platform.client.ui.element;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Interface;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.client.utils.render.Draw2DProcessor;
import platform.client.utils.render.EasingList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

public class BottomBar {
    private final float[] a = new float[3];
    private boolean b;
    private float c;
    private float d;
    private final List<Button> e;

    public BottomBar(Button... buttons) {
        this.e = java.util.Arrays.asList(buttons);
    }

    public void a(int screenWidth, int screenHeight) {
        float buttonsRowY = screenHeight * 0.85f;
        this.c = (screenWidth - 79.0f) / 2.0f;
        this.d = (buttonsRowY - 19.5f) - 5.0f;
        a(screenWidth, buttonsRowY, 5.0f, (Button[]) this.e.toArray(new Button[0]));
    }

    public void a(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        a(context, mouseX, mouseY, delta, 1.0f);
    }

    public void a(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, float alpha) {
        a(context, alpha);
        for (Button button : this.e) {
            button.a(context, mouseX, mouseY, delta, alpha);
        }
    }

    private void a(GuiGraphicsExtractor context, float alpha) {
        this.a[1] = this.a[1] + ((this.a[0] - this.a[1]) * 0.25f);
        Draw2DProcessor draw = Delta.h().d().i();
        float knobX = this.c + 1.75f + (this.a[1] * Math.max(1.0f, 79.0f - 19.5f));
        float knobY = this.d + ((19.5f - 16.0f) / 2.0f);
        draw.b(context, this.c, this.d, 79.0f, 19.5f, 8.0f, ColorUtil.a(11, 11, 13, (int) (150.0f * alpha)), 1.0f);
        draw.a(context, this.c, this.d, 79.0f, 19.5f, 8.0f, 0.5f, ColorUtil.a(255, 255, 255, (int) (15.0f * alpha)));
        float textX = this.c + 9.0f;
        float visibleWidth = (knobX - 3.0f) - textX;
        Fonts.e.c(context, "Выйти из игры", textX, (this.d + ((19.5f - 7.0f) / 2.0f)) - 0.5f, 7.0f, ColorUtil.a(ColorUtil.a(220, 80, 80, 255), this.a[1] * alpha), visibleWidth);
        int knob = ColorUtil.a(ColorUtil.a(255, 255, 255, 13), ColorUtil.a(220, 80, 80, 40), this.a[1]);
        context.pose().pushMatrix();
        float cx = knobX + 8.0f;
        float cy = knobY + 8.0f;
        context.pose().translate(cx, cy);
        context.pose().rotate((float) Math.toRadians(-90.0f + (180.0f * this.a[1])));
        context.pose().translate(-cx, -cy);
        draw.a(context, knobX, knobY, 16.0f, 16.0f, 7.0f, ColorUtil.a(knob, (ColorUtil.b(knob)[3] / 255.0f) * alpha));
        Fonts.a.a(context, "c", (cx - (Fonts.a.a("c", 8.5f) / 2.0f)) + 1.0f, cy - 4.5f, 8.5f, ColorUtil.a(ColorUtil.a(-1, ColorUtil.a(220, 80, 80, 255), this.a[1]), alpha));
        context.pose().popMatrix();
    }

    public boolean a(double mouseX, double mouseY) {
        float knobX = this.c + 1.75f + (this.a[0] * Math.max(1.0f, 79.0f - 19.5f));
        float knobY = this.d + ((19.5f - 16.0f) / 2.0f);
        if (MathUtil.a(mouseX, mouseY, knobX, knobY, 16.0f, 16.0f)) {
            this.a[2] = ((float) mouseX) - knobX;
            this.b = true;
            return true;
        }
        for (Button button : this.e) {
            if (button.e() != null && MathUtil.a(mouseX, mouseY, button.f(), button.g(), button.b(), button.c())) {
                button.e().run();
                return true;
            }
        }
        return false;
    }

    public boolean a(double mouseX) {
        if (this.b) {
            this.a[0] = Mth.clamp((((((float) mouseX) - this.a[2]) - this.c) - 1.75f) / Math.max(1.0f, 79.0f - 19.5f), 0.0f, 1.0f);
            return false;
        }
        return false;
    }

    public boolean a() {
        if (this.b) {
            this.b = false;
            this.a[0] = this.a[0] >= 0.95f ? this.a[0] : 0.0f;
            if (this.a[0] > 0.0f) {
                System.exit(0);
                return false;
            }
            return false;
        }
        return false;
    }

    private void a(float screenWidth, float y, float gap, Button... row) {
        float totalWidth = -gap;
        for (Button button : row) {
            totalWidth += button.b() + gap;
        }
        float x = (screenWidth - totalWidth) / 2.0f;
        for (Button button2 : row) {
            button2.a(x, y);
            x += button2.b() + gap;
        }
    }
}



