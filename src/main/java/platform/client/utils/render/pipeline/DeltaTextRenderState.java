package platform.client.utils.render.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fc;

import java.util.List;

import static platform.api.module.Interface.aM_;

public final class DeltaTextRenderState extends DeltaElementRenderState {
    private final float size;
    private final int color;
    private final int outlineColor;
    private final float outlineThickness;
    private final float fontWeight;
    private final float distanceRange;
    private final Identifier fontTexture;
    private final List<GlyphData> glyphs;
    private TextureSetup cachedTextureSetup;

    public DeltaTextRenderState(
            Matrix3x2fc pose,
            float x,
            float y,
            float size,
            int color,
            int outlineColor,
            float outlineThickness,
            float fontWeight,
            float distanceRange,
            Identifier fontTexture,
            List<GlyphData> glyphs,
            float totalWidth,
            float totalHeight,
            ScreenRectangle scissor
    ) {
        super(pose, scissor, x, y, totalWidth, totalHeight);
        this.size = size;
        this.color = color;
        this.outlineColor = outlineColor;
        this.outlineThickness = outlineThickness;
        this.fontWeight = fontWeight;
        this.distanceRange = distanceRange;
        this.fontTexture = fontTexture;
        this.glyphs = glyphs;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        int packedOutlineRg = VertexPacking.packU8Pair((this.outlineColor >> 16) & 0xFF, (this.outlineColor >> 8) & 0xFF);
        int packedOutlineBa = VertexPacking.packU8Pair(this.outlineColor & 0xFF, (this.outlineColor >>> 24) & 0xFF);
        int packedPxRange = VertexPacking.packRadius(this.distanceRange);

        for (GlyphData glyph : this.glyphs) {
            float localPxPerSdfUnit = glyph.sdfScale;
            addVertex(vertexConsumer, glyph.x0, glyph.y0, glyph.u0, glyph.v0, packedOutlineRg, packedOutlineBa, packedPxRange, localPxPerSdfUnit);
            addVertex(vertexConsumer, glyph.x0, glyph.y1, glyph.u0, glyph.v1, packedOutlineRg, packedOutlineBa, packedPxRange, localPxPerSdfUnit);
            addVertex(vertexConsumer, glyph.x1, glyph.y1, glyph.u1, glyph.v1, packedOutlineRg, packedOutlineBa, packedPxRange, localPxPerSdfUnit);
            addVertex(vertexConsumer, glyph.x1, glyph.y0, glyph.u1, glyph.v0, packedOutlineRg, packedOutlineBa, packedPxRange, localPxPerSdfUnit);
        }
    }

    private void addVertex(
            VertexConsumer vertexConsumer,
            float x,
            float y,
            float u,
            float v,
            int packedOutlineRg,
            int packedOutlineBa,
            int packedPxRange,
            float localPxPerSdfUnit
    ) {
        vertexConsumer.addVertex(transformX(x, y), transformY(x, y), localPxPerSdfUnit)
                .setColor(this.color)
                .setUv(u, v)
                .setUv1(packedOutlineRg, packedOutlineBa)
                .setUv2(packedPxRange, 0)
                .setNormal(this.fontWeight, 0.0F, 0.0F)
                .setLineWidth(this.outlineThickness);
    }

    @Override
    public RenderPipeline pipeline() {
        return DeltaPipelines.TEXT;
    }

    @Override
    public TextureSetup textureSetup() {
        if (this.cachedTextureSetup == null) {
            AbstractTexture texture = aM_.getTextureManager().getTexture(this.fontTexture);
            this.cachedTextureSetup = TextureSetup.singleTexture(
                    texture.getTextureView(),
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR, false)
            );
        }
        return this.cachedTextureSetup;
    }

    public record GlyphData(
            float x0,
            float y0,
            float x1,
            float y1,
            float u0,
            float v0,
            float u1,
            float v1,
            float sdfScale
    ) {
    }
}



