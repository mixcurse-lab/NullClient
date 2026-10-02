package platform.client.utils.render;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import platform.client.processors.draw.fonts.FontData;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MsdfFont {
    private static final Gson GSON = new Gson();
    private static final Map<Identifier, MsdfFont> CACHE = new ConcurrentHashMap<>();

    private final Identifier textureId;
    private final float distanceRange;
    private final int atlasWidth;
    private final int atlasHeight;
    private final float atlasSize;
    private final float lineHeightEm;
    private final float ascenderEm;
    private final float descenderEm;
    private final Int2ObjectMap<Glyph> glyphs;
    private final Long2FloatMap kerning;
    private final Glyph fallbackGlyph;
    private TextureSetup textureSetup;

    private MsdfFont(Identifier metadataId, FontFile file) {
        this.textureId = metadataId.withPath(path -> path.replace(".json", ".png"));
        this.distanceRange = (float) file.atlas.distanceRange;
        this.atlasWidth = file.atlas.width;
        this.atlasHeight = file.atlas.height;
        this.atlasSize = (float) file.atlas.size;
        this.lineHeightEm = (float) file.metrics.lineHeight;
        this.ascenderEm = (float) file.metrics.ascender;
        this.descenderEm = (float) file.metrics.descender;

        this.glyphs = new Int2ObjectOpenHashMap<>(file.glyphs.size());
        for (RawGlyph raw : file.glyphs) {
            this.glyphs.put(raw.unicode, Glyph.bake(raw, this.atlasWidth, this.atlasHeight));
        }
        this.fallbackGlyph = this.glyphs.get('?');

        this.kerning = new Long2FloatOpenHashMap();
        this.kerning.defaultReturnValue(0.0F);
        if (file.kerning != null) {
            for (KerningPair pair : file.kerning) {
                this.kerning.put(packKerningKey(pair.unicode1, pair.unicode2), (float) pair.advance);
            }
        }
    }

    public static MsdfFont load(Identifier metadataId) {
        return CACHE.computeIfAbsent(metadataId, MsdfFont::readFont);
    }

    public float lineHeight(float size) { return this.lineHeightEm * size; }
    public float ascender(float size) { return this.ascenderEm * size; }
    public float textHeight(float size) { return (this.ascenderEm - this.descenderEm) * size; }
    public float distanceRange() { return this.distanceRange; }
    public int atlasWidth() { return this.atlasWidth; }
    public int atlasHeight() { return this.atlasHeight; }
    public float atlasSize() { return this.atlasSize; }
    public float localPxPerSdfUnit(float size) { return this.distanceRange * size / this.atlasSize; }

    public Glyph glyph(int codePoint) {
        Glyph glyph = this.glyphs.get(codePoint);
        return glyph != null ? glyph : this.fallbackGlyph;
    }

    public float kerning(int leftCodePoint, int rightCodePoint) {
        return this.kerning.get(packKerningKey(leftCodePoint, rightCodePoint));
    }

    public float measureWidth(String text, float size) {
        return measureWidth(text, size, 0.0F);
    }

    public float measureWidth(String text, float size, float letterSpacing) {
        float pen = 0.0F;
        int prev = -1;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            int len = Character.charCount(cp);
            Glyph g = this.glyphs.get(cp);
            if (g == null) g = this.fallbackGlyph;
            if (g != null) {
                if (prev != -1) pen += kerning(prev, cp);
                pen += g.advanceEm();
                prev = cp;
            }
            i += len;
        }
        return pen * size;
    }

    public Identifier textureId() { return this.textureId; }

    public TextureSetup textureSetup() {
        if (this.textureSetup == null) {
            this.textureSetup = createTextureSetup();
        }
        return this.textureSetup;
    }

    private TextureSetup createTextureSetup() {
        NativeImage image = readAtlasImage(this.textureId);
        DynamicTexture texture = new DynamicTexture(() -> this.textureId.toString(), image);
        Minecraft.getInstance().getTextureManager().register(this.textureId, texture);
        return TextureSetup.singleTexture(
                texture.getTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR, false)
        );
    }

    private static MsdfFont readFont(Identifier metadataId) {
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(metadataId)) {
            FontFile file = GSON.fromJson(reader, FontFile.class);
            if (file == null || file.atlas == null || file.metrics == null || file.glyphs == null) {
                throw new IllegalStateException("Invalid MSDF font: " + metadataId);
            }
            return new MsdfFont(metadataId, file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read MSDF font: " + metadataId, e);
        }
    }

    private static NativeImage readAtlasImage(Identifier textureId) {
        try (InputStream is = Minecraft.getInstance().getResourceManager().open(textureId)) {
            return NativeImage.read(is);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read MSDF atlas: " + textureId, e);
        }
    }

    private static long packKerningKey(int left, int right) {
        return ((long) left << 32) | (right & 0xFFFFFFFFL);
    }

    public static final class Glyph {
        private final float advanceEm;
        private final Bounds planeBounds;
        private final float u0, v0, u1, v1;

        private Glyph(float advanceEm, Bounds planeBounds, float u0, float v0, float u1, float v1) {
            this.advanceEm = advanceEm;
            this.planeBounds = planeBounds;
            this.u0 = u0; this.v0 = v0; this.u1 = u1; this.v1 = v1;
        }

        private static Glyph bake(RawGlyph raw, int atlasWidth, int atlasHeight) {
            Bounds plane = raw.planeBounds != null && raw.atlasBounds != null
                    ? new Bounds((float) raw.planeBounds.left, (float) raw.planeBounds.bottom,
                                 (float) raw.planeBounds.right, (float) raw.planeBounds.top)
                    : null;
            float u0 = 0, v0 = 0, u1 = 0, v1 = 0;
            if (plane != null) {
                u0 = (float) (raw.atlasBounds.left / atlasWidth);
                v0 = (float) (1.0D - raw.atlasBounds.top / atlasHeight);
                u1 = (float) (raw.atlasBounds.right / atlasWidth);
                v1 = (float) (1.0D - raw.atlasBounds.bottom / atlasHeight);
            }
            return new Glyph((float) raw.advance, plane, u0, v0, u1, v1);
        }

        public float advanceEm() { return this.advanceEm; }
        public Bounds planeBounds() { return this.planeBounds; }
        public float u0() { return this.u0; }
        public float v0() { return this.v0; }
        public float u1() { return this.u1; }
        public float v1() { return this.v1; }
    }

    public record Bounds(float left, float bottom, float right, float top) {}

    private static final class FontFile {
        Atlas atlas;
        Metrics metrics;
        List<RawGlyph> glyphs;
        List<KerningPair> kerning;
    }
    private static final class Atlas {
        @SerializedName("distanceRange")
        double distanceRange;
        int width, height, size;
    }
    private static final class Metrics {
        double lineHeight, ascender, descender;
    }
    private static final class RawGlyph {
        int unicode;
        double advance;
        RawBounds planeBounds, atlasBounds;
    }
    private static final class RawBounds {
        double left, bottom, right, top;
    }
    private static final class KerningPair {
        @SerializedName("unicode1")
        int unicode1;
        @SerializedName("unicode2")
        int unicode2;
        double advance;
    }
}



