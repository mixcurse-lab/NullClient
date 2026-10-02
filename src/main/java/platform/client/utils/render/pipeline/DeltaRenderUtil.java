package platform.client.utils.render.pipeline;

import platform.client.utils.render.DeltaBlurProcessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fc;
import platform.inject.accessors.GuiGraphicsExtractorAccessor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class DeltaRenderUtil {
    private static final List<GuiElementRenderState> QUEUED = new ArrayList<>(128);
    private static int pendingCount;
    private static int lastFlushedCount;
    private static float shadowStrength = 1.0f;

    public static void setShadowStrength(float value) {
        shadowStrength = Math.max(0.0f, Math.min(1.0f, value));
    }

    public static float getShadowStrength() {
        return shadowStrength;
    }

    private static final Field SCISSOR_STACK_FIELD;
    private static final Field SCISSOR_DEQUE_FIELD;

    static {
        Field scissorStack = null;
        Field deque = null;
        try {
            scissorStack = GuiGraphicsExtractor.class.getDeclaredField("scissorStack");
            scissorStack.setAccessible(true);
            Class<?> stackType = scissorStack.getType();
            deque = stackType.getDeclaredField("stack");
            deque.setAccessible(true);
        } catch (Exception ignored) {
        }
        SCISSOR_STACK_FIELD = scissorStack;
        SCISSOR_DEQUE_FIELD = deque;
    }

    private static ScreenRectangle currentScissor(GuiGraphicsExtractor extractor) {
        try {
            if (SCISSOR_STACK_FIELD == null || SCISSOR_DEQUE_FIELD == null) {
                return null;
            }
            Object stack = SCISSOR_STACK_FIELD.get(extractor);
            if (stack == null) {
                return null;
            }
            Deque<?> deque = (Deque<?>) SCISSOR_DEQUE_FIELD.get(stack);
            if (deque == null || deque.isEmpty()) {
                return null;
            }
            ScreenRectangle result = null;
            for (Object entry : deque) {
                ScreenRectangle rect = (ScreenRectangle) entry;
                if (rect == null || rect.width() <= 0 || rect.height() <= 0) {
                    continue;
                }
                result = (result == null) ? rect : result.intersection(rect);
                if (result == null || result.width() <= 0 || result.height() <= 0) {
                    return null;
                }
            }
            return result;
        } catch (Exception ignored) {
            return null;
        }
    }

    private DeltaRenderUtil() {
    }

    public static void beginFrame() {
        QUEUED.clear();
        pendingCount = 0;
    }

    public static int flush(GuiGraphicsExtractor extractor) {
        if (extractor == null) {
            QUEUED.clear();
            lastFlushedCount = 0;
            pendingCount = 0;
            return 0;
        }

        GuiGraphicsExtractorAccessor accessor = (GuiGraphicsExtractorAccessor) extractor;
        for (GuiElementRenderState renderState : QUEUED) {
            accessor.getGuiRenderState().addGuiElement(renderState);
        }

        int flushed = QUEUED.size();
        QUEUED.clear();
        pendingCount = 0;
        lastFlushedCount = flushed;
        return flushed;
    }

    public static int pendingCount() {
        return pendingCount;
    }

    public static int lastFlushedCount() {
        return lastFlushedCount;
    }

    public static void queueRect(
            GuiGraphicsExtractor extractor,
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
        Matrix3x2fc pose = extractor.pose();
        DeltaRectRenderState state = new DeltaRectRenderState(
                pose, x0, y0, x1, y1, color, radius, borderThickness, borderColor, scissor != null ? scissor : currentScissor(extractor)
        );
        QUEUED.add(state);
        pendingCount = QUEUED.size();
    }

    public static void queueRect(
            GuiGraphicsExtractor extractor,
            float x0,
            float y0,
            float x1,
            float y1,
            int topLeftColor,
            int topRightColor,
            int bottomRightColor,
            int bottomLeftColor,
            float topLeftRadius,
            float topRightRadius,
            float bottomRightRadius,
            float bottomLeftRadius,
            float borderThickness,
            int borderColor,
            ScreenRectangle scissor
    ) {
        Matrix3x2fc pose = extractor.pose();
        DeltaRectRenderState state = new DeltaRectRenderState(
                pose, x0, y0, x1, y1,
                topLeftColor, bottomLeftColor, bottomRightColor, topRightColor,
                topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius,
                borderThickness, borderColor, scissor != null ? scissor : currentScissor(extractor)
        );
        QUEUED.add(state);
        pendingCount = QUEUED.size();
    }

    public static void queueGradientRect(
            GuiGraphicsExtractor extractor,
            float x0,
            float y0,
            float x1,
            float y1,
            int topLeftColor,
            int topRightColor,
            int bottomRightColor,
            int bottomLeftColor,
            float topLeftRadius,
            float topRightRadius,
            float bottomRightRadius,
            float bottomLeftRadius,
            ScreenRectangle scissor
    ) {
        Matrix3x2fc pose = extractor.pose();
        DeltaRectRenderState state = DeltaRectRenderState.gradient(
                pose, x0, y0, x1, y1,
                topLeftColor, bottomLeftColor, bottomRightColor, topRightColor,
                topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius,
                scissor != null ? scissor : currentScissor(extractor)
        );
        QUEUED.add(state);
        pendingCount = QUEUED.size();
    }

    public static void queueBlurredShadow(
            GuiGraphicsExtractor extractor,
            float x0,
            float y0,
            float x1,
            float y1,
            float topLeftRadius,
            float topRightRadius,
            float bottomRightRadius,
            float bottomLeftRadius,
            float alpha,
            ScreenRectangle scissor
    ) {
        Matrix3x2fc pose = extractor.pose();
        DeltaBlurProcessor.getInstance().acquireView();
        DeltaFrostedRenderState state = new DeltaFrostedRenderState(
                pose, x0, y0, x1, y1, 0xFFFFFFFF,
                topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius,
                0.0F, 0.8F, alpha * DeltaBlurProcessor.getStrength(), true, scissor != null ? scissor : currentScissor(extractor)
        );
        QUEUED.add(state);
        pendingCount = QUEUED.size();
    }

    public static void queueBlurredRect(
            GuiGraphicsExtractor extractor,
            float x0,
            float y0,
            float x1,
            float y1,
            int color,
            float topLeftRadius,
            float topRightRadius,
            float bottomRightRadius,
            float bottomLeftRadius,
            ScreenRectangle scissor
    ) {
        Matrix3x2fc pose = extractor.pose();
        DeltaBlurProcessor.getInstance().acquireView();
        float alphaMul = ((color >>> 24) & 0xFF) / 255.0F;
        float mix = DeltaBlurProcessor.getStrength() <= 0.01F ? 1.0F : alphaMul;
        DeltaFrostedRenderState state = new DeltaFrostedRenderState(
                pose, x0, y0, x1, y1, color,
                topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius,
                mix, 0.8F, alphaMul, false, scissor != null ? scissor : currentScissor(extractor)
        );
        QUEUED.add(state);
        pendingCount = QUEUED.size();
    }

    public static void queueShadow(
            GuiGraphicsExtractor extractor,
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
        Matrix3x2fc pose = extractor.pose();
        int scaledShadowColor = ((int) (((shadowColor >>> 24) & 0xFF) * getShadowStrength()) << 24) | (shadowColor & 0x00FFFFFF);
        DeltaRectRenderState state = DeltaRectRenderState.shadow(
                pose, x0, y0, x1, y1,
                topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius,
                shadowBlur, scaledShadowColor, scissor != null ? scissor : currentScissor(extractor)
        );
        QUEUED.add(state);
        pendingCount = QUEUED.size();
    }

    public static void queueTexture(
            GuiGraphicsExtractor extractor,
            Identifier textureId,
            float x,
            float y,
            float width,
            float height,
            int color,
            ScreenRectangle scissor
    ) {
        Matrix3x2fc pose = extractor.pose();
        DeltaTextureRenderState state = new DeltaTextureRenderState(
                pose, textureId, x, y, width, height, color, scissor != null ? scissor : currentScissor(extractor)
        );
        QUEUED.add(state);
        pendingCount = QUEUED.size();
    }

    public static void queueTexture(
            GuiGraphicsExtractor extractor,
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
        Matrix3x2fc pose = extractor.pose();
        DeltaTextureRenderState state = new DeltaTextureRenderState(
                pose, textureId, x, y, width, height, u0, v0, u1, v1, color, cornerRadius, scissor != null ? scissor : currentScissor(extractor)
        );
        QUEUED.add(state);
        pendingCount = QUEUED.size();
    }

    public static void queueText(
            GuiGraphicsExtractor extractor,
            float x,
            float y,
            float size,
            int color,
            int outlineColor,
            float outlineThickness,
            float fontWeight,
            float distanceRange,
            Identifier fontTexture,
            List<DeltaTextRenderState.GlyphData> glyphs,
            float totalWidth,
            float totalHeight,
            ScreenRectangle scissor
    ) {
        Matrix3x2fc pose = extractor.pose();
        DeltaTextRenderState state = new DeltaTextRenderState(
                pose, x, y, size, color, outlineColor, outlineThickness, fontWeight,
                distanceRange, fontTexture, glyphs, totalWidth, totalHeight, scissor != null ? scissor : currentScissor(extractor)
        );
        QUEUED.add(state);
        pendingCount = QUEUED.size();
    }
}


