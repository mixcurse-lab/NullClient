package platform.client.utils.render;

import platform.client.utils.render.pipeline.DeltaRenderUtil;
import platform.client.utils.render.pipeline.DeltaTextRenderState;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class Font {
    private final String name;
    private final MsdfFont msdfFont;
    private boolean logged;

    @Generated
    public String b() { return this.name; }

    public Font(String name, MsdfFont msdfFont) {
        this.name = name;
        this.msdfFont = msdfFont;
    }

    public static FontBuilder a() { return new FontBuilder(); }

    public MsdfFont msdfFont() { return this.msdfFont; }

    public float a(float size) { return size; }

    private static String normalize(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); ) {
            char c = raw.charAt(i);
            if (c == 9889) {
                sb.append((char) 349);
                i++;
            } else if (c == 9733) {
                sb.append((char) 350);
                i++;
            } else if (c == 3618) {
                i++;
            } else if (c == 167 && i + 1 < raw.length() && "0123456789abcdefklor".indexOf(raw.charAt(i + 1)) >= 0) {
                i += 2;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    public float a(String text, float size) {
        if (text == null || text.isEmpty()) return 0.0f;
        return this.msdfFont.measureWidth(normalize(text), size);
    }

    public float b(String text, float size) {
        if (text == null || text.isEmpty()) return 0.0f;
        MsdfFont.Glyph g = this.msdfFont.glyph(text.charAt(0));
        return g.planeBounds() != null ? (g.planeBounds().right() - g.planeBounds().left()) * size : 0.0f;
    }

    public float a(String text, float size, float centerY) {
        if (text == null || text.isEmpty()) {
            return centerY - (a(size) / 2.0f);
        }
        MsdfFont.Glyph glyph = this.msdfFont.glyph(text.charAt(0));
        MsdfFont.Bounds plane = glyph != null ? glyph.planeBounds() : null;
        if (plane == null) {
            return centerY - (a(size) / 2.0f);
        }
        float height = (plane.top() - plane.bottom()) * size;
        float inkCenter = (this.msdfFont.ascender(size) - (plane.top() * size)) + (height / 2.0f);
        return centerY - inkCenter;
    }

    private void renderText(GuiGraphicsExtractor context, String text, float x, float y, float size, int color, float letterSpacing) {
        if (text == null || text.isEmpty() || context == null) return;
        text = normalize(text);
        if (text.isEmpty()) return;
        if (!logged) {
            logged = true;
        }
        Identifier fontTex = this.msdfFont.textureId();
        float baseline = y + this.msdfFont.ascender(size);
        List<DeltaTextRenderState.GlyphData> gd = new ArrayList<>();
        float pen = x;
        int prev = -1;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            int len = Character.charCount(cp);
            MsdfFont.Glyph glyph = this.msdfFont.glyph(cp);
            if (glyph.planeBounds() != null) {
                MsdfFont.Bounds plane = glyph.planeBounds();
                float gx0 = pen + plane.left() * size;
                float gy0 = baseline - plane.top() * size;
                float gx1 = pen + plane.right() * size;
                float gy1 = baseline - plane.bottom() * size;
                gd.add(new DeltaTextRenderState.GlyphData(
                    gx0, gy0, gx1, gy1,
                    glyph.u0(), glyph.v0(), glyph.u1(), glyph.v1(),
                    this.msdfFont.localPxPerSdfUnit(size)
                ));
            }
            if (prev != -1) pen += this.msdfFont.kerning(prev, cp) * size;
            pen += glyph.advanceEm() * size + letterSpacing;
            prev = cp;
            i += len;
        }
        if (!gd.isEmpty()) {
            float totalW = pen - x;
            float totalH = this.msdfFont.lineHeight(size);
            DeltaRenderUtil.queueText(context, x, y, size, color, -1, 0.0f, 0.0f, this.msdfFont.distanceRange(), fontTex, gd, totalW, totalH, null);
        }
    }

    private static final class RenderState {
        float pen;
        int currentColor;
        float batchStartX;
        List<DeltaTextRenderState.GlyphData> batch = new ArrayList<>();
    }

    private void flushBatch(RenderState state, GuiGraphicsExtractor context, Identifier fontTex, float y, float size) {
        if (!state.batch.isEmpty()) {
            float totalW = state.pen - state.batchStartX;
            float totalH = this.msdfFont.lineHeight(size);
            DeltaRenderUtil.queueText(context, state.batchStartX, y, size, state.currentColor, -1, 0.0f, 0.0f, this.msdfFont.distanceRange(), fontTex, state.batch, totalW, totalH, null);
            state.batch = new ArrayList<>();
            state.batchStartX = state.pen;
        }
    }

    private void renderComponent(GuiGraphicsExtractor context, Component text, float x, float y, float size, int defaultColor, float alpha) {
        if (text == null || context == null) return;
        Identifier fontTex = this.msdfFont.textureId();
        RenderState state = new RenderState();
        state.pen = x;
        state.currentColor = defaultColor;
        state.batchStartX = x;
        boolean[] started = {false};

        text.visit((style, content) -> {
            String raw = content;
            if (raw == null || raw.isEmpty()) return java.util.Optional.empty();
            if (!started[0]) {
                raw = raw.replaceFirst("^\\s+", "");
                if (raw.isEmpty()) return java.util.Optional.empty();
                started[0] = true;
            }
            String str = normalize(raw);
            if (str.isEmpty()) return java.util.Optional.empty();
            int color = style.getColor() != null ? style.getColor().getValue() | 0xFF000000 : defaultColor;
            if (alpha < 1.0f) {
                color = ColorUtil.a(color, alpha);
            }
            if (color != state.currentColor) {
                flushBatch(state, context, fontTex, y, size);
                state.currentColor = color;
            }

            int prev = -1;
            for (int i = 0; i < str.length(); ) {
                int cp = str.codePointAt(i);
                int len = Character.charCount(cp);
                MsdfFont.Glyph glyph = this.msdfFont.glyph(cp);
                if (glyph.planeBounds() != null) {
                    MsdfFont.Bounds plane = glyph.planeBounds();
                    float baseline = y + this.msdfFont.ascender(size);
                    float gx0 = state.pen + plane.left() * size;
                    float gy0 = baseline - plane.top() * size;
                    float gx1 = state.pen + plane.right() * size;
                    float gy1 = baseline - plane.bottom() * size;
                    state.batch.add(new DeltaTextRenderState.GlyphData(
                        gx0, gy0, gx1, gy1,
                        glyph.u0(), glyph.v0(), glyph.u1(), glyph.v1(),
                        this.msdfFont.localPxPerSdfUnit(size)
                    ));
                }
                if (prev != -1) state.pen += this.msdfFont.kerning(prev, cp) * size;
                state.pen += glyph.advanceEm() * size;
                prev = cp;
                i += len;
            }
            return java.util.Optional.empty();
        }, Style.EMPTY);

        flushBatch(state, context, fontTex, y, size);
    }

    public void a(GuiGraphicsExtractor context, String text, float x, float y, float size, int color) {
        renderText(context, text, x, y, size, color, 0.0f);
    }

    public void a(GuiGraphicsExtractor context, String text, float x, float y, float size, int color, float thickness) {
        renderText(context, text, x, y, size, color, 0.0f);
    }

    public void a(GuiGraphicsExtractor context, Component text, float x, float y, float size) {
        renderComponent(context, text, x, y, size, -1, 1.0f);
    }

    public void a(GuiGraphicsExtractor context, Component text, float x, float y, float size, double alpha) {
        renderComponent(context, text, x, y, size, -1, (float) alpha);
    }

    public void b(GuiGraphicsExtractor context, String text, float x, float y, float size, int color) {
        float textWidth = a(text, size);
        renderText(context, text, x - (textWidth / 2.0f), y, size, color, 0.0f);
    }

    public void c(GuiGraphicsExtractor context, String text, float x, float y, float size, int color, float maxWidth) {
        if (text == null || text.isEmpty() || context == null || maxWidth <= 0.0f) return;
        float textWidth = a(text, size);
        if (textWidth <= maxWidth) {
            renderText(context, text, x, y, size, color, 0.0f);
            return;
        }
        context.enableScissor((int) x - 1, (int) (y - size * 0.5f), (int) (x + maxWidth + 1), (int) (y + size * 1.5f + 0.5f));
        renderText(context, text, x, y, size, color, 0.0f);
        context.disableScissor();
    }

    private static final java.util.Map<Object, float[]> SCROLL_STATES = new java.util.WeakHashMap<>();

    public void a(GuiGraphicsExtractor context, Object key, String text, float x, float y, float size, int color, float maxWidth, boolean isHovered, float speed, float delta) {
        if (text == null || text.isEmpty() || maxWidth <= 0.0f) return;
        float textWidth = a(text, size);
        if (textWidth <= maxWidth) {
            SCROLL_STATES.remove(key);
            if (context != null) renderText(context, text, x, y, size, color, 0.0f);
            return;
        }
        if (context == null) return;
        float[] s = SCROLL_STATES.computeIfAbsent(key, k -> new float[]{0.0f, 1.0f});
        float maxOffset = textWidth - maxWidth;
        float dt = Math.max(0.0f, Math.min(delta, 0.05f));
        if (isHovered) {
            s[0] += s[1] * speed * dt;
            if (s[0] >= maxOffset) {
                s[0] = maxOffset;
                s[1] = -1.0f;
            } else if (s[0] <= 0.0f) {
                s[0] = 0.0f;
                s[1] = 1.0f;
            }
        } else {
            s[0] += (0.0f - s[0]) * Math.min(1.0f, dt * 12.0f);
            if (s[0] < 0.3f) {
                s[0] = 0.0f;
                s[1] = 1.0f;
            }
        }
        context.enableScissor((int) x - 1, (int) (y - size * 0.5f), (int) (x + maxWidth + 1), (int) (y + size * 1.5f + 0.5f));
        renderText(context, text, x - s[0], y, size, color, 0.0f);
        context.disableScissor();
    }

    public float a(GuiGraphicsExtractor context, String text, float x, float y, float size, int color, float speed, float offset) {
        renderText(context, text, x, y, size, color, 0.0f);
        return offset;
    }
}



