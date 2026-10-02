package platform.client.utils.render;

import platform.client.utils.render.pipeline.DeltaPipelines;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;

public final class NoiseShader {
    private static final int UNIFORM_SIZE = new Std140SizeCalculator().putVec4().putFloat().get();
    private static final NoiseShader INSTANCE = new NoiseShader();

    private GpuBuffer uniformBuffer;
    private GpuBuffer quadBuffer;
    private int quadVertexCount;
    private GpuSampler sampler;

    private NoiseShader() {
    }

    public static NoiseShader getInstance() {
        return INSTANCE;
    }

    public void e() {
    }

    public void a(float[] color) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || RenderSystem.tryGetDevice() == null) {
            return;
        }
        RenderTarget mainTarget = minecraft.gameRenderer.mainRenderTarget();
        if (mainTarget == null || mainTarget.getColorTexture() == null || mainTarget.width <= 1 || mainTarget.height <= 1) {
            return;
        }
        this.ensureResources();
        if (this.sampler == null) {
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE)
                    .putFloat(color[0])
                    .putFloat(color[1])
                    .putFloat(color[2])
                    .putFloat(color[3])
                    .putFloat((float) (System.currentTimeMillis() % 100000) / 1000.0f)
                    .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.uniformBuffer.slice(), data);
        }
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Delta Noise Hands",
                mainTarget.getColorTextureView(),
                Optional.empty(),
                mainTarget.getDepthTextureView(),
                OptionalDouble.empty()
        )) {
            pass.setPipeline(DeltaPipelines.NOISE_HANDS);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DeltaNoiseUniforms", this.uniformBuffer);
            pass.bindTexture("Sampler0", mainTarget.getColorTextureView(), this.sampler);
            pass.bindTexture("Sampler1", mainTarget.getDepthTextureView(), this.sampler);
            pass.setVertexBuffer(0, this.quadBuffer.slice());
            pass.draw(this.quadVertexCount, 1, 0, 0);
        }
    }

    private void ensureResources() {
        if (this.uniformBuffer == null) {
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "Delta Noise UBO",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                    UNIFORM_SIZE
            );
        }
        if (this.quadBuffer == null) {
            BufferBuilder builder = new BufferBuilder(
                    ByteBufferBuilder.exactlySized(6 * DefaultVertexFormat.POSITION_COLOR.getVertexSize()),
                    PrimitiveTopology.TRIANGLES,
                    DefaultVertexFormat.POSITION_COLOR
            );
            builder.addVertex(-1.0f, -1.0f, 0.0f).setColor(255, 255, 255, 255);
            builder.addVertex(1.0f, -1.0f, 0.0f).setColor(255, 255, 255, 255);
            builder.addVertex(1.0f, 1.0f, 0.0f).setColor(255, 255, 255, 255);
            builder.addVertex(1.0f, 1.0f, 0.0f).setColor(255, 255, 255, 255);
            builder.addVertex(-1.0f, 1.0f, 0.0f).setColor(255, 255, 255, 255);
            builder.addVertex(-1.0f, -1.0f, 0.0f).setColor(255, 255, 255, 255);
            try (MeshData mesh = builder.buildOrThrow()) {
                this.quadVertexCount = mesh.drawState().vertexCount();
                this.quadBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Delta Noise Quad",
                        GpuBuffer.USAGE_VERTEX,
                        mesh.vertexBuffer()
                );
            }
        }
    }
}


