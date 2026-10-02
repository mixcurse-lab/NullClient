package platform.client.utils.render;

import platform.client.utils.render.pipeline.DeltaPipelines;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.Optional;

public final class DeltaBlurProcessor {
    private static final int PASS_COUNT = 3;
    private static final int UNIFORM_SIZE = new Std140SizeCalculator().putVec2().putVec2().putFloat().get();
    private static final float BLUR_RADIUS = 16.0f;
    private static final DeltaBlurProcessor INSTANCE = new DeltaBlurProcessor();

    private static float strength = 1.0f;

    private static GpuBuffer quadBuffer;
    private static int quadVertexCount;

    private TextureTarget halfTarget;
    private TextureTarget quarterTarget;
    private GpuBuffer[] passUniforms;
    private GpuSampler sampler;

    private DeltaBlurProcessor() {
    }

    public static DeltaBlurProcessor getInstance() {
        return INSTANCE;
    }

    public static void setStrength(float value) {
        strength = Mth.clamp(value, 0.0f, 1.0f);
    }

    public static float getStrength() {
        return strength;
    }

    public GpuTextureView getBlurView() {
        return this.acquireView();
    }

    public GpuSampler getBlurSampler() {
        return this.sampler;
    }

    public GpuTextureView acquireView() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || RenderSystem.tryGetDevice() == null) {
            return null;
        }
        RenderTarget mainTarget = minecraft.gameRenderer.mainRenderTarget();
        if (mainTarget == null || mainTarget.getColorTexture() == null || mainTarget.width <= 1 || mainTarget.height <= 1) {
            return null;
        }
        if (!this.ensureTargets(mainTarget.width, mainTarget.height)) {
            return null;
        }
        if (this.sampler == null) {
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        }
        return this.halfTarget.getColorTextureView();
    }

    public void run() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || RenderSystem.tryGetDevice() == null) {
            return;
        }
        RenderTarget mainTarget = minecraft.gameRenderer.mainRenderTarget();
        if (mainTarget == null || mainTarget.getColorTexture() == null || mainTarget.width <= 1 || mainTarget.height <= 1) {
            return;
        }
        if (!this.ensureTargets(mainTarget.width, mainTarget.height)) {
            return;
        }
        this.ensureStaticResources();
        float radius = Math.max(1.0F, BLUR_RADIUS * strength);
        float offset = Mth.clamp(radius / 8.0F, 0.5F, 4.0F);
        this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        this.runPass(0, DeltaPipelines.BLUR_DOWNSCALE, mainTarget.getColorTextureView(), mainTarget.width, mainTarget.height, this.halfTarget, 1.0f);
        if (radius >= 6.0F) {
            this.runPass(1, DeltaPipelines.BLUR_DOWNSCALE, this.halfTarget.getColorTextureView(), this.halfTarget.width, this.halfTarget.height, this.quarterTarget, offset);
            this.runPass(2, DeltaPipelines.BLUR_UPSCALE, this.quarterTarget.getColorTextureView(), this.quarterTarget.width, this.quarterTarget.height, this.halfTarget, offset);
        }
    }

    private boolean ensureTargets(int mainWidth, int mainHeight) {
        int halfWidth = Math.max(1, mainWidth / 2);
        int halfHeight = Math.max(1, mainHeight / 2);
        if (this.halfTarget == null || this.halfTarget.width != halfWidth || this.halfTarget.height != halfHeight) {
            if (this.halfTarget != null) {
                this.halfTarget.destroyBuffers();
            }
            if (this.quarterTarget != null) {
                this.quarterTarget.destroyBuffers();
                this.quarterTarget = null;
            }
            this.halfTarget = new TextureTarget("Delta Blur Half", halfWidth, halfHeight, false, GpuFormat.RGBA8_UNORM);
            this.quarterTarget = new TextureTarget("Delta Blur Quarter", Math.max(1, halfWidth / 2), Math.max(1, halfHeight / 2), false, GpuFormat.RGBA8_UNORM);
        }
        return this.halfTarget != null && this.quarterTarget != null;
    }

    private void ensureStaticResources() {
        if (this.passUniforms == null) {
            this.passUniforms = new GpuBuffer[PASS_COUNT];
            for (int i = 0; i < this.passUniforms.length; i++) {
                int index = i;
                this.passUniforms[i] = RenderSystem.getDevice().createBuffer(
                        () -> "Delta Blur UBO " + index,
                        GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                        UNIFORM_SIZE
                );
            }
        }
        if (quadBuffer == null) {
            BufferBuilder builder = new BufferBuilder(
                    ByteBufferBuilder.exactlySized(6 * DefaultVertexFormat.POSITION_TEX.getVertexSize()),
                    PrimitiveTopology.TRIANGLES,
                    DefaultVertexFormat.POSITION_TEX
            );
            builder.addVertex(-1.0f, -1.0f, 0.0f).setUv(0.0f, 0.0f);
            builder.addVertex(1.0f, -1.0f, 0.0f).setUv(1.0f, 0.0f);
            builder.addVertex(1.0f, 1.0f, 0.0f).setUv(1.0f, 1.0f);
            builder.addVertex(1.0f, 1.0f, 0.0f).setUv(1.0f, 1.0f);
            builder.addVertex(-1.0f, 1.0f, 0.0f).setUv(0.0f, 1.0f);
            builder.addVertex(-1.0f, -1.0f, 0.0f).setUv(0.0f, 0.0f);
            try (MeshData mesh = builder.buildOrThrow()) {
                quadVertexCount = mesh.drawState().vertexCount();
                quadBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Delta Blur Quad",
                        GpuBuffer.USAGE_VERTEX,
                        mesh.vertexBuffer()
                );
            }
        }
    }

    private void runPass(int passIndex, RenderPipeline pipeline, GpuTextureView input, int inputWidth, int inputHeight, TextureTarget destination, float offset) {
        GpuBuffer uniforms = this.passUniforms[passIndex];
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE)
                    .putFloat(0.5f / inputWidth)
                    .putFloat(0.5f / inputHeight)
                    .putFloat(0.0f)
                    .putFloat(0.0f)
                    .putFloat(offset)
                    .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(uniforms.slice(), data);
        }
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Delta Blur",
                destination.getColorTextureView(),
                Optional.empty()
        )) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DeltaKawaseUniforms", uniforms);
            pass.bindTexture("CurrentInput", input, this.sampler);
            pass.setVertexBuffer(0, quadBuffer.slice());
            pass.draw(quadVertexCount, 1, 0, 0);
        }
    }
}



