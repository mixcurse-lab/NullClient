package platform.client.ui.shader;

import platform.client.utils.render.ColorUtil;
import java.awt.Color;
import lombok.Generated;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class GradientUtil {
    @Generated
    private GradientUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static MutableComponent a(String text, int startColor, int endColor, int speed, float ratio) {
        MutableComponent component = Component.literal("");
        if (text == null || text.isEmpty()) return component;
        float time = ((System.currentTimeMillis() % 10000) / 1000.0f) * (100.0f / speed);
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            int color = ColorUtil.a(startColor, endColor, i, length, time, ratio);
            Style style = Style.EMPTY.withColor(color & 16777215);
            component.append(Component.literal(String.valueOf(c)).withStyle(style));
        }
        return component;
    }

    public static MutableComponent a(String text, int color, float speed, float offset) {
        MutableComponent component = Component.literal("");
        if (text == null || text.isEmpty()) return component;
        float time = (System.currentTimeMillis() % ((long) (speed * 1000.0f))) / (speed * 1000.0f);
        float[] hsb = Color.RGBtoHSB((color >> 16) & 255, (color >> 8) & 255, color & 255, (float[]) null);
        for (int i = 0; i < text.length(); i++) {
            float factor = (float) ((Math.sin(((double) (time + ((i * offset) / text.length()))) * 3.1415926649985018d * 2.0d) * 0.5d) + 0.5d);
            int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] * (0.5f + (0.5f * factor))) & 16777215;
            component.append(Component.literal(String.valueOf(text.charAt(i))).withColor(rgb));
        }
        return component;
    }

    public static int a(int speed, int angle, int startColor, int endColor, long time) {
        float animatedAngle = (((long) angle) + (time / 10)) % 360;
        float ratio = animatedAngle / 360.0f;
        return ColorUtil.b(startColor, endColor, (float) ((Math.sin(((((double) ratio) * 3.1415926649985018d) * 2.0d) * ((double) speed)) + 1.0d) / 2.0d));
    }
}



