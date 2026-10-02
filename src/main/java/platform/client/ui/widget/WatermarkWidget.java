package platform.client.ui.widget;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.InterfaceC0020Opcode;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.client.utils.player.ServerUtil;
import platform.api.system.configs.ThemeInfo;
import platform.api.module.Interface;
import platform.api.event.events.render.DrawEvent;
import platform.api.module.setting.BooleanSetting;
import platform.client.ui.element.DragInfo;
import platform.client.utils.render.AnimationUtil;
import platform.inject.accessors.BossHealthOverlayAccessor;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class WatermarkWidget extends Widget implements Interface {
    private float f;
    private final AnimationUtil bossOffset = new AnimationUtil();
    private final BooleanSetting g;
    private final BooleanSetting h;
    private final BooleanSetting i;
    private final BooleanSetting j;
    private final BooleanSetting k;
    private final BooleanSetting l;
    private final BooleanSetting m;
    private final BooleanSetting n;
    private final BooleanSetting o;

    public WatermarkWidget() {
        super(new DragInfo("Инфо-панель", 0.0f, 0.0f, 0.0f, 0.0f));
        this.g = new BooleanSetting("Боковое отображение", false);
        this.h = new BooleanSetting("Разделять элементы", false);
        this.i = new BooleanSetting("Частота кадров", true);
        this.j = new BooleanSetting("Задержка игрока", true);
        this.k = new BooleanSetting("Текущее время", true);
        this.l = new BooleanSetting("Логин в клиенте", true);
        this.m = new BooleanSetting("Координаты", true);
        this.n = new BooleanSetting("Задержка сервера", true);
        this.o = new BooleanSetting("Скорость игрока", true);
        j().a(this);
        j().a(2);
        this.bossOffset.b(5.0f);
        addSettings(this.g, this.h, this.i, this.j, this.k, this.l, this.m, this.n, this.o);
    }

    @Override
    public void a(DrawEvent event) {
        GuiGraphicsExtractor context = event.i();
        if (context == null) { super.a(event); return; }
        d().a(true);
        d().a(0.0f, 1.0f, 0.3f, EasingList.g, event.g());
        this.f = fpsSmooth;
        float iconSize = this.e - 0.5f;
        float logoSize = this.e + 1.0f;
        float sectionGap = !this.h.c().booleanValue() ? 5.0f : 2.0f;
        String[][] topSections = k();
        String[][] bottomSections = l();
        float topWidth = measureWidth(topSections, true, iconSize, logoSize, 5.0f, sectionGap, 3.0f, 4.0f);
        float bottomWidth = measureWidth(bottomSections, false, iconSize, logoSize, 5.0f, sectionGap, 3.0f, 4.0f);
        float x;
        float y;
        x = this.g.c().booleanValue() ? 5.0f : (aM_.getWindow().getGuiScaledWidth() - topWidth) / 2.0f;
        y = this.g.c().booleanValue() ? 5.0f : this.bossOffset.a(bossBarTargetY(), 0.35f);
        j().a(x);
        j().b(y);
        float bottomX = this.g.c().booleanValue() ? x : x + ((topWidth - bottomWidth) / 2.0f);
        j().c(topWidth);
        j().d(bottomSections.length > 0 ? this.d + 3.0f + this.d : this.d);
        int primaryColor = ColorUtil.a(Delta.h().d().o().a(ThemeInfo.PRIMARY).a(), 1.0f);
        drawRow(context, x, y, topWidth, topSections, true, primaryColor, iconSize, logoSize, 5.0f, sectionGap, 3.0f, 4.0f, -0.5f);
        if (bottomSections.length > 0) {
            drawRow(context, bottomX, y + this.d + 3.0f, bottomWidth, bottomSections, false, primaryColor, iconSize, logoSize, 5.0f, sectionGap, 3.0f, 4.0f, -0.5f);
        }
        super.a(event);
    }

    private void drawRow(GuiGraphicsExtractor context, float x, float y, float width, String[][] sections, boolean logo, int primaryColor, float iconSize, float logoSize, float startPadding, float sectionGap, float iconTextGap, float logoGap, float textYOffset) {
        if (this.h.c().booleanValue()) {
            drawSplitRow(context, x, y, sections, logo, primaryColor, iconSize, logoSize, startPadding, sectionGap, iconTextGap, textYOffset);
            return;
        }
        drawBackground(context, x, y, width, this.d, true, 1.0f);
        float cursor = x + startPadding;
        float textY = y + ((this.d - Fonts.e.a(this.e)) / 2.0f) + textYOffset;
        if (logo) {
            Delta.h().d().i().a(context, Identifier.fromNamespaceAndPath("delta", "icon.png"), cursor, y + ((this.d - logoSize) / 2.0f), logoSize, logoSize, 0.0f, primaryColor, 128, 128);
            float cursor2 = cursor + Fonts.a.a("a", logoSize) + logoGap;
            drawSeparator(context, cursor2, y, this.d, 1.0f);
            cursor = cursor2 + 1.0f + sectionGap;
        }
        for (int idx = 0; idx < sections.length; idx++) {
            if (idx > 0) {
                drawSeparator(context, cursor, y, this.d, 1.0f);
                cursor += 1.0f + sectionGap;
            }
            Fonts.a.a(context, sections[idx][0], cursor, y + ((this.d - Fonts.a.a(iconSize)) / 2.0f), iconSize, primaryColor);
            float cursor3 = cursor + Fonts.a.a(sections[idx][0], iconSize) + iconTextGap;
            Fonts.e.a(context, sections[idx][1], cursor3, textY, this.e, -1);
            cursor = cursor3 + Fonts.e.a(sections[idx][1], this.e) + sectionGap;
        }
    }

    private void drawSplitRow(GuiGraphicsExtractor context, float x, float y, String[][] sections, boolean logo, int primaryColor, float iconSize, float logoSize, float startPadding, float sectionGap, float iconTextGap, float textYOffset) {
        float cursor = x;
        float textY = y + ((this.d - Fonts.e.a(this.e)) / 2.0f) + textYOffset;
        if (logo) {
            float logoWidth = (startPadding * 2.0f) + Fonts.a.a("a", logoSize);
            drawBackground(context, cursor, y, logoWidth, this.d, true, 1.0f);
            Delta.h().d().i().a(context, Identifier.fromNamespaceAndPath("delta", "icon.png"), cursor + startPadding, y + ((this.d - logoSize) / 2.0f), logoSize, logoSize, 0.0f, primaryColor, 128, 128);
            cursor += logoWidth + sectionGap;
        }
        for (String[] section : sections) {
            float sectionWidth = (startPadding * 2.0f) + Fonts.a.a(section[0], iconSize) + iconTextGap + Fonts.e.a(section[1], this.e);
            drawBackground(context, cursor, y, sectionWidth, this.d, true, 1.0f);
            float inner = cursor + startPadding;
            Fonts.a.a(context, section[0], inner, y + ((this.d - Fonts.a.a(iconSize)) / 2.0f), iconSize, primaryColor);
            Fonts.e.a(context, section[1], inner + Fonts.a.a(section[0], iconSize) + iconTextGap, textY, this.e, -1);
            cursor += sectionWidth + sectionGap;
        }
    }

    private float measureWidth(String[][] sections, boolean logo, float iconSize, float logoSize, float startPadding, float sectionGap, float iconTextGap, float logoGap) {
        if (this.h.c().booleanValue()) {
            float width = 0.0f;
            if (logo) {
                width = (startPadding * 2.0f) + Fonts.a.a("a", logoSize) + sectionGap;
            }
            for (String[] section : sections) {
                width += (startPadding * 2.0f) + Fonts.a.a(section[0], iconSize) + iconTextGap + Fonts.e.a(section[1], this.e) + sectionGap;
            }
            return Math.max(0.0f, width - sectionGap);
        }
        float width2 = startPadding;
        if (logo) {
            width2 = width2 + Fonts.a.a("a", logoSize) + logoGap + 1.0f + sectionGap;
        }
        for (int idx = 0; idx < sections.length; idx++) {
            if (idx > 0) {
                width2 += 1.0f + sectionGap;
            }
            width2 = width2 + Fonts.a.a(sections[idx][0], iconSize) + iconTextGap + Fonts.e.a(sections[idx][1], this.e) + sectionGap;
        }
        return width2;
    }

    private static long lastNano = System.nanoTime();
    private static int frameCount = 0;
    private static float fpsSmooth = 60.0f;

    private int bossBarCount() {
        if (aM_.player == null || aM_.gui.hud == null || aM_.gui.hud.getBossOverlay() == null) {
            return 0;
        }
        return ((BossHealthOverlayAccessor) aM_.gui.hud.getBossOverlay()).getBossBars().size();
    }

    private float bossBarTargetY() {
        int bars = bossBarCount();
        return bars > 0 ? (((float) bars) * 19.0f) + 3.0f : 5.0f;
    }

    private void updateFps() {
        frameCount++;
        long now = System.nanoTime();
        if (now - lastNano >= 1000000000L) {
            fpsSmooth = frameCount;
            frameCount = 0;
            lastNano = now;
        }
    }

    private String[][] k() {
        updateFps();
        List<String[]> sections = new ArrayList<>();
        if (this.l.c().booleanValue()) {
            String playerName = aM_.player != null ? aM_.player.getName().getString() : aM_.getUser().getName();
            sections.add(new String[]{"L", playerName});
        }
        if (this.i.c().booleanValue()) {
            sections.add(new String[]{"q", ((int) this.f) + " FPS"});
        }
        if (this.j.c().booleanValue()) {
            sections.add(new String[]{"P", ServerUtil.d() + " ms"});
        }
        if (this.k.c().booleanValue()) {
            sections.add(new String[]{"T", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))});
        }
        return (String[][]) sections.toArray(new String[0][]);
    }

    private String[][] l() {
        List<String[]> sections = new ArrayList<>();
        if (this.m.c().booleanValue() && aM_.player != null) {
            sections.add(new String[]{"b", "x " + ((int) aM_.player.getX()) + " y " + ((int) aM_.player.getY()) + " z " + ((int) aM_.player.getZ())});
        }
        if (this.n.c().booleanValue()) {
            sections.add(new String[]{"g", "20.0 TPS"});
        }
        if (this.o.c().booleanValue()) {
            sections.add(new String[]{"e", String.format("%.2f BPS", ServerUtil.c())});
        }
        return (String[][]) sections.toArray(new String[0][]);
    }
}
