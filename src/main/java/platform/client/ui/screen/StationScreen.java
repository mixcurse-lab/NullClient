package platform.client.ui.screen;

import platform.api.system.interfaces.NativeMethodLookup;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Interface;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;

import platform.api.utils.auction.AutoBuySection;
import platform.api.utils.auction.CollectorSection;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.configs.ThemeProcessor;
import platform.client.utils.render.Draw2DProcessor;
import platform.client.utils.render.DeltaBlurProcessor;
import platform.client.utils.render.pipeline.DeltaRenderUtil;
import platform.client.utils.render.ScaleUtil;

import platform.client.utils.render.AnimationUtil;
import platform.client.ui.element.Section;
import platform.api.annotation.Compile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Generated;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.CharacterEvent;
import org.joml.Vector4f;

public class StationScreen extends Screen {
    private final List<Section> a;
    private final Vector4f b;
    private final Vector4f c;
    private final Vector4f d;
    private final AnimationUtil e;
    private final AnimationUtil f;
    private int g;
    private float h;

    @Compile
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        DeltaBlurProcessor.setStrength(1.0f);
        DeltaRenderUtil.setShadowStrength(1.0f);
        DeltaRenderUtil.beginFrame();
        super.extractRenderState(context, mouseX, mouseY, delta);
        double dA = MathUtil.scale(mouseX, 2);
        double dA2 = MathUtil.scale(mouseY, 2);
        ScaleUtil.a(context, 2);
        Vector4f vector4f = this.b;
        Minecraft class_310Var = Interface.aM_;
        vector4f.x = (class_310Var.getWindow().getGuiScaledWidth() - this.b.z) * 0.5f;
        this.b.y = (class_310Var.getWindow().getGuiScaledHeight() - this.b.w) * 0.5f;
        this.f.a(0.0f, 1.0f, 0.3f, EasingList.g, delta);
        this.f.a(true);
        Vector4f vector4f2 = this.b;
        float f = vector4f2.x;
        float f2 = 0.5f * vector4f2.z;
        float f3 = vector4f2.y;
        float f4 = 0.5f * vector4f2.w;
        float fEase = 0.85f + (EasingList.s.ease(this.f.c()) * 0.15f);
        context.pose().pushMatrix();
        float f5 = f2 + f;
        float f6 = f4 + f3;
        context.pose().translate(f5, ((1.0f - EasingList.p.ease(this.f.c())) * 14.0f) + f6);
        context.pose().scale(fEase, fEase);
        context.pose().translate(-f5, -f6);
        Draw2DProcessor draw2DProcessorI = Delta.h().d().i();
        ThemeProcessor themeProcessorO = Delta.h().d().o();
        int iA = themeProcessorO.a(ThemeInfo.BACKGROUND_GUI).a();
        ThemeInfo themeInfo = ThemeInfo.PRIMARY;
        int iA2 = ColorUtil.a(ColorUtil.a(iA, themeProcessorO.a(themeInfo).a(), themeProcessorO.a(themeInfo).b() * 0.25f), 220);
        draw2DProcessorI.a(context, vector4f2.x, vector4f2.y, vector4f2.z, vector4f2.w, 8.0f, iA2, 1.0f, iA2, 16.0f);
        draw2DProcessorI.a(context, vector4f2.x, vector4f2.y, vector4f2.z, vector4f2.w, 8.0f, 0.5f, themeProcessorO.a(ThemeInfo.OUTLINE_MEDIUM).a());
        a(context, delta, iA2);
        this.d.set(vector4f2.x, this.c.y + this.c.w + 8.0f, vector4f2.z, ((vector4f2.y + vector4f2.w) - 8.0f) - ((this.c.y + this.c.w) + 12.0f));
        b().a(context, this.d, (int)dA, (int)dA2, this.e.a(), delta);
        Draw2DProcessor draw2DProcessorI2 = Delta.h().d().i();
        float f7 = this.h;
        draw2DProcessorI2.a(context, f7 - 3.0f, this.c.y + this.c.w, 6.0f, 0.5f, Delta.h().d().o().a(ThemeInfo.PRIMARY).a());
        context.pose().popMatrix();
        this.e.a(Math.min(0.0f, this.d.w - b().a(this.d)), 0.0f, 1.0f);
        ScaleUtil.a(context);
        DeltaRenderUtil.flush(context);
    }

    @Compile
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double scaledX = MathUtil.scale(mouseX, 2);
        double scaledY = MathUtil.scale(mouseY, 2);
        if (b().a(scaledX, scaledY, verticalAmount)) {
            return true;
        }
        this.e.a(((float) verticalAmount) * 15.0f);
        return true;
    }

    @Compile
    public boolean mouseClicked(MouseButtonEvent event, boolean held) {
        double mouseX = MathUtil.scale(event.x(), 2);
        double mouseY = MathUtil.scale(event.y(), 2);
        int button = event.button();
        Vector4f vector4f = this.c;
        List<Section> list = this.a;
        Vector4f vector4f2 = this.d;
        AnimationUtil animationUtil = this.e;
        float f = vector4f.x + 12.0f;
        for (int i = 0; list.size() > i; i++) {
            float fB = Fonts.a.b(list.get(i).b(), 8.5f);
            if (MathUtil.a(mouseX, mouseY, f - 6.0f, vector4f.y, fB + 12.0f, vector4f.w)) {
                this.g = i;
                animationUtil.b(0.0f);
                return true;
            }
            f += fB + 12.0f;
        }
        if (vector4f2.y > mouseY || !b().a(mouseX, mouseY, button)) {
            return super.mouseClicked(event, held);
        }
        return true;
    }

    @Compile
    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = MathUtil.scale(event.x(), 2);
        double mouseY = MathUtil.scale(event.y(), 2);
        int button = event.button();
        if (b().b(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Compile
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        double mouseX = MathUtil.scale(event.x(), 2);
        double mouseY = MathUtil.scale(event.y(), 2);
        int button = event.button();
        if (b().a(mouseX, mouseY, button, MathUtil.scale(deltaX,2), MathUtil.scale(deltaY,2))) {
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Compile
    public boolean charTyped(CharacterEvent event) {
        char character = (char) event.codepoint();
        if (b().a(character, 0)) {
            return true;
        }
        return super.charTyped(event);
    }

    @Compile
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int scanCode = event.scancode();
        int modifiers = event.modifiers();
        Section sectionB = b();
        if (sectionB == null) {
            throw new NullPointerException();
        }
        if (sectionB.a(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(event);
    }

    static {
        NativeMethodLookup.lookup(StationScreen.class, 17);
    }

    @Generated
    public AnimationUtil a() {
        return this.e;
    }

    public StationScreen(Component title) {
        this(title, 0);
    }

    public StationScreen(Component title, int selected) {
        super(title);
        this.a = new ArrayList();
        this.b = new Vector4f(0.0f, 0.0f, 425.0f, 235.0f);
        this.c = new Vector4f(0.0f, 0.0f, 0.0f, 16.0f);
        this.d = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
        this.e = new AnimationUtil();
        this.f = new AnimationUtil();
        this.a.add(new CollectorSection());
        this.a.add(new AutoBuySection());
        this.g = selected;
    }

    public void onClose() {
        super.onClose();
        this.f.c(0.0f);
    }

    @Override
    public void removed() {
        DeltaBlurProcessor.setStrength(1.0f);
        DeltaRenderUtil.setShadowStrength(1.0f);
        super.removed();
    }

    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private Section b() {
        return this.a.get(this.g);
    }

    private void a(GuiGraphicsExtractor context, float delta, int background) {
        Draw2DProcessor draw = Delta.h().d().i();
        ThemeProcessor theme = Delta.h().d().o();
        float iconsWidth = 12.0f * (this.a.size() - 1);
        Iterator<Section> it = this.a.iterator();
        while (it.hasNext()) {
            iconsWidth += Fonts.a.b(it.next().b(), 8.5f);
        }
        this.c.z = iconsWidth + 24.0f;
        this.c.x = this.b.x + ((this.b.z - this.c.z) / 2.0f);
        this.c.y = this.b.y + 8.0f;
        draw.a(context, this.c.x, this.c.y, this.c.z, this.c.w, 6.0f, 0.5f, ColorUtil.a(255, 255, 255, 4));
        draw.a(context, Identifier.fromNamespaceAndPath("delta", "icon.png"), this.b.x + 8.0f, this.c.y + 2.0f, 12.0f, 12.0f, 0.0f, ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), 0.75f), 128, 128);
        float separatorX = this.b.x + 8.0f + Fonts.a.a("a", 12.0f) + 8.0f;
        draw.a(context, separatorX, this.c.y + ((this.c.w - 8.0f) / 2.0f), 0.75f, 8.0f, ColorUtil.a(255, 255, 255, 25));
        Fonts.c.a(context, "deltaclient.xyz", separatorX + 8.0f, (this.c.y + ((this.c.w - Fonts.c.a(6.75f)) / 2.0f)) - 0.5f, 6.75f, theme.a(ThemeInfo.TEXT_DISABLED).a());
        float avatarX = ((this.b.x + this.b.z) - 12.0f) - 8.0f;
        draw.a(context, Identifier.fromNamespaceAndPath("delta", "icon.png"), avatarX, this.c.y + ((this.c.w - 12.0f) / 2.0f), 12.0f, 12.0f, 5.0f, -1, 128, 128);
        String sectionName = b().c();
        float separatorX2 = avatarX - 8.0f;
        draw.a(context, separatorX2, this.c.y + ((this.c.w - 8.0f) / 2.0f), 0.75f, 8.0f, ColorUtil.a(255, 255, 255, 25));
        Fonts.c.a(context, sectionName, (separatorX2 - 8.0f) - Fonts.c.a(sectionName, 6.75f), (this.c.y + ((this.c.w - Fonts.c.a(6.75f)) / 2.0f)) - 0.5f, 6.75f, theme.a(ThemeInfo.TEXT_DISABLED).a());
        float x = this.c.x + 12.0f;
        float y = this.c.y + ((this.c.w - Fonts.a.a(8.5f)) / 2.0f);
        float target = this.h;
        int i = 0;
        while (i < this.a.size()) {
            Section section = this.a.get(i);
            float iconWidth = Fonts.a.b(section.b(), 8.5f);
            section.a().a(i == this.g);
            section.a().a(0.0f, 1.0f, 0.3f, EasingList.i, delta);
            Fonts.a.a(context, section.b(), x, y, 8.5f, ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(), 1.0f), section.a().c()));
            if (i == this.g) {
                target = x + (iconWidth / 2.0f);
            }
            x += iconWidth + 12.0f;
            i++;
        }
        this.h = this.h == 0.0f ? target : MathUtil.c(this.h, target, 1.25f);
    }
}
