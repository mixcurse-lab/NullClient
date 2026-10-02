package platform.client.ui.widget;

import platform.client.Delta;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.client.utils.render.AnimationUtil;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class EffectMarker {
    private EffectMarker() {
    }

    public static void a(List<a> list, float x, float y) {
        if (list != null) {
            list.add(new a(x, y));
        }
    }

    public static void a(GuiGraphicsExtractor context, float partialTicks, List<a> list) {
        if (list == null || list.isEmpty()) return;
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).a(context, partialTicks)) {
                list.remove(i);
            }
        }
    }

    public static final class a {
        private final AnimationUtil a = new AnimationUtil();
        private final long b = System.nanoTime() + 300000000;
        private final float c;
        private final float d;
        private boolean e;

        a(float x, float y) {
            this.c = x;
            this.d = y;
        }

        boolean a(GuiGraphicsExtractor context, float partialTicks) {
            this.a.a(0.0f, 1.0f, 0.2f, EasingList.h, partialTicks);
            if (!this.e && System.nanoTime() >= this.b) {
                this.e = true;
            }
            this.a.a(!this.e);
            float progress = MathUtil.b(this.a.c(), 0.0f, 1.0f);
            float scale = this.e ? progress : a(progress);
            float length = 4.0f * Math.max(1.0E-4f, scale);
            int color = ColorUtil.a(255, 255, 255, Math.round(250.0f * progress));
            context.pose().pushMatrix();
            context.pose().translate(this.c, this.d);
            for (int i = 0; i < 4; i++) {
                a(context, length, 45.0f + (90.0f * i), length, color);
            }
            context.pose().popMatrix();
            return this.e && progress <= 0.01f;
        }

        private void a(GuiGraphicsExtractor context, float length, float angleDeg, float offset, int color) {
            context.pose().pushMatrix();
            context.pose().rotate((float) Math.toRadians(angleDeg));
            context.pose().translate(offset, 0.0f);
            Delta.h().d().i().a(context, -length / 2.0f, -0.25f, length, 0.5f, 0.0f, color);
            context.pose().popMatrix();
        }

        private float a(float scale) {
            if (scale <= 0.0f) return 0.0f;
            if (scale < 0.6f) return scale / 0.6f;
            return scale < 0.8f ? 1.0f + (((scale - 0.6f) / 0.5f) * 0.5f) : 1.2f - (((scale - 0.8f) / 0.2f) * 0.2f);
        }
    }
}



