package platform.client.ui.element;

import static platform.api.module.Interface.aM_;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public final class GravityGrid {
    private static final GravityGrid INSTANCE = new GravityGrid();

    private static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(Identifier.parse("delta:pipeline/gravity_grid"))
            .withVertexShader(Identifier.parse("delta:core/gravity_grid"))
            .withFragmentShader(Identifier.parse("delta:core/gravity_grid"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withVertexBinding(0, DefaultVertexFormat.POSITION)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withCull(false)
            .build();

    private GpuBuffer vertices;
    private int vertexWidth = -1;
    private int vertexHeight = -1;
    private boolean broken;

    private GravityGrid() {
    }

    public static GravityGrid a() {
        return INSTANCE;
    }

    public void a(int framebufferWidth, int framebufferHeight, float[] wells, float[] mass, int count, float cursorX, float cursorY, float alpha, int accentTop, int accentBottom) {
        if (this.broken || framebufferWidth <= 0 || framebufferHeight <= 0 || alpha <= 0.01f) {
            return;
        }
        try {
            this.b();
            ByteBuffer data = ByteBuffer.allocateDirect(16 + 16 + 16 + (16 * 32) + 16 + 16).order(ByteOrder.nativeOrder());
            Std140Builder builder = Std140Builder.intoBuffer(data);
            builder.putVec2((float) framebufferWidth, (float) framebufferHeight);
            builder.putVec2(cursorX, cursorY);
            builder.putVec4((float) (System.nanoTime() % 240000000000L) / 1.0E9f, Math.max(0.0f, Math.min(1.0f, alpha)), (float) count, 0.0f);
            for (int i = 0; i < 32; i++) {
                int offset = i * 4;
                if (i < count) {
                    builder.putVec4(wells[offset], wells[offset + 1], Math.max(wells[offset + 2], 1.0f), mass[i]);
                } else {
                    builder.putVec4(0.0f, 0.0f, 1.0f, 0.0f);
                }
            }
            builder.putVec4(a(accentTop), b(accentTop), c(accentTop), 0.0f);
            builder.putVec4(a(accentBottom), b(accentBottom), c(accentBottom), 0.0f);
            GpuBuffer uniforms = RenderSystem.getDevice().createBuffer(() -> "Delta-Grid-Uniforms", GpuBuffer.USAGE_UNIFORM, builder.get());
            try {
                var renderTarget = aM_.gameRenderer.mainRenderTarget();
                try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Delta-Grid", renderTarget.getColorTextureView(), Optional.empty())) {
                    pass.setPipeline(PIPELINE);
                    pass.setUniform("GridParams", uniforms.slice());
                    pass.setVertexBuffer(0, this.vertices.slice());
                    pass.draw(3, 1, 0, 0);
                }
            } finally {
                uniforms.close();
            }
        } catch (Throwable ignored) {
            this.broken = true;
        }
    }

    private void b() {
        if (this.vertices != null) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(3 * 12).order(ByteOrder.nativeOrder());
        buffer.putFloat(-1.0f).putFloat(-1.0f).putFloat(0.0f);
        buffer.putFloat(3.0f).putFloat(-1.0f).putFloat(0.0f);
        buffer.putFloat(-1.0f).putFloat(3.0f).putFloat(0.0f);
        buffer.rewind();
        this.vertices = RenderSystem.getDevice().createBuffer(() -> "Delta-Grid-Vertices", GpuBuffer.USAGE_VERTEX, buffer);
        this.vertexWidth = 1;
        this.vertexHeight = 1;
    }

    private static float a(int color) {
        return (float) ((color >>> 16) & 0xFF) / 255.0f;
    }

    private static float b(int color) {
        return (float) ((color >>> 8) & 0xFF) / 255.0f;
    }

    private static float c(int color) {
        return (float) (color & 0xFF) / 255.0f;
    }
}



