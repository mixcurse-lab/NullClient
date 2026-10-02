package platform.client.utils.render;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.api.system.configs.BaseProcessor;
import platform.api.module.Interface;
import platform.client.utils.render.pipeline.DeltaRenderUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class Draw2DProcessor extends BaseProcessor implements Interface {
    private float b = 1.0f;

    @Override
    public void setup() {
    }

    static {
        NativeMethodLookup.lookup(Draw2DProcessor.class, 28);
    }

    public void a(float scale) { this.b = scale; }
    public float a() { return this.b; }
    @Override
    public void unSetup() {}

    public void a(GuiGraphicsExtractor context, float x, float y, float width, float height, float radius, int color) {
        if (context == null) return;
        DeltaRenderUtil.queueRect(context, x, y, x + width, y + height, color, radius, 0.0f, 0x00000000, null);
    }

    public void a(GuiGraphicsExtractor context, float x, float y, float width, float height, float radius, float outlineWidth, int color) {
        if (context == null) return;
        DeltaRenderUtil.queueRect(context, x, y, x + width, y + height, 0x00000000, radius, outlineWidth, color, null);
    }

    public void a(GuiGraphicsExtractor context, Identifier texture, float x, float y, float width, float height, float radius, int color) {
        if (context == null) return;
        context.blit(RenderPipelines.GUI_TEXTURED, texture, (int)x, (int)y, 0, 0, (int)width, (int)height, (int)width, (int)height, color);
    }

    public void a(GuiGraphicsExtractor context, Identifier texture, float x, float y, float width, float height, float radius, int color, int textureWidth, int textureHeight) {
        if (context == null) return;
        context.blit(RenderPipelines.GUI_TEXTURED, texture, (int)x, (int)y, 0.0f, 0.0f, (int)width, (int)height, textureWidth, textureHeight, textureWidth, textureHeight, color);
    }

    public void a(GuiGraphicsExtractor context, float x, float y, float width, float height, float radius, int color, float u, float v, float textureWidth, float textureHeight, int textureId) {
        if (context == null) return;
        Identifier texture = Identifier.fromNamespaceAndPath("delta", "textures/item/" + textureId + ".png");
        context.blit(RenderPipelines.GUI_TEXTURED, texture, (int)x, (int)y, (int)(u * 16), (int)(v * 16), (int)width, (int)height, 16, 16, -1);
    }

    public void a(GuiGraphicsExtractor context, Identifier skin, LivingEntity target, float x, float y, float width, float height, float radius, float alpha) {
        if (skin == null || context == null) return;
        int color = ColorUtil.a(255, 255, 255, (int) (alpha * 255.0f));
        DeltaRenderUtil.queueTexture(context, skin, x, y, width, height, 0.125f, 0.125f, 0.25f, 0.25f, color, radius, null);
        DeltaRenderUtil.queueTexture(context, skin, x, y, width, height, 0.625f, 0.125f, 0.75f, 0.25f, color, radius, null);
    }

    public void a(GuiGraphicsExtractor context, float x, float y, float width, float height, float radius, int topLeftColor, int topRightColor, int bottomLeftColor, int bottomRightColor) {
        if (context == null) return;
        DeltaRenderUtil.queueGradientRect(context, x, y, x + width, y + height,
                topLeftColor, topRightColor, bottomRightColor, bottomLeftColor,
                radius, radius, radius, radius, null);
    }

    public void b(GuiGraphicsExtractor context, float x, float y, float width, float height, float radius, int color) {
        if (context == null) return;
        DeltaRenderUtil.queueBlurredRect(context, x, y, x + width, y + height,
                color, radius, radius, radius, radius, null);
    }

    public void a(GuiGraphicsExtractor context, float x, float y, float width, float height, float radius, int color, float mix) {
        if (context == null) return;
        DeltaRenderUtil.queueBlurredRect(context, x, y, x + width, y + height,
                color, radius, radius, radius, radius, null);
    }

    public void a(GuiGraphicsExtractor context, float x, float y, float width, float height, float radius, int color, float alpha, int glowColor, float glowRadius) {
        if (context == null) return;
        DeltaRenderUtil.queueBlurredShadow(context, x, y, x + width, y + height,
                radius, radius, radius, radius, alpha, null);
        DeltaRenderUtil.queueShadow(context, x, y, x + width, y + height,
                radius, radius, radius, radius, glowRadius, glowColor, null);
        DeltaRenderUtil.queueRect(context, x, y, x + width, y + height, color, radius, 0.0f, 0x00000000, null);
    }

    public void b(GuiGraphicsExtractor context, float x, float y, float width, float height, float radius, int color, float alpha) {
        if (context == null) return;
        DeltaRenderUtil.queueBlurredShadow(context, x, y, x + width, y + height,
                radius, radius, radius, radius, alpha, null);
        DeltaRenderUtil.queueRect(context, x, y, x + width, y + height, color, radius, 0.0f, 0x00000000, null);
    }

    public void a(GuiGraphicsExtractor context, float x, float y, float width, float height, int color) {
        if (context == null) return;
        DeltaRenderUtil.queueRect(context, x, y, x + width, y + height, color, 0.0f, 0.0f, 0x00000000, null);
    }
}


