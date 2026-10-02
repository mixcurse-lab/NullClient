package platform.client.features.modules.combat;

import platform.api.handlers.Handler_2;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.ColorUtil;

import platform.api.system.configs.ThemeInfo;
import platform.api.event.interfaces.EventTarget;
import platform.api.event.GlobalEvent;
import platform.api.module.Interface;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.handlers.BaseHandler;
import platform.client.features.modules.combat.Aura;

import platform.client.utils.render.AnimationUtil;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Generated;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;

@Handler_2
public class AuraHandler extends BaseHandler implements Interface {
    private static final Identifier BLOOM_TEXTURE = Identifier.parse("delta:pictures/bloom.png");

    private static final RenderPipeline AURA_FILLED = RenderPipeline.builder()
            .withLocation(Identifier.parse("delta:pipeline/aura_filled"))
            .withVertexShader(Identifier.parse("delta:core/aura_filled"))
            .withFragmentShader(Identifier.parse("delta:core/aura_filled"))
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE)))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withCull(false)
            .build();

    private static final RenderPipeline AURA_LINE = RenderPipeline.builder()
            .withLocation(Identifier.parse("delta:pipeline/aura_line"))
            .withVertexShader(Identifier.parse("delta:core/aura_filled"))
            .withFragmentShader(Identifier.parse("delta:core/aura_filled"))
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE)))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.LINES)
            .withCull(false)
            .build();

    private static final RenderPipeline AURA_BLOOM = RenderPipeline.builder()
            .withLocation(Identifier.parse("delta:pipeline/aura_bloom"))
            .withVertexShader(Identifier.parse("delta:core/aura_bloom"))
            .withFragmentShader(Identifier.parse("delta:core/aura_bloom"))
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
            .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE)))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withCull(false)
            .build();

    private final Vector3f[] b = {new Vector3f(0.0f, 1.5f, 0.0f), new Vector3f(0.0f, -1.5f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(-1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 0.0f, -1.0f)};
    private final int[][] c = {new int[]{0, 4, 2}, new int[]{0, 3, 4}, new int[]{0, 5, 3}, new int[]{0, 2, 5}, new int[]{1, 2, 4}, new int[]{1, 4, 3}, new int[]{1, 3, 5}, new int[]{1, 5, 2}};
    private final float[] d = {1.0f, 0.8f, 0.6f, 0.9f, 0.7f, 0.5f, 0.4f, 0.6f};
    private final AnimationUtil e = new AnimationUtil();
    private LivingEntity f;

    @Generated
    public AnimationUtil a() {
        return this.e;
    }

    @EventTarget
    public void a(DrawEvent event) {
        this.e.a(0.0f, 1.0f, 0.2f, EasingList.g, event.g());
        float moving = ((System.currentTimeMillis() % 360000) / 2.5f) + this.e.c();
        if (event.c() && this.f != null) {
            float anim = this.e.c();
            if (anim > 0.0f) {
                int themeColor = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
                Vec3 renderPos = a(this.f, event.g());
                float ringWidth = this.f.getBbWidth() * 1.5f;
                float ringScale = 1.25f - (0.5f * anim);
                if (Delta.h().d().t().B().r().l("Круг")) {
                    a(event.h(), renderPos, ColorUtil.a(themeColor, anim));
                } else {
                    a(event.h(), renderPos, ringWidth, ringScale, moving, ColorUtil.a(themeColor, anim));
                    a(event.h(), renderPos, ringWidth, ringScale, moving, ColorUtil.a(themeColor, anim * 0.2f), anim);
                }
            }
        }
    }

    @EventTarget
    public void a(GlobalEvent event) {
        if (aM_.player != null) {
            Delta.h().d().t().B().incrementTicks();
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        Aura aura = Delta.h().d().t().B();
        LivingEntity current = aura.s() != null ? aura.s() : Delta.h().d().t().X().s();
        boolean changed = (current == null || this.f == null || current == this.f) ? false : true;
        boolean visible = (current == null || changed) ? false : true;
        if (visible) {
            this.f = current;
        }
        this.e.a(visible);
        if (!visible && this.e.a() <= 0.0f) {
            this.f = changed ? current : null;
        }
    }

    private static Matrix4f viewPose(Camera camera) {
        Vec3 cam = camera.position();
        return new Matrix4f().rotation(camera.rotation().invert(new Quaternionf())).translate((float) -cam.x, (float) -cam.y, (float) -cam.z);
    }

    private static Matrix4f billboardPose(Camera camera, Vec3 worldPos) {
        Vec3 cam = camera.position();
        return new Matrix4f().rotation(camera.rotation().invert(new Quaternionf()))
                .translate((float) (worldPos.x - cam.x), (float) (worldPos.y - cam.y), (float) (worldPos.z - cam.z))
                .rotate(camera.rotation());
    }

    private void a(PoseStack stack, Vec3 renderPos, float ringWidth, float ringScale, float moving, int color) {
        Camera camera = aM_.gameRenderer.mainCamera();
        Vec3 targetCenter = renderPos.add(0.0d, ((double) this.f.getBbHeight()) / 2.0d, 0.0d);
        Matrix4f view = viewPose(camera);
        try (ByteBufferBuilder bbBuilder = new ByteBufferBuilder(DefaultVertexFormat.POSITION_COLOR.getVertexSize() * (this.c.length * 4 * 18))) {
            BufferBuilder buffer = new BufferBuilder(bbBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i < 360; i += 20) {
                float angle = (float) Math.toRadians(i + (moving * 0.3f));
                float offsetX = ((float) Math.sin(angle)) * ringWidth * ringScale;
                float offsetZ = ((float) Math.cos(angle)) * ringWidth * ringScale;
                float offsetY = 0.1f + (this.f.getBbHeight() * Math.abs((float) Math.sin(i)));
                Vec3 crystalPos = renderPos.add(offsetX, offsetY, offsetZ);
                Matrix4f pose = new Matrix4f(view).translate((float) crystalPos.x, (float) crystalPos.y, (float) crystalPos.z).rotate(new Quaternionf().rotationTo(new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f((float) (targetCenter.x - crystalPos.x), (float) (targetCenter.y - crystalPos.y), (float) (targetCenter.z - crystalPos.z)).normalize())).scale(0.1f, 0.1f, 0.1f);
                a(pose, buffer, color);
            }
            drawWorld("Delta-Aura-Filled", buffer, AURA_FILLED, PrimitiveTopology.QUADS, this.c.length * 18 * 6);
        }
    }

    private static com.mojang.blaze3d.buffers.GpuBufferSlice levelProjection() {
        com.mojang.blaze3d.buffers.GpuBufferSlice slice = platform.client.utils.render.LevelProjection.get();
        return slice != null ? slice : RenderSystem.getProjectionMatrixBuffer();
    }

    private void drawWorld(String name, BufferBuilder buffer, RenderPipeline pipeline, PrimitiveTopology topology, int indexCount) {
        try (MeshData mesh = buffer.buildOrThrow()) {
            GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> name, 32, mesh.vertexBuffer());
            try {
                var renderTarget = aM_.gameRenderer.mainRenderTarget();
                try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> name, renderTarget.getColorTextureView(), java.util.Optional.empty(), renderTarget.getDepthTextureView(), java.util.OptionalDouble.empty())) {
                    renderPass.setPipeline(pipeline);
                    renderPass.setUniform("Projection", levelProjection());
                    renderPass.setVertexBuffer(0, gpuBuffer.slice());
                    GpuBuffer indexBuffer = RenderSystem.getSequentialBuffer(topology).getBuffer(indexCount);
                    renderPass.setIndexBuffer(indexBuffer, RenderSystem.getSequentialBuffer(topology).type());
                    renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
                }
            } finally {
                gpuBuffer.close();
            }
        }
    }

    private void drawWorldTextured(String name, BufferBuilder buffer, RenderPipeline pipeline, PrimitiveTopology topology, int indexCount, Identifier textureId) {
        AbstractTexture texture = aM_.getTextureManager().getTexture(textureId);
        GpuTextureView textureView = texture.getTextureView();
        GpuSampler sampler = texture.getSampler();
        try (MeshData mesh = buffer.buildOrThrow()) {
            GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> name, 32, mesh.vertexBuffer());
            try {
                var renderTarget = aM_.gameRenderer.mainRenderTarget();
                try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                        () -> name, renderTarget.getColorTextureView(), java.util.Optional.empty(), renderTarget.getDepthTextureView(), java.util.OptionalDouble.empty())) {
                    renderPass.setPipeline(pipeline);
                    renderPass.setUniform("Projection", levelProjection());
                    renderPass.bindTexture("Sampler0", textureView, sampler);
                    renderPass.setVertexBuffer(0, gpuBuffer.slice());
                    GpuBuffer indexBuffer = RenderSystem.getSequentialBuffer(topology).getBuffer(indexCount);
                    renderPass.setIndexBuffer(indexBuffer, RenderSystem.getSequentialBuffer(topology).type());
                    renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
                }
            } finally {
                gpuBuffer.close();
            }
        }
    }

    private void a(Matrix4f pose, BufferBuilder buffer, int color) {
        int[] rgba = ColorUtil.b(color);
        float red = rgba[0] / 255.0f;
        float green = rgba[1] / 255.0f;
        float blue = rgba[2] / 255.0f;
        float alpha = rgba[3] / 255.0f;
        for (int i = 0; i < this.c.length; i++) {
            int[] face = this.c[i];
            float brightness = this.d[i];
            float r = red * brightness;
            float g = green * brightness;
            float b = blue * brightness;
            Vector3f v0 = this.b[face[0]];
            Vector3f v1 = this.b[face[1]];
            Vector3f v2 = this.b[face[2]];
            buffer.addVertex(pose, v0.x, v0.y, v0.z).setColor(r, g, b, alpha);
            buffer.addVertex(pose, v1.x, v1.y, v1.z).setColor(r, g, b, alpha);
            buffer.addVertex(pose, v2.x, v2.y, v2.z).setColor(r, g, b, alpha);
            buffer.addVertex(pose, v2.x, v2.y, v2.z).setColor(r, g, b, alpha);
        }
    }

    private void a(PoseStack stack, Vec3 renderPos, float ringWidth, float ringScale, float moving, int color, float anim) {
        int[] rgba = ColorUtil.b(color);
        int red = rgba[0];
        int green = rgba[1];
        int blue = rgba[2];
        int alpha = rgba[3];
        a(renderPos, ringWidth, ringScale, moving, 1.5f * anim, red, green, blue, alpha);
        a(renderPos, ringWidth, ringScale, moving, 0.6f * anim, red, green, blue, alpha);
    }

    private void a(Vec3 renderPos, float ringWidth, float ringScale, float moving, float size, int red, int green, int blue, int alpha) {
        Camera camera = aM_.gameRenderer.mainCamera();
        float half = size / 2.0f;
        float r = red / 255.0f;
        float g = green / 255.0f;
        float b = blue / 255.0f;
        float a = alpha / 255.0f;
        try (ByteBufferBuilder bbBuilder = new ByteBufferBuilder(DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize() * (4 * 18))) {
            BufferBuilder buffer = new BufferBuilder(bbBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (int i = 0; i < 360; i += 20) {
                float angle = (float) Math.toRadians(i + (moving * 0.3f));
                float offsetX = ((float) Math.sin(angle)) * ringWidth * ringScale;
                float offsetZ = ((float) Math.cos(angle)) * ringWidth * ringScale;
                float offsetY = 0.1f + (this.f.getBbHeight() * Math.abs((float) Math.sin(i)));
                Matrix4f pose = billboardPose(camera, new Vec3(renderPos.x + ((double) offsetX), renderPos.y + ((double) offsetY), renderPos.z + ((double) offsetZ)));
                buffer.addVertex(pose, -half, -half, 0.0f).setUv(0.0f, 0.0f).setColor(r, g, b, a);
                buffer.addVertex(pose, -half, half, 0.0f).setUv(0.0f, 1.0f).setColor(r, g, b, a);
                buffer.addVertex(pose, half, half, 0.0f).setUv(1.0f, 1.0f).setColor(r, g, b, a);
                buffer.addVertex(pose, half, -half, 0.0f).setUv(1.0f, 0.0f).setColor(r, g, b, a);
            }
            drawWorldTextured("Delta-Aura-Bloom", buffer, AURA_BLOOM, PrimitiveTopology.QUADS, 18 * 6, BLOOM_TEXTURE);
        }
    }

    private void a(PoseStack stack, Vec3 renderPos, int color) {
        Camera camera = aM_.gameRenderer.mainCamera();
        Matrix4f viewPoseMatrix = viewPose(camera);
        float height = this.f.getBbHeight() + 0.15f;
        float radius = this.f.getBbWidth() * 0.8f;
        double time = System.currentTimeMillis() % 1750.0d;
        boolean inverted = time > 875.0d;
        double progress = time / 875.0d;
        double progress2 = inverted ? progress - 1.0d : 1.0d - progress;
        double ease = progress2 < 0.5d ? 2.0d * progress2 * progress2 : 1.0d - (Math.pow(((-2.0d) * progress2) + 2.0d, 2.0d) / 2.0d);
        float y = (float) (renderPos.y + (((double) height) * ease));
        float yOffset = (float) (((double) height) * 0.8000001435473696d * Math.min(ease, 1.0d - ease) * (inverted ? -1.0d : 1.0d));
        int[] c = ColorUtil.b(color);
        float r = c[0] / 255.0f;
        float g = c[1] / 255.0f;
        float b = c[2] / 255.0f;
        float a = c[3] / 255.0f;
        double cx = renderPos.x;
        double cz = renderPos.z;
        int segmentCount = 72;
        try (ByteBufferBuilder bbBuilder = new ByteBufferBuilder(DefaultVertexFormat.POSITION_COLOR.getVertexSize() * (4 * segmentCount))) {
            BufferBuilder skirt = new BufferBuilder(bbBuilder, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (int deg = 0; deg < segmentCount; deg++) {
                float a0 = (float) Math.toRadians((deg * 360.0d) / segmentCount);
                float a1 = (float) Math.toRadians(((deg + 1) * 360.0d) / segmentCount);
                float x0 = (float) (cx + (Math.cos(a0) * ((double) radius)));
                float z0 = (float) (cz + (Math.sin(a0) * ((double) radius)));
                float x1 = (float) (cx + (Math.cos(a1) * ((double) radius)));
                float z1 = (float) (cz + (Math.sin(a1) * ((double) radius)));
                skirt.addVertex(viewPoseMatrix, x0, y, z0).setColor(r, g, b, a * 0.55f);
                skirt.addVertex(viewPoseMatrix, x1, y, z1).setColor(r, g, b, a * 0.55f);
                skirt.addVertex(viewPoseMatrix, x1, y + yOffset, z1).setColor(r, g, b, 0.0f);
                skirt.addVertex(viewPoseMatrix, x0, y + yOffset, z0).setColor(r, g, b, 0.0f);
            }
            drawWorld("Delta-Aura-Skirt", skirt, AURA_FILLED, PrimitiveTopology.QUADS, segmentCount * 6);
        }
        try (ByteBufferBuilder bbBuilder = new ByteBufferBuilder(DefaultVertexFormat.POSITION_COLOR.getVertexSize() * (2 * 360))) {
            BufferBuilder outline = new BufferBuilder(bbBuilder, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR);
            for (int deg2 = 0; deg2 < 360; deg2++) {
                double a0 = Math.toRadians(deg2);
                double a1 = Math.toRadians(deg2 + 1);
                outline.addVertex(viewPoseMatrix, (float) (cx + (Math.cos(a0) * ((double) radius))), y, (float) (cz + (Math.sin(a0) * ((double) radius)))).setColor(r, g, b, a);
                outline.addVertex(viewPoseMatrix, (float) (cx + (Math.cos(a1) * ((double) radius))), y, (float) (cz + (Math.sin(a1) * ((double) radius)))).setColor(r, g, b, a);
            }
            drawWorld("Delta-Aura-Outline", outline, AURA_LINE, PrimitiveTopology.LINES, 2 * 360);
        }
    }

    private Vec3 a(LivingEntity target, float tickDelta) {
        return new Vec3(net.minecraft.util.Mth.lerp(tickDelta, target.xOld, target.getX()), net.minecraft.util.Mth.lerp(tickDelta, target.yOld, target.getY()), net.minecraft.util.Mth.lerp(tickDelta, target.zOld, target.getZ()));
    }
}



