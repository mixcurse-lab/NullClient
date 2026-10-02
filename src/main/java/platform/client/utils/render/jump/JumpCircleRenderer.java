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
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.util.Optional;
import java.util.OptionalDouble;

public final class JumpCircleRenderer {
    private static final Identifier FREQUENCY_TEXTURE = Identifier.parse("delta:pictures/jump_frequency.png");

    private static final int UNIFORM_SIZE = new Std140SizeCalculator()
            .putVec4()
            .putVec4()
            .get();

    private static final BindGroupLayout LAYOUT = BindGroupLayout.builder()
            .withSampler("iChannel0")
            .withUniform("JumpCircleUniforms", UniformType.UNIFORM_BUFFER)
            .build();

    private static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(Identifier.parse("delta:pipeline/world/jump_circle"))
            .withVertexShader(Identifier.parse("delta:core/jump/jump_circle"))
            .withFragmentShader(Identifier.parse("delta:core/jump/jump_circle"))
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(LAYOUT)
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .withCull(false)
            .build();

    private GpuSampler sampler;

    public void render(Vec3 center, float radius, float alpha, float time, int color, boolean glowEdge) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null || mc.player == null || radius <= 0.001F || alpha <= 0.003F) {
            return;
        }

        var target = mc.gameRenderer.mainRenderTarget();
        GpuTextureView colorView = target != null ? target.getColorTextureView() : null;
        if (colorView == null) {
            return;
        }

        AbstractTexture frequencyTexture = mc.getTextureManager().getTexture(FREQUENCY_TEXTURE);
        GpuTextureView frequencyView = frequencyTexture != null ? frequencyTexture.getTextureView() : null;
        if (frequencyView == null) {
            return;
        }

        if (this.sampler == null) {
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        }

        var device = RenderSystem.getDevice();
        MeshData meshData = buildMesh(mc, center, radius);
        GpuBuffer vertexBuffer = null;
        GpuBuffer uniformBuffer = null;
        try {
            vertexBuffer = device.createBuffer(
                    () -> "Delta Jump Circle Vertices",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                    meshData.vertexBuffer()
            );
            uniformBuffer = uploadUniform(color, time, alpha, glowEdge);

            GpuTextureView depthView = target.getDepthTextureView();
            RenderPass pass = depthView != null
                    ? device.createCommandEncoder().createRenderPass(
                    () -> "Delta Jump Circle Pass", colorView, Optional.empty(), depthView, OptionalDouble.empty())
                    : device.createCommandEncoder().createRenderPass(
                    () -> "Delta Jump Circle Pass", colorView, Optional.empty());
            try {
                pass.setPipeline(PIPELINE);
                pass.setUniform("Projection", levelProjection());
                pass.setUniform("JumpCircleUniforms", uniformBuffer);
                pass.bindTexture("iChannel0", frequencyView, this.sampler);
                pass.setVertexBuffer(0, vertexBuffer.slice());
                pass.draw(6, 1, 0, 0);
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

    private MeshData buildMesh(net.minecraft.client.Minecraft mc, Vec3 center, float radius) {
        Camera camera = mc.gameRenderer.mainCamera();
        Matrix4f pose = cameraViewPose(camera);
        float cx = (float) center.x;
        float cy = (float) center.y + 0.04F;
        float cz = (float) center.z;
        float r = radius;

        Vector4f p1 = toViewSpace(pose, cx + r, cy, cz + r);
        Vector4f p2 = toViewSpace(pose, cx + r, cy, cz - r);
        Vector4f p3 = toViewSpace(pose, cx - r, cy, cz - r);
        Vector4f p4 = toViewSpace(pose, cx - r, cy, cz + r);

        BufferBuilder builder = new BufferBuilder(
                new ByteBufferBuilder(6 * DefaultVertexFormat.POSITION_TEX.getVertexSize()),
                PrimitiveTopology.TRIANGLES,
                DefaultVertexFormat.POSITION_TEX
        );
        addVertex(builder, p1, 1.0F, 1.0F);
        addVertex(builder, p2, 1.0F, 0.0F);
        addVertex(builder, p3, 0.0F, 0.0F);
        addVertex(builder, p1, 1.0F, 1.0F);
        addVertex(builder, p3, 0.0F, 0.0F);
        addVertex(builder, p4, 0.0F, 1.0F);
        return builder.buildOrThrow();
    }

    private static void addVertex(BufferBuilder builder, Vector4f point, float u, float v) {
        builder.addVertex(point.x, point.y, point.z).setUv(u, v);
    }

    private GpuBuffer uploadUniform(int color, float time, float alpha, boolean glowEdge) {
        var device = RenderSystem.getDevice();
        GpuBuffer buffer = device.createBuffer(
                () -> "Delta Jump Circle UBO",
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
                    .putVec4(time, glowEdge ? 1.0F : 0.0F, 0.0F, alpha)
                    .get();
            device.createCommandEncoder().writeToBuffer(buffer.slice(), data);
        }

        return buffer;
    }

    static Matrix4f cameraViewPose(Camera camera) {
        Vec3 cam = camera.position();
        return new Matrix4f().rotation(camera.rotation().invert(new Quaternionf()))
                .translate((float) -cam.x, (float) -cam.y, (float) -cam.z);
    }

    static Vector4f toViewSpace(Matrix4f viewPose, double x, double y, double z) {
        return new Vector4f((float) x, (float) y, (float) z, 1.0F).mul(viewPose);
    }
}
