
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

public interface MultiBufferSource {
    VertexConsumer getBuffer(Object renderType);

    interface Immediate extends MultiBufferSource {
        void draw();
    }
}

