package platform.client.utils.render.jump;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.util.Optional;
import java.util.OptionalDouble;

public final class JumpGlowRenderer {
    private static final int SEGMENTS = 48;
    private static final int VERTEX_COUNT = SEGMENTS * 6;
    private static final float GLOW_HEIGHT = 1.0F;
    private static final int UNIFORM_SIZE = new Std140SizeCalculator()
            .putVec4()
            .putVec4()
            .get();

    private static final BindGroupLayout LAYOUT = BindGroupLayout.builder()
            .withSampler("SceneSampler")
            .withUniform("JumpGlowUniforms", UniformType.UNIFORM_BUFFER)
            .build();

    private static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(Identifier.parse("delta:pipeline/world/jump_glow"))
            .withVertexShader(Identifier.parse("delta:core/jump/jump_glow"))
            .withFragmentShader(Identifier.parse("delta:core/jump/jump_glow"))
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(LAYOUT)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withCull(false)
            .build();

    private final SceneSnapshot scene = new SceneSnapshot("Delta Jump Glow Scene");
    private GpuSampler sampler;

    public void render(Vec3 center, float radius, float ringProgress, int color, float fade, float time) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || fade <= 0.003F || radius <= 0.001F) {
            return;
        }

        var target = mc.gameRenderer.mainRenderTarget();
        GpuTextureView colorView = target != null ? target.getColorTextureView() : null;
        if (colorView == null) {
            return;
        }

        var sceneCopy = this.scene.capture();
        if (sceneCopy == null) {
            return;
        }

        if (this.sampler == null) {
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        }

        float currentRadius = Math.max(0.001F, radius * ringProgress);
        var device = RenderSystem.getDevice();
        MeshData meshData = buildMesh(mc, center, currentRadius);
        GpuBuffer vertexBuffer = null;
        GpuBuffer uniformBuffer = null;
        try {
            vertexBuffer = device.createBuffer(
                    () -> "Delta Jump Glow Vertices",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                    meshData.vertexBuffer()
            );
            uniformBuffer = uploadUniform(color, fade, ringProgress, time);

            GpuTextureView depthView = target.getDepthTextureView();
            RenderPass pass = depthView != null
                    ? device.createCommandEncoder().createRenderPass(
                    () -> "Delta Jump Glow Pass", colorView, Optional.empty(), depthView, OptionalDouble.empty())
                    : device.createCommandEncoder().createRenderPass(
                    () -> "Delta Jump Glow Pass", colorView, Optional.empty());
            try {
                pass.setPipeline(PIPELINE);
                pass.setUniform("Projection", levelProjection());
                pass.setUniform("JumpGlowUniforms", uniformBuffer);
                pass.bindTexture("SceneSampler", sceneCopy.getColorTextureView(), this.sampler);
                pass.setVertexBuffer(0, vertexBuffer.slice());
                pass.draw(VERTEX_COUNT, 1, 0, 0);
            } finally {
                pass.close();
            }
        } finally {
            if (uniformBuffer != null) {
                uniformBuffer.close();
            }
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }
            meshData.close();
        }
    }

    private static GpuBufferSlice levelProjection() {
        GpuBufferSlice slice = platform.client.utils.render.LevelProjection.get();
        return slice != null ? slice : RenderSystem.getProjectionMatrixBuffer();
    }

    private MeshData buildMesh(Minecraft mc, Vec3 center, float radius) {
        Camera camera = mc.gameRenderer.mainCamera();
        Matrix4f pose = JumpCircleRenderer.cameraViewPose(camera);
        float baseX = (float) center.x;
        float baseY = (float) center.y + 0.02F;
        float baseZ = (float) center.z;

        BufferBuilder builder = new BufferBuilder(
                new ByteBufferBuilder(VERTEX_COUNT * DefaultVertexFormat.POSITION_TEX.getVertexSize()),
                PrimitiveTopology.TRIANGLES,
                DefaultVertexFormat.POSITION_TEX
        );
        for (int i = 0; i < SEGMENTS; i++) {
            float a0 = (float) (Math.PI * 2.0 * i / SEGMENTS);
            float a1 = (float) (Math.PI * 2.0 * (i + 1) / SEGMENTS);
            float u0 = (float) i / SEGMENTS;
            float u1 = (float) (i + 1) / SEGMENTS;

            double b0x = baseX + Math.cos(a0) * radius;
            double b0z = baseZ + Math.sin(a0) * radius;
            double b1x = baseX + Math.cos(a1) * radius;
            double b1z = baseZ + Math.sin(a1) * radius;
            double topY = baseY + GLOW_HEIGHT;

            Vector4f vb0 = JumpCircleRenderer.toViewSpace(pose, b0x, baseY, b0z);
            Vector4f vb1 = JumpCircleRenderer.toViewSpace(pose, b1x, baseY, b1z);
            Vector4f vt0 = JumpCircleRenderer.toViewSpace(pose, b0x, topY, b0z);
            Vector4f vt1 = JumpCircleRenderer.toViewSpace(pose, b1x, topY, b1z);

            addVertex(builder, vb0, u0, 0.0F);
            addVertex(builder, vb1, u1, 0.0F);
            addVertex(builder, vt1, u1, 1.0F);
            addVertex(builder, vb0, u0, 0.0F);
            addVertex(builder, vt1, u1, 1.0F);
            addVertex(builder, vt0, u0, 1.0F);
        }
        return builder.buildOrThrow();
    }

    private static void addVertex(BufferBuilder builder, Vector4f point, float u, float v) {
        builder.addVertex(point.x, point.y, point.z).setUv(u, v);
    }

    private GpuBuffer uploadUniform(int color, float fade, float ringProgress, float time) {
        var device = RenderSystem.getDevice();
        GpuBuffer buffer = device.createBuffer(
                () -> "Delta Jump Glow UBO",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                UNIFORM_SIZE
        );

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var data = Std140Builder.onStack(stack, UNIFORM_SIZE)
                    .putVec4(
                            ((color >> 16) & 0xFF) / 255.0F,
                            ((color >> 8) & 0xFF) / 255.0F,
                            (color & 0xFF) / 255.0F,
                            ((color >>> 24) & 0xFF) / 255.0F
                    )
                    .putVec4(fade, time, 0.0F, ringProgress)
                    .get();
            device.createCommandEncoder().writeToBuffer(buffer.slice(), data);
        }

        return buffer;
    }

    public void release() {
        this.scene.release();
    }
}
