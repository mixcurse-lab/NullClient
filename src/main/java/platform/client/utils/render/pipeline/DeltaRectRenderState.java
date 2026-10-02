package platform.client.utils.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import org.joml.Matrix3x2fc;

public final class DeltaRectRenderState extends DeltaElementRenderState {
    private final float x0;
    private final float y0;
    private final float x1;
    private final float y1;
    private final int topLeftColor;
    private final int bottomLeftColor;
    private final int bottomRightColor;
    private final int topRightColor;
    private final float topLeftRadius;
    private final float topRightRadius;
    private final float bottomRightRadius;
    private final float bottomLeftRadius;
    private final float borderThickness;
    private final int borderColor;
    private final float shadowBlur;
    private final int shadowColor;
    private final boolean shadow;
    private final boolean isGradient;

    public DeltaRectRenderState(
            Matrix3x2fc pose,
            float x0,
            float y0,
            float x1,
            float y1,
            int topLeftColor,
            int bottomLeftColor,
            int bottomRightColor,
            int topRightColor,
            float topLeftRadius,
            float topRightRadius,
            float bottomRightRadius,
            float bottomLeftRadius,
            float borderThickness,
            int borderColor,
            float shadowBlur,
            int shadowColor,
            boolean shadow,
            ScreenRectangle scissor,
            boolean isGradient
    ) {
        super(
                pose,
                scissor,
                shadow ? x0 - spread(shadowBlur) : x0,
                shadow ? y0 - spread(shadowBlur) : y0,
                (x1 - x0) + (shadow ? spread(shadowBlur) * 2.0F : 0.0F),
                (y1 - y0) + (shadow ? spread(shadowBlur) * 2.0F : 0.0F)
        );
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.topLeftColor = topLeftColor;
        this.bottomLeftColor = bottomLeftColor;
        this.bottomRightColor = bottomRightColor;
        this.topRightColor = topRightColor;

        this.topLeftRadius = clampRadius(topLeftRadius);
        this.topRightRadius = clampRadius(topRightRadius);
        this.bottomRightRadius = clampRadius(bottomRightRadius);
        this.bottomLeftRadius = clampRadius(bottomLeftRadius);
        this.borderThickness = borderThickness;
        this.borderColor = borderColor;
        this.shadowBlur = shadowBlur;
        this.shadowColor = shadowColor;
        this.shadow = shadow;
        this.isGradient = isGradient;
    }

    public DeltaRectRenderState(
            Matrix3x2fc pose,
            float x0,
            float y0,
            float x1,
            float y1,
            int color,
            float radius,
            float borderThickness,
            int borderColor,
            ScreenRectangle scissor
    ) {
        this(pose, x0, y0, x1, y1, color, color, color, color, radius, radius, radius, radius, borderThickness, borderColor, 0.0F, 0x00000000, false, scissor, false);
    }

    public DeltaRectRenderState(
            Matrix3x2fc pose,
            float x0,
            float y0,
            float x1,
            float y1,
            int topLeftColor,
            int bottomLeftColor,
            int bottomRightColor,
            int topRightColor,
            float topLeftRadius,
            float topRightRadius,
            float bottomRightRadius,
            float bottomLeftRadius,
            float borderThickness,
            int borderColor,
            ScreenRectangle scissor
    ) {
        this(
                pose,
                x0,
                y0,
                x1,
                y1,
                topLeftColor,
                bottomLeftColor,
                bottomRightColor,
                topRightColor,
                topLeftRadius,
                topRightRadius,
                bottomRightRadius,
                bottomLeftRadius,
                borderThickness,
                borderColor,
                0.0F,
                0x00000000,
                false,
                scissor,
                false
        );
    }

    public static DeltaRectRenderState shadow(
            Matrix3x2fc pose,
            float x0,
            float y0,
            float x1,
            float y1,
            float topLeftRadius,
            float topRightRadius,
            float bottomRightRadius,
            float bottomLeftRadius,
            float shadowBlur,
            int shadowColor,
            ScreenRectangle scissor
    ) {
        return new DeltaRectRenderState(
                pose,
                x0,
                y0,
                x1,
                y1,
                0x00000000,
                0x00000000,
                0x00000000,
                0x00000000,
                topLeftRadius,
                topRightRadius,
                bottomRightRadius,
                bottomLeftRadius,
                0.0F,
                0x00000000,
                Math.max(shadowBlur, 1.0F),
                shadowColor,
                true,
                scissor,
                false
        );
    }

    public static DeltaRectRenderState gradient(
            Matrix3x2fc pose,
            float x0,
            float y0,
            float x1,
            float y1,
            int topLeftColor,
            int bottomLeftColor,
            int bottomRightColor,
            int topRightColor,
            float topLeftRadius,
            float topRightRadius,
            float bottomRightRadius,
            float bottomLeftRadius,
            ScreenRectangle scissor
    ) {
        return new DeltaRectRenderState(
                pose,
                x0,
                y0,
                x1,
                y1,
                topLeftColor,
                bottomLeftColor,
                bottomRightColor,
                topRightColor,
                topLeftRadius,
                topRightRadius,
                bottomRightRadius,
                bottomLeftRadius,
                0.0F,
                0x00000000,
                0.0F,
                0x00000000,
                false,
                scissor,
                true
        );
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        if (this.isGradient) {
            buildGradientVertices(vertexConsumer);
            return;
        }

        float halfWidth = (this.x1 - this.x0) * 0.5F;
        float halfHeight = (this.y1 - this.y0) * 0.5F;

        float spread = this.shadow ? spread(this.shadowBlur) : 0.0F;

        int effectColor = this.shadow ? this.shadowColor : this.borderColor;
        float packedZ = VertexPacking.packZ(
                this.shadow ? this.shadowBlur : this.borderThickness,
                (effectColor >>> 24) & 0xFF,
                this.shadow
        );
        int packedSizeX = VertexPacking.packSize(halfWidth * 2.0F);
        int packedSizeY = VertexPacking.packSize(halfHeight * 2.0F);
        int packedTopLeft = VertexPacking.packRadius(this.topLeftRadius);
        int packedTopRight = VertexPacking.packRadius(this.topRightRadius);
        float packedBottom = VertexPacking.packDual12(this.bottomRightRadius, this.bottomLeftRadius);

        float effectRed = VertexPacking.snormChannel((effectColor >> 16) & 0xFF);
        float effectGreen = VertexPacking.snormChannel((effectColor >> 8) & 0xFF);
        float effectBlue = VertexPacking.snormChannel(effectColor & 0xFF);

        float localX = halfWidth + spread;
        float localY = halfHeight + spread;
        addVertex(vertexConsumer, this.x0 - spread, this.y0 - spread, -localX, -localY, packedZ, packedSizeX, packedSizeY, packedTopLeft, packedTopRight, packedBottom, effectRed, effectGreen, effectBlue, this.topLeftColor);
        addVertex(vertexConsumer, this.x0 - spread, this.y1 + spread, -localX, localY, packedZ, packedSizeX, packedSizeY, packedTopLeft, packedTopRight, packedBottom, effectRed, effectGreen, effectBlue, this.bottomLeftColor);
        addVertex(vertexConsumer, this.x1 + spread, this.y1 + spread, localX, localY, packedZ, packedSizeX, packedSizeY, packedTopLeft, packedTopRight, packedBottom, effectRed, effectGreen, effectBlue, this.bottomRightColor);
        addVertex(vertexConsumer, this.x1 + spread, this.y0 - spread, localX, -localY, packedZ, packedSizeX, packedSizeY, packedTopLeft, packedTopRight, packedBottom, effectRed, effectGreen, effectBlue, this.topRightColor);
    }

    private void buildGradientVertices(VertexConsumer vertexConsumer) {
        int subdivisions = 8;
        float width = this.x1 - this.x0;
        float height = this.y1 - this.y0;
        float halfWidth = width * 0.5F;
        float halfHeight = height * 0.5F;
        float centerX = this.x0 + halfWidth;
        float centerY = this.y0 + halfHeight;

        float packedZ = VertexPacking.packZ(0.0F, 0, false);
        int packedSizeX = VertexPacking.packSize(width);
        int packedSizeY = VertexPacking.packSize(height);
        int packedTopLeft = VertexPacking.packRadius(this.topLeftRadius);
        int packedTopRight = VertexPacking.packRadius(this.topRightRadius);
        float packedBottom = VertexPacking.packDual12(this.bottomRightRadius, this.bottomLeftRadius);

        float effectRed = VertexPacking.snormChannel(0);
        float effectGreen = VertexPacking.snormChannel(0);
        float effectBlue = VertexPacking.snormChannel(0);

        for (int row = 0; row < subdivisions; row++) {
            for (int column = 0; column < subdivisions; column++) {
                float u0 = (float) column / subdivisions;
                float u1 = (float) (column + 1) / subdivisions;
                float v0 = (float) row / subdivisions;
                float v1 = (float) (row + 1) / subdivisions;

                int colorTopLeft = bilinearColor(u0, v0);
                int colorBottomLeft = bilinearColor(u0, v1);
                int colorBottomRight = bilinearColor(u1, v1);
                int colorTopRight = bilinearColor(u1, v0);

                float sx0 = this.x0 + u0 * width;
                float sx1 = this.x0 + u1 * width;
                float sy0 = this.y0 + v0 * height;
                float sy1 = this.y0 + v1 * height;

                addVertex(vertexConsumer, sx0, sy0, sx0 - centerX, sy0 - centerY, packedZ, packedSizeX, packedSizeY, packedTopLeft, packedTopRight, packedBottom, effectRed, effectGreen, effectBlue, colorTopLeft);
                addVertex(vertexConsumer, sx0, sy1, sx0 - centerX, sy1 - centerY, packedZ, packedSizeX, packedSizeY, packedTopLeft, packedTopRight, packedBottom, effectRed, effectGreen, effectBlue, colorBottomLeft);
                addVertex(vertexConsumer, sx1, sy1, sx1 - centerX, sy1 - centerY, packedZ, packedSizeX, packedSizeY, packedTopLeft, packedTopRight, packedBottom, effectRed, effectGreen, effectBlue, colorBottomRight);
                addVertex(vertexConsumer, sx1, sy0, sx1 - centerX, sy0 - centerY, packedZ, packedSizeX, packedSizeY, packedTopLeft, packedTopRight, packedBottom, effectRed, effectGreen, effectBlue, colorTopRight);
            }
        }
    }

    private int bilinearColor(float u, float v) {
        int top = mixColor(this.topLeftColor, this.topRightColor, u);
        int bottom = mixColor(this.bottomLeftColor, this.bottomRightColor, u);
        return mixColor(top, bottom, v);
    }

    private static int mixColor(int a, int b, float t) {
        int aA = (a >>> 24) & 0xFF;
        int aR = (a >>> 16) & 0xFF;
        int aG = (a >>> 8) & 0xFF;
        int aB = a & 0xFF;
        int bA = (b >>> 24) & 0xFF;
        int bR = (b >>> 16) & 0xFF;
        int bG = (b >>> 8) & 0xFF;
        int bB = b & 0xFF;
        int rA = Math.round(aA + (bA - aA) * t);
        int rR = Math.round(aR + (bR - aR) * t);
        int rG = Math.round(aG + (bG - aG) * t);
        int rB = Math.round(aB + (bB - aB) * t);
        return (rA << 24) | (rR << 16) | (rG << 8) | rB;
    }

    private void addVertex(
            VertexConsumer vertexConsumer,
            float x,
            float y,
            float localX,
            float localY,
            float packedZ,
            int packedSizeX,
            int packedSizeY,
            int packedTopLeft,
            int packedTopRight,
            float packedBottom,
            float effectRed,
            float effectGreen,
            float effectBlue,
            int color
    ) {
        vertexConsumer.addVertex(transformX(x, y), transformY(x, y), packedZ)
                .setColor(color)
                .setUv(localX, localY)
                .setUv1(packedSizeX, packedSizeY)
                .setUv2(packedTopLeft, packedTopRight)
                .setNormal(effectRed, effectGreen, effectBlue)
                .setLineWidth(packedBottom);
    }

    private static float clampRadius(float radius) {
        return VertexPacking.clamp(radius, 0.0F, VertexPacking.MAX_DUAL_RADIUS);
    }

    private static float spread(float blur) {
        return Math.max(blur, 1.0F);
    }

    @Override
    public RenderPipeline pipeline() {
        return DeltaPipelines.RECT;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }
}



