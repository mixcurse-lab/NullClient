package platform.client.utils.render;

import platform.api.system.configs.ThemeInfo;
import platform.client.Delta;
import platform.client.utils.render.pipeline.DeltaPipelines;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;

public final class SkyShaderRenderer {

    private static final int UNIFORM_SIZE = new Std140SizeCalculator()
            .putVec2()
            .putFloat()
            .putVec3()
            .putFloat()
            .putFloat()
            .putFloat()
            .putFloat()
            .putVec2()
            .putFloat()
            .get();

    private static final SkyShaderRenderer INSTANCE = new SkyShaderRenderer();

    private GpuBuffer uniformBuffer;
    private GpuBuffer quadBuffer;
    private int quadVertexCount;
    private GpuSampler sampler;
    private long startMillis = -1;

    private SkyShaderRenderer() {}

    public static SkyShaderRenderer getInstance() { return INSTANCE; }

    public void resetTime() { startMillis = -1; }

    private static long lastDbg = 0;


    private static boolean chunksAroundLoaded(Minecraft mc) {
        try {
            if (mc.player == null || mc.level == null) return false;
            var cp = mc.player.chunkPosition();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (!mc.level.getChunkSource().hasChunk(cp.x() + dx, cp.z() + dz)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Throwable ignored) {
            return true;
        }
    }

    public void render(String mode, float speed, float scale, float intensity, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || RenderSystem.tryGetDevice() == null) {

            return;
        }
        RenderTarget mainTarget = mc.gameRenderer.mainRenderTarget();
        if (mainTarget == null || mainTarget.getColorTexture() == null || mainTarget.width <= 1 || mainTarget.height <= 1) return;

        if (mainTarget.getDepthTexture() == null || mainTarget.getDepthTextureView() == null) return;
        if (mc.level == null || mc.player == null) return;
        boolean ready = chunksAroundLoaded(mc);
        if (!ready) return;
        if (startMillis < 0) startMillis = System.currentTimeMillis();
        float time = (System.currentTimeMillis() - startMillis) / 1000.0f;


        float resW = (float) mainTarget.width;
        float resH = (float) mainTarget.height;


        int themeInt = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        float cr = ((themeInt >> 16) & 0xFF) / 255f;
        float cg = ((themeInt >> 8) & 0xFF) / 255f;
        float cb = (themeInt & 0xFF) / 255f;


        float yawRad = 0f, pitchRad = 0f, fov = 70f;
        try {
            Camera cam = mc.getEntityRenderDispatcher().camera;
            if (cam != null) {
                yawRad = (float) Math.toRadians(-cam.yRot());
                pitchRad = (float) Math.toRadians(cam.xRot());
                fov = cam.getFov();
            } else {
                fov = 70f;
            }
        } catch (Throwable ignored) {
            try {
                yawRad = (float) Math.toRadians(-mc.getEntityRenderDispatcher().camera.yRot());
                pitchRad = (float) Math.toRadians(mc.getEntityRenderDispatcher().camera.xRot());
                fov = mc.getEntityRenderDispatcher().camera.getFov();
            } catch (Throwable ignored2) {}
        }

        if (fov < 1f || fov > 179f) fov = 70f;

        ensureResources();
        if (sampler == null) sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);


        RenderPipeline pipeline;
        switch (mode) {
            case "Caustics": pipeline = DeltaPipelines.SKY_CAUSTICS; break;
            case "Clouds": pipeline = DeltaPipelines.SKY_CLOUDS; break;
            case "Matrix": pipeline = DeltaPipelines.SKY_MATRIX; break;
            case "Starfield": pipeline = DeltaPipelines.SKY_STARFIELD; break;
            default: pipeline = DeltaPipelines.SKY_DARKNESS; break;
        }


        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE)
                    .putVec2(resW, resH)
                    .putFloat(time)
                    .putVec3(cr, cg, cb)
                    .putFloat(alpha)
                    .putFloat(speed)
                    .putFloat(scale)
                    .putFloat(intensity)
                    .putVec2(yawRad, pitchRad)
                    .putFloat(fov)
                    .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.uniformBuffer.slice(), data);
        }

        boolean depthViewUsed = mainTarget.getDepthTextureView() != null;
        try (RenderPass pass = depthViewUsed
                ? RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Delta Sky Shader " + mode,
                mainTarget.getColorTextureView(),
                Optional.empty(),
                mainTarget.getDepthTextureView(),
                OptionalDouble.empty()
        )
                : RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Delta Sky Shader " + mode,
                mainTarget.getColorTextureView(),
                Optional.empty()
        )) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("SkyUniforms", this.uniformBuffer);
            pass.bindTexture("ScreenTexture", mainTarget.getColorTextureView(), sampler);
            pass.bindTexture("DepthTexture", mainTarget.getDepthTextureView(), sampler);
            pass.setVertexBuffer(0, this.quadBuffer.slice());
            pass.draw(this.quadVertexCount, 1, 0, 0);
        } catch (Throwable t) {
            long now = System.currentTimeMillis();
            if (now - lastDbg > 3000L) {
                lastDbg = now;
                System.err.println("[SkyShader] render failed: " + t.getMessage());
            }
        }
    }

    private void ensureResources() {
        if (this.uniformBuffer == null) {
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "Delta Sky UBO",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                    UNIFORM_SIZE
            );
        }
        if (this.quadBuffer == null) {
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
                this.quadVertexCount = mesh.drawState().vertexCount();
                this.quadBuffer = RenderSystem.getDevice().createBuffer(
                        () -> "Delta Sky Quad",
                        GpuBuffer.USAGE_VERTEX,
                        mesh.vertexBuffer()
                );
            }
        }
    }
}
