package platform.client.utils.render.pipeline;

final class VertexPacking {

    static final float SIZE_SCALE = 8.0F;
    static final float RADIUS_SCALE = 16.0F;
    static final float MAX_DUAL_RADIUS = 4095.0F / RADIUS_SCALE;
    private static final int MODE_FLAG = 1 << 12;

    private VertexPacking() {
    }

    static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    static int clampByte(int value) {
        return clamp(value, 0, 255);
    }

    static int packSize(float px) {
        return clamp(Math.round(px * SIZE_SCALE), 0, 32767);
    }

    static int packSignedSize(float px) {
        return clamp(Math.round(px * SIZE_SCALE), -32768, 32767);
    }

    static int packRadius(float px) {
        return clamp(Math.round(px * RADIUS_SCALE), 0, 32767);
    }

    static int packU8Pair(int lo, int hi) {
        return clampByte(lo) | (clampByte(hi) << 8);
    }

    static float packZ(float valuePx, int alpha8, boolean mode) {
        int units = clamp(Math.round(valuePx * RADIUS_SCALE), 0, MODE_FLAG - 1);
        if (mode) {
            units |= MODE_FLAG;
        }
        return units * 256.0F + clampByte(alpha8);
    }

    static float packDual12(float hiPx, float loPx) {
        int hi = clamp(Math.round(hiPx * RADIUS_SCALE), 0, 4095);
        int lo = clamp(Math.round(loPx * RADIUS_SCALE), 0, 4095);
        return hi * 4096.0F + lo;
    }

    static float packDual12Raw(int hi, int lo) {
        return clamp(hi, 0, 4095) * 4096.0F + clamp(lo, 0, 4095);
    }

    static float snormChannel(int channel) {
        return channel / 127.5F - 1.0F;
    }

    static int encodeUnit01(float value) {
        return clampByte(Math.round(value * 255.0F));
    }
}



