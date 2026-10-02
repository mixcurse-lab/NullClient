package platform.client.utils.render.pipeline;

import platform.client.utils.render.DeltaBlurProcessor;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import org.joml.Matrix3x2fc;

public final class DeltaFrostedRenderState extends DeltaElementRenderState {
    private static final float PADDING = 0.0F;

    private final float x0;
    private final float y0;
    private final float x1;
    private final float y1;
    private final int color;
    private final float topLeftRadius;
    private final float topRightRadius;
    private final float bottomRightRadius;
    private final float bottomLeftRadius;
    private final float mix;
    private final float smoothness;
    private final float alphaMul;
    private final boolean shadow;

    public DeltaFrostedRenderState(
            Matrix3x2fc pose,
            float x0,
            float y0,
            float x1,
            float y1,
            int color,
            float topLeftRadius,
            float topRightRadius,
            float bottomRightRadius,
            float bottomLeftRadius,
            float mix,
            float smoothness,
            float alphaMul,
            boolean shadow,
            ScreenRectangle scissor
    ) {
        super(pose, scissor, x0 - PADDING, y0 - PADDING, (x1 - x0) + PADDING * 2.0F, (y1 - y0) + PADDING * 2.0F);
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.color = color;
        this.topLeftRadius = clampRadius(topLeftRadius);
        this.topRightRadius = clampRadius(topRightRadius);
        this.bottomRightRadius = clampRadius(bottomRightRadius);
        this.bottomLeftRadius = clampRadius(bottomLeftRadius);
        this.mix = mix;
        this.smoothness = smoothness;
        this.alphaMul = alphaMul;
        this.shadow = shadow;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        int packedSizeX = VertexPacking.packSize(this.x1 - this.x0);
        int packedSizeY = VertexPacking.packSize(this.y1 - this.y0);
        int packedTopLeft = VertexPacking.packRadius(this.topLeftRadius);
        int packedTopRight = VertexPacking.packRadius(this.topRightRadius);
        float packedBottom = VertexPacking.packDual12(this.bottomRightRadius, this.bottomLeftRadius);
        int packedMix = VertexPacking.encodeUnit01(this.mix);
        int packedSmoothness = VertexPacking.encodeUnit01(this.smoothness);
        int packedAlphaMul = VertexPacking.encodeUnit01(this.alphaMul);
        float mixChannel = VertexPacking.snormChannel(packedMix);
        float smoothnessChannel = VertexPacking.snormChannel(packedSmoothness);
        float alphaMulChannel = VertexPacking.snormChannel(packedAlphaMul);

        this.addVertex(vertexConsumer, this.x0 - PADDING, this.y0 - PADDING, 0.0F, 0.0F, packedSizeX, packedSizeY, packedTopLeft, packedTopRight, packedBottom, mixChannel, smoothnessChannel, alphaMulChannel, this.color);
        this.addVertex(vertexConsumer, this.x0 - PADDING, this.y1 + PADDING, 0.0F, 1.0F, packedSizeX, packedSizeY, packedTopLeft, packedTopRight, packedBottom, mixChannel, smoothnessChannel, alphaMulChannel, this.color);
        this.addVertex(vertexConsumer, this.x1 + PADDING, this.y1 + PADDING, 1.0F, 1.0F, packedSizeX, packedSizeY, packedTopLeft, packedTopRight, packedBottom, mixChannel, smoothnessChannel, alphaMulChannel, this.color);
        this.addVertex(vertexConsumer, this.x1 + PADDING, this.y0 - PADDING, 1.0F, 0.0F, packedSizeX, packedSizeY, packedTopLeft, packedTopRight, packedBottom, mixChannel, smoothnessChannel, alphaMulChannel, this.color);
    }

    private void addVertex(
            VertexConsumer vertexConsumer,
            float x,
            float y,
            float uvX,
            float uvY,
            int packedSizeX,
            int packedSizeY,
            int packedTopLeft,
            int packedTopRight,
            float packedBottom,
            float mixChannel,
            float smoothnessChannel,
            float alphaMulChannel,
            int color
    ) {
        vertexConsumer.addVertex(transformX(x, y), transformY(x, y), 0.0F)
                .setColor(color)
                .setUv(uvX, uvY)
                .setUv1(packedSizeX, packedSizeY)
                .setUv2(packedTopLeft, packedTopRight)
                .setNormal(mixChannel, smoothnessChannel, alphaMulChannel)
                .setLineWidth(packedBottom);
    }

    private static float clampRadius(float radius) {
        return VertexPacking.clamp(radius, 0.0F, VertexPacking.MAX_DUAL_RADIUS);
    }

    @Override
    public RenderPipeline pipeline() {
        return this.shadow ? DeltaPipelines.FROSTED_SHADOW : DeltaPipelines.FROSTED;
    }

    @Override
    public TextureSetup textureSetup() {
        DeltaBlurProcessor processor = DeltaBlurProcessor.getInstance();
        GpuTextureView view = processor.getBlurView();
        GpuSampler sampler = processor.getBlurSampler();
        if (view != null && sampler != null) {
            return TextureSetup.singleTexture(view, sampler);
        }
        RenderTarget mainTarget = net.minecraft.client.Minecraft.getInstance().gameRenderer.mainRenderTarget();
        if (mainTarget != null && mainTarget.getColorTextureView() != null && sampler != null) {
            return TextureSetup.singleTexture(mainTarget.getColorTextureView(), sampler);
        }
        return TextureSetup.noTexture();
    }
}



