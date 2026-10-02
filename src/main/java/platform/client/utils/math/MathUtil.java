package platform.client.utils.math;

import platform.client.utils.text.StringUtils;

import static platform.api.module.Interface.aM_;

import platform.api.module.Interface;

import lombok.Generated;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class MathUtil implements Interface {
    @Generated
    private MathUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static float a(float start, float end, float delta) {
        return start + ((end - start) * delta);
    }

    public static Vec3 a(Entity entity, float partialTicks) {
        return new Vec3(entity.xo + ((entity.getX() - entity.xo) * ((double) partialTicks)), entity.yo + ((entity.getY() - entity.yo) * ((double) partialTicks)), entity.zo + ((entity.getZ() - entity.zo) * ((double) partialTicks)));
    }

    public static float b(float num, float min, float max) {
        return Math.min(Math.max(num, min), max);
    }

    public static double scale(double coordinate, int factor) {
        return (coordinate * (double) aM_.getWindow().getGuiScale()) / ((double) aM_.getWindow().calculateScale(factor, aM_.isEnforceUnicode()));
    }

    public static float a(float value) {
        float clamped = b(value, 0.0f, 1.0f);
        return clamped * clamped * (3.0f - (2.0f * clamped));
    }

    public static float a(float min, float max) {
        return (float) ((Math.random() * ((double) (max - min))) + ((double) min));
    }

    public static float[] b(float smoothness) {
        float horizontal = ((-smoothness) / 2.0f) + (smoothness * 2.0f);
        float vertical = (smoothness / 2.0f) + smoothness;
        return new float[]{horizontal, vertical};
    }

    public static String a(int amplifier) {
        String[] strings = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (amplifier < 0 || amplifier >= strings.length) ? String.valueOf(amplifier + 1) : strings[amplifier];
    }

    public static int a(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        String norm = text.replaceAll("§.", "").toLowerCase().replaceAll("\\s+", StringUtils.a).trim();
        String[] strings = {"i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x"};
        for (int i = strings.length - 1; i >= 0; i--) {
            if (norm.contains(strings[i])) {
                return i + 1;
            }
        }
        String digits = norm.replaceAll("[^0-9]", "");
        if (!digits.isEmpty() && Integer.parseInt(digits) >= 1 && Integer.parseInt(digits) <= 10) {
            return Integer.parseInt(digits);
        }
        return 0;
    }

    public static boolean a(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= ((double) x) && mouseX <= ((double) (x + width)) && mouseY >= ((double) y) && mouseY <= ((double) (y + height));
    }

    public static float c(float current, float target, float speed) {
        float delta = aM_.getDeltaTracker().getRealtimeDeltaTicks();
        return current + ((target - current) * (1.0f - ((float) Math.exp((-speed) * delta))));
    }
}



