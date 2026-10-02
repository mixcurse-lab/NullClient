package platform.client.utils.render.pipeline;

import platform.api.module.Interface;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fc;

public final class DeltaTextureRenderState extends DeltaElementRenderState {
    private final Identifier textureId;
    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float u0;
    private final float v0;
    private final float u1;
    private final float v1;
    private final int color;
    private final float cornerRadius;

    public DeltaTextureRenderState(
            Matrix3x2fc pose,
            Identifier textureId,
            float x,
            float y,
            float width,
            float height,
            float u0,
            float v0,
            float u1,
            float v1,
            int color,
            float cornerRadius,
            ScreenRectangle scissor
    ) {
        super(pose, scissor, x, y, width, height);
        this.textureId = textureId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.color = color;
        this.cornerRadius = cornerRadius;
    }

    public DeltaTextureRenderState(
            Matrix3x2fc pose,
            Identifier textureId,
            float x,
            float y,
            float width,
            float height,
            int color,
            ScreenRectangle scissor
    ) {
        this(pose, textureId, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, 0.0F, scissor);
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        float halfWidth = this.width * 0.5F;
        float halfHeight = this.height * 0.5F;
        int packedSizeX = VertexPacking.packSize(this.width);
        int packedSizeY = VertexPacking.packSize(this.height);

        addVertex(vertexConsumer, this.x, this.y, this.u0, this.v0, -halfWidth, -halfHeight, packedSizeX, packedSizeY);
        addVertex(vertexConsumer, this.x, this.y + this.height, this.u0, this.v1, -halfWidth, halfHeight, packedSizeX, packedSizeY);
        addVertex(vertexConsumer, this.x + this.width, this.y + this.height, this.u1, this.v1, halfWidth, halfHeight, packedSizeX, packedSizeY);
        addVertex(vertexConsumer, this.x + this.width, this.y, this.u1, this.v0, halfWidth, -halfHeight, packedSizeX, packedSizeY);
    }

    private void addVertex(VertexConsumer vertexConsumer, float x, float y, float u, float v, float localX, float localY, int packedSizeX, int packedSizeY) {
        vertexConsumer.addVertex(transformX(x, y), transformY(x, y), 0.0F)
                .setColor(this.color)
                .setUv(u, v)
                .setUv1(VertexPacking.packSignedSize(localX), VertexPacking.packSignedSize(localY))
                .setUv2(packedSizeX, packedSizeY)
                .setNormal(0.0F, 0.0F, 1.0F)
                .setLineWidth(Math.max(this.cornerRadius, 0.0F));
    }

    @Override
    public RenderPipeline pipeline() {
        return DeltaPipelines.TEXTURE;
    }

    @Override
    public TextureSetup textureSetup() {
        AbstractTexture texture = Interface.aM_.getTextureManager().getTexture(this.textureId);
        return TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler());
    }
}



