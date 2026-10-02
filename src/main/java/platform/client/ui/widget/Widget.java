package platform.client.ui.widget;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.InterfaceC0020Opcode;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.configs.ThemeProcessor;
import platform.api.event.GlobalEvent;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.PacketEvent;
import platform.client.ui.element.Element_2;
import platform.client.utils.render.AnimationUtil;
import platform.client.ui.element.DragInfo;
import platform.api.module.setting.Setting;
import platform.client.utils.render.Draw2DProcessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;

public class Widget {
    private final List<Setting<?>> f = new ObjectArrayList();
    private final List<Element_2<?>> g = new ObjectArrayList();
    protected final AnimationUtil a = new AnimationUtil();
    protected final AnimationUtil b = new AnimationUtil();
    protected final AnimationUtil c = new AnimationUtil();
    private boolean h = false;
    protected float d = 12.5f;
    protected float e = 7.0f;
    private final DragInfo i;

    @Generated
    public List<Setting<?>> b() { return this.f; }

    @Generated
    public List<Element_2<?>> c() { return this.g; }

    @Generated
    public AnimationUtil d() { return this.a; }

    @Generated
    public AnimationUtil e() { return this.b; }

    @Generated
    public AnimationUtil f() { return this.c; }

    @Generated
    public void setEnabled(boolean status) { this.h = status; }

    @Generated
    public boolean g() { return this.h; }

    @Generated
    public float h() { return this.d; }

    @Generated
    public float i() { return this.e; }

    @Generated
    public DragInfo j() { return this.i; }

    public Widget(DragInfo dragInfo) {
        this.i = dragInfo;
        dragInfo.a(this);
    }

    protected final void addSettings(Setting<?>... settings) {
        for (Setting<?> setting : settings) {
            this.f.add(setting);
            this.g.add(setting.d());
        }
    }

    public void a(DrawEvent event) {
        e().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        this.c.a(this.h && (aM_.gui.screen() instanceof ChatScreen));
        this.c.a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        if (this.c.c() > 0.0f) {
            b(event);
        }
    }

    public void a(GlobalEvent event) {
        e().a(this.i == Delta.h().d().s().g());
    }

    public void a(PacketEvent event) {
    }

    protected void b(DrawEvent event) {
        GuiGraphicsExtractor context = event.i();
        List<Element_2<?>> visible = this.g.stream().filter(e -> e.e().e().get().booleanValue()).toList();
        if (!visible.isEmpty()) {
            float panelWidth = ((Float) visible.stream().map(e2 -> Float.valueOf(19.5f + Fonts.e.a(e2.e().i(), 6.5f) + 25.0f)).reduce(Float.valueOf(0.0f), Math::max)).floatValue();
            float totalHeight = (12.0f * visible.size()) + (visible.size() - 1);
            float anim = this.c.c() * a();
            float baseX = (this.i.b() - totalHeight) - 2.0f >= 0.0f ? (this.i.a() + (this.i.f() / 2.0f)) - (panelWidth / 2.0f) : this.i.a() + this.i.f() + 2.0f;
            float baseY = (this.i.b() - totalHeight) - 2.0f >= 0.0f ? (this.i.b() - totalHeight) - 2.0f : this.i.b();
            float baseX2 = Math.min(Math.max(baseX, 0.0f), (aM_.getWindow().getGuiScaledWidth() - panelWidth) - 2.0f);
            float baseY2 = Math.min(Math.max(baseY, 0.0f), aM_.getWindow().getGuiScaledHeight() - totalHeight);
            drawBackground(context, baseX2, baseY2, panelWidth, totalHeight, true, anim);
            float y = baseY2;
            for (Element_2<?> element : visible) {
                element.d().set(baseX2, y, panelWidth, 12.0f);
                element.a(event, baseX2, y, panelWidth, anim);
                y += 12.0f + 1.0f;
                if (element != visible.getLast()) {
                    Delta.h().d().i().a(context, baseX2, y - 1.0f, panelWidth, 0.75f, ColorUtil.a(ColorUtil.a(InterfaceC0020Opcode.aN, InterfaceC0020Opcode.aN, InterfaceC0020Opcode.aN, 255), 0.2f * anim));
                }
            }
        }
    }

    protected void drawHeader(GuiGraphicsExtractor context, String icon, Object title, float width, float animation) {
        drawHeader(context, icon, title, width, animation, Delta.h().d().o().a(ThemeInfo.PRIMARY).a());
    }

    protected void drawHeader(GuiGraphicsExtractor context, String icon, Object title, float width, float animation, int iconColor) {
        drawHeader(context, this.i.a(), this.i.b(), icon, title, width, animation, iconColor);
    }

    protected void drawHeader(GuiGraphicsExtractor context, float x, float y, String icon, Object title, float width, float animation, int iconColor) {
        if (animation > 0.0f) {
            float iconSize = this.e + 1.0f;
            drawBackground(context, x, y, width, this.d, true, animation);
            Fonts.a.a(context, icon, x + 3.0f, y + ((this.d - Fonts.a.a(iconSize)) / 2.0f), iconSize, ColorUtil.a(iconColor, animation));
            drawSeparator(context, x + 13.5f, y, this.d, animation);
            Fonts.e.a(context, String.valueOf(title), x + 17.5f, (y + ((this.d - Fonts.e.a(this.e)) / 2.0f)) - 0.5f, this.e, ColorUtil.a(-1, animation));
        }
    }

    protected void drawBackground(GuiGraphicsExtractor context, float x, float y, float width, float height, boolean glow, float animation) {
        if (animation > 0.0f) {
            ThemeProcessor themeProcessor = Delta.h().d().o();
            float alpha = themeProcessor.a(ThemeInfo.BACKGROUND_HUD).b() * animation;
            int background = ColorUtil.a(themeProcessor.a(ThemeInfo.BACKGROUND_HUD).a(), themeProcessor.a(ThemeInfo.PRIMARY).a(), themeProcessor.a(ThemeInfo.PRIMARY).b() / 6.0f);
            if (glow) {
                Delta.h().d().i().a(context, x, y, width, height, 5.0f + (1.0f * this.b.c()), ColorUtil.a(background, alpha), animation, ColorUtil.a(background, alpha), 8.0f + (2.0f * this.b.c()));
            } else {
                Delta.h().d().i().b(context, x, y, width, height, 5.0f, ColorUtil.a(background, alpha), animation);
            }
        }
    }

    protected void drawSeparator(GuiGraphicsExtractor context, float x, float y, float height, float animation) {
        float separatorHeight = height / 2.0f;
        Delta.h().d().i().a(context, x, y + ((height - separatorHeight) / 2.0f), 0.75f, separatorHeight, ColorUtil.a(ColorUtil.a(InterfaceC0020Opcode.aN, InterfaceC0020Opcode.aN, InterfaceC0020Opcode.aN, 255), 0.5f * animation));
    }

    public float a() {
        return this.a.c() * (1.0f - (0.1f * this.b.c()));
    }
}



