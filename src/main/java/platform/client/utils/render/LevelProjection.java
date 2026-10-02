package platform.client.utils.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;

public final class LevelProjection {
    private static GpuBufferSlice slice;

    private LevelProjection() {
    }

    public static void set(GpuBufferSlice value) {
        slice = value;
    }

    public static GpuBufferSlice get() {
        return slice;
    }
}



