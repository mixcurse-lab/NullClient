package platform.client.utils.render;

import platform.client.processors.draw.fonts.FontData;
import java.util.Map;

public class MsdfGlyph {
    private final int unicode;
    private final float advance;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private final float planeWidth;
    private final float planeHeight;
    private final float planeTop;
    private final float sdfScale;

    public static final class a {
        private final int unicode;
        private final int color;

        public a(int c, int color) {
            this.unicode = c;
            this.color = color;
        }

        public int unicode() { return this.unicode; }
        public int color() { return this.color; }
    }

    public MsdfGlyph(FontData.GlyphData data, float atlasWidth, float atlasHeight, float range) {
        this.unicode = data.unicode();
        this.advance = data.advance();
        FontData.BoundsData atlasBounds = data.atlasBounds();
        if (atlasBounds != null) {
            this.u0 = atlasBounds.left() / atlasWidth;
            this.u1 = atlasBounds.right() / atlasWidth;
            this.v0 = 1.0f - (atlasBounds.top() / atlasHeight);
            this.v1 = 1.0f - (atlasBounds.bottom() / atlasHeight);
        } else {
            this.u0 = 0.0f; this.u1 = 0.0f;
            this.v0 = 0.0f; this.v1 = 0.0f;
        }
        FontData.BoundsData planeBounds = data.planeBounds();
        if (planeBounds != null) {
            this.planeWidth = planeBounds.right() - planeBounds.left();
            this.planeHeight = planeBounds.top() - planeBounds.bottom();
            this.planeTop = planeBounds.top();
        } else {
            this.planeWidth = 0.0f;
            this.planeHeight = 0.0f;
            this.planeTop = 0.0f;
        }
        this.sdfScale = range > 0 ? (1.0f / range) : 0.0f;
    }

    public float getAdvance(float size) { return this.advance * size; }
    public float getPlaneWidth(float size) { return this.planeWidth * size; }
    public float getPlaneHeight(float size) { return this.planeHeight * size; }
    public float getPlaneTop() { return this.planeTop; }
    public float getSdfScale() { return this.sdfScale; }
    public int getUnicode() { return this.unicode; }
    public float getU0() { return this.u0; }
    public float getU1() { return this.u1; }
    public float getV0() { return this.v0; }
    public float getV1() { return this.v1; }
}



