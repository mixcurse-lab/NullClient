package platform.client.ui.screen;

import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.configs.ThemeProcessor;
import platform.client.Delta;
import platform.client.ui.element.Element_2;
import platform.client.utils.input.KeyUtil;
import platform.client.utils.math.MathUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.Draw2DProcessor;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector4f;

import java.util.List;

public class GUIPanel {
    private final Vector4f bounds = new Vector4f(0.0f, 0.0f, 150.0f, 295.0f);
    private final platform.client.utils.render.AnimationUtil scrollAnimation =
            new platform.client.utils.render.AnimationUtil();
    private final platform.client.utils.render.AnimationUtil openAnimation =
            new platform.client.utils.render.AnimationUtil();
    private final Category category;
    private List<Module> modules = List.of();
    private Module hovered;
    private float scrollTarget;
    private float scroll;
    private float clipTop;
    private float clipBottom;

    public GUIPanel(Category category) {
        this.category = category;
    }

    public boolean a(double mouseX, double mouseY, int button) {
        for (Module module : this.modules) {
            if (module.n()) {
                module.a(-100 + button);
                module.b(false);
                return true;
            }
        }
        for (Module module : this.modules) {
            if (!module.o()) continue;
            for (Element_2<?> element : module.d()) {
                if (element.a() && element.a(mouseX, mouseY, button)) return true;
            }
        }
        if (this.hovered == null) return false;
        if (button == 0) {
            this.hovered.a();
            return true;
        }
        if (button == 1) {
            this.hovered.c(!this.hovered.o());
            return true;
        }
        if (button == 2) {
            for (Module module : this.modules) {
                module.b(module == this.hovered && !module.n());
            }
            return true;
        }
        return false;
    }

    public boolean b(double mouseX, double mouseY, int button) {
        for (Module module : this.modules) {
            if (!module.o()) continue;
            for (Element_2<?> element : module.d()) {
                if (element.a() && element.b(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }

    public boolean a(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (Module module : this.modules) {
            if (!module.o()) continue;
            for (Element_2<?> element : module.d()) {
                if (element.a() && element.a(mouseX, mouseY, button, deltaX, deltaY)) return true;
            }
        }
        return false;
    }

    public boolean a(int keyCode, int scanCode, int modifiers) {
        for (Module module : this.modules) {
            if (module.n()) {
                module.a(keyCode == 256 ? -1 : keyCode);
                module.b(false);
                return true;
            }
        }
        for (Module module : this.modules) {
            if (!module.o()) continue;
            for (Element_2<?> element : module.d()) {
                if (element.a() && element.a(keyCode, scanCode, modifiers)) return true;
            }
        }
        return false;
    }

    public boolean a(char chr, int modifiers) {
        for (Module module : this.modules) {
            if (!module.o()) continue;
            for (Element_2<?> element : module.d()) {
                if (element.a() && element.a(chr, modifiers)) return true;
            }
        }
        return false;
    }

    public boolean a(double mouseX, double mouseY, double amount) {
        if (!MathUtil.a(mouseX, mouseY, this.bounds.x, this.clipTop,
                this.bounds.z, this.clipBottom - this.clipTop)) return false;
        for (Module module : this.modules) {
            if (!module.o()) continue;
            for (Element_2<?> element : module.d()) {
                if (element.a() && element.a(mouseX, mouseY, amount)) return true;
            }
        }
        float maxScroll = Math.max(0.0f, contentHeight() - (this.clipBottom - this.clipTop));
        this.scrollTarget = MathUtil.b(this.scrollTarget + (float) amount * 15.0f, -maxScroll, 0.0f);
        return true;
    }

    public void a(List<Module> modules) {
        this.modules = modules;
    }

    public void a(Module hovered) {
        this.hovered = hovered;
    }

    public Vector4f f() {
        return this.bounds;
    }

    public platform.client.utils.render.AnimationUtil a() {
        return this.scrollAnimation;
    }

    public platform.client.utils.render.AnimationUtil b() {
        return this.openAnimation;
    }

    public Category c() {
        return this.category;
    }

    public List<Module> d() {
        return this.modules;
    }

    public Module e() {
        return this.hovered;
    }

    public void a(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Draw2DProcessor draw = Delta.h().d().i();
        ThemeProcessor theme = Delta.h().d().o();
        float panelX = this.bounds.x;
        float panelY = this.bounds.y;
        float panelWidth = this.bounds.z;
        float panelHeight = this.bounds.w;
        float open = EasingList.s.ease(this.openAnimation.c());

        context.pose().pushMatrix();
        context.pose().translate(panelX + panelWidth / 2.0f,
                panelY + panelHeight / 2.0f + (1.0f - open) * 12.0f);
        context.pose().scale(0.92f + open * 0.08f, 0.92f + open * 0.08f);
        context.pose().translate(-(panelX + panelWidth / 2.0f),
                -(panelY + panelHeight / 2.0f));

        int background = ColorUtil.a(theme.a(ThemeInfo.BACKGROUND_GUI).a(), 225);
        int outline = ColorUtil.a(theme.a(ThemeInfo.OUTLINE_MEDIUM).a(), 90);
        draw.b(context, panelX, panelY, panelWidth, panelHeight, 8.0f,
                ColorUtil.a(background, 0.88f));
        draw.a(context, panelX, panelY, panelWidth, panelHeight, 8.0f, background);
        draw.a(context, panelX, panelY, panelWidth, panelHeight, 8.0f, 0.6f, outline);

        String title = switch (this.category) {
            case Combat -> "Combat";
            case Movement -> "Movement";
            case Render -> "Visual";
            case Player -> "Player";
            case Misc -> "Misc";
        };
        int titleColor = theme.a(ThemeInfo.TEXT).a();
        Fonts.c.a(context, title, panelX + 12.0f,
                Fonts.c.a(title, 14.0f, panelY + 17.0f), 14.0f, titleColor);
        Fonts.a.a(context, this.category.a(),
                panelX + panelWidth - 18.0f,
                Fonts.a.a(this.category.a(), 11.0f, panelY + 17.0f), 11.0f,
                theme.a(ThemeInfo.PRIMARY).a());

        this.clipTop = panelY + 31.0f;
        this.clipBottom = panelY + panelHeight - 9.0f;
        float viewportHeight = this.clipBottom - this.clipTop;
        float maxScroll = Math.max(0.0f, contentHeight() - viewportHeight);
        this.scrollTarget = MathUtil.b(this.scrollTarget, -maxScroll, 0.0f);
        this.scroll += (this.scrollTarget - this.scroll) * 0.22f;
        if (Math.abs(this.scrollTarget - this.scroll) < 0.05f) this.scroll = this.scrollTarget;

        this.hovered = null;
        context.enableScissor((int) panelX, (int) this.clipTop,
                (int) (panelX + panelWidth), (int) this.clipBottom);
        float rowY = this.clipTop + this.scroll;
        for (Module module : this.modules) {
            module.h().a(0.0f, 1.0f, 0.5f, EasingList.i, delta);
            module.h().a(module.o());
            module.i().a(0.0f, 1.0f, 0.25f, EasingList.i, delta);
            module.i().a(module == this.hovered);
            for (Element_2<?> element : module.d()) {
                element.c().a(element.a());
                element.c().a(0.0f, 1.0f, 0.4f, EasingList.i, delta);
            }
            float rowHeight = moduleHeight(module);
            float visibleTop = Math.max(rowY, this.clipTop);
            float visibleBottom = Math.min(rowY + rowHeight, this.clipBottom);
            boolean inViewport = visibleTop < visibleBottom;
            boolean isHovered = inViewport && mouseX >= panelX + 6.0f
                    && mouseX <= panelX + panelWidth - 6.0f
                    && mouseY >= rowY && mouseY <= rowY + 18.0f;
            if (isHovered) this.hovered = module;

            if (inViewport) {
                float activation = module.f().c();
                int rowBackground = ColorUtil.a(
                        activation > 0.0f ? theme.a(ThemeInfo.PRIMARY).a() : 0xFF231D23,
                        (activation > 0.0f ? 48 : 210) / 255.0f);
                draw.a(context, panelX + 6.0f, rowY, panelWidth - 12.0f,
                        Math.max(18.0f, rowHeight), 5.0f, rowBackground);
                draw.a(context, panelX + 6.0f, rowY, panelWidth - 12.0f,
                        Math.max(18.0f, rowHeight), 5.0f, 0.35f,
                        ColorUtil.a(theme.a(ThemeInfo.OUTLINE_SMALL).a(), 0.55f));
                if (isHovered) {
                    draw.a(context, panelX + 6.0f, rowY, panelWidth - 12.0f,
                            18.0f, 5.0f, ColorUtil.a(0xFFFFFFFF, 0.035f));
                }

                String name = module.j();
                float reserve = module.m() ? 25.0f : 13.0f;
                float nameMax = Math.max(20.0f, panelWidth - 35.0f - reserve);
                Fonts.c.a(context, module, name, panelX + 12.0f,
                        Fonts.c.a(name, 8.2f, rowY + 9.0f), 8.2f,
                        ColorUtil.a(theme.a(ThemeInfo.TEXT).a(), 1.0f),
                        nameMax, isHovered && !module.n(), 25.0f, delta);

                if (module.d().stream().anyMatch(Element_2::a)) {
                    Fonts.c.a(context, "...", panelX + panelWidth - 37.0f,
                            Fonts.c.a("...", 10.0f, rowY + 9.0f), 10.0f,
                            theme.a(ThemeInfo.TEXT_DISABLED).a());
                }

                float toggleX = panelX + panelWidth - 23.0f;
                float toggleY = rowY + 5.0f;
                draw.a(context, toggleX, toggleY, 13.0f, 8.0f, 3.0f,
                        ColorUtil.a(theme.a(ThemeInfo.PRIMARY).a(),
                                activation * 0.72f + 0.12f));
                draw.a(context, toggleX + 1.0f + activation * 4.0f,
                        toggleY + 1.0f, 6.0f, 6.0f, 3.0f,
                        ColorUtil.a(0xFFFFFFFF, 0.9f));

                if (module.g().c() > 0.0f) {
                    String bind = module.n() ? "?" : KeyUtil.b(module.p());
                    Fonts.c.a(context, bind, panelX + 12.0f,
                            Fonts.c.a(bind, 6.5f, rowY + 9.0f), 6.5f,
                            ColorUtil.a(theme.a(ThemeInfo.TEXT_DISABLED).a(), module.g().c()));
                }

                if (module.h().c() > 0.0f) {
                    float settingY = rowY + 17.0f - (4.0f * (1.0f - module.h().c()));
                    float offset = 0.0f;
                    for (Element_2<?> element : module.d()) {
                        float visible = element.c().c();
                        if (visible <= 0.0f) continue;
                        float height = element.d().w();
                        float currentY = settingY + offset;
                        element.d().set(panelX + 11.0f, currentY,
                                panelWidth - 22.0f, height);
                        element.a(context, mouseX, mouseY, delta,
                                module.h().c() * visible);
                        offset += (height + 3.0f) * visible;
                    }
                }
            }
            rowY += rowHeight + 2.0f;
        }
        context.disableScissor();
        context.pose().popMatrix();
    }

    private float contentHeight() {
        float height = 0.0f;
        for (Module module : this.modules) height += moduleHeight(module) + 2.0f;
        return height;
    }

    private float moduleHeight(Module module) {
        float settingsHeight = 0.0f;
        for (Element_2<?> element : module.d()) {
            settingsHeight += (element.d().w() + 3.0f) * element.c().c();
        }
        return 18.0f + settingsHeight * module.h().c();
    }
}
