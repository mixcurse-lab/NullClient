package platform.client.features.modules.render;

import static platform.api.module.Interface.aM_;

import platform.api.event.interfaces.EventTarget;
import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ColorSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.system.configs.ThemeInfo;
import platform.client.Delta;
import platform.client.features.modules.combat.Aura;
import platform.client.utils.math.MathUtil;
import platform.client.utils.math.ProjectUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.Fonts;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

/**
 * TargetESP — отображает визуальный индикатор текущей цели ауры.
 *
 * Режимы:
 *   Ромб    — вращающийся ромб вокруг цели (3D, BatchProcessor)
 *   Кружок  — плавный круг на уровне ног цели (3D)
 *   Теги    — 2D-плашка над головой с именем, HP и расстоянием
 *
 * Таргет берётся из модуля Aura (аналог AimBot/AttackAura из оригинала).
 * Анимации — через MathUtil.c() (экспоненциальный smoothstep).
 * Рендер  — Draw2DProcessor + Draw3DProcessor + PoseStack.
 */
@ModuleRegister(a = "Target ESP", b = "Показывает индикатор текущей цели ауры", c = Category.Render)
public class TargetESP extends Module {

    // ─── Настройки ────────────────────────────────────────────────────────────

    private final ModeSetting modeS     = new ModeSetting("Режим", "Теги", "Теги", "Кружок", "Ромб");
    private final BooleanSetting redHit = new BooleanSetting("Краснеть при ударе", true);
    private final ColorSetting colorS   = new ColorSetting("Цвет", ColorUtil.a(255, 255, 255, 255));

    // ─── Анимация появления/исчезновения цели ────────────────────────────────

    /** 0..1 — плавный прогресс появления/исчезновения. */
    private float showAnim   = 0.0f;
    /** Кэш цели (сохраняется пока showAnim > 0, чтобы анимация ухода доиграла). */
    private LivingEntity cachedTarget = null;

    // ─── Анимация Ромб ────────────────────────────────────────────────────────

    private float diamondAngle = 0.0f;

    // ─── Анимация Кружок ─────────────────────────────────────────────────────

    private float circleAngle = 0.0f;

    public TargetESP() {
        a(modeS, redHit, colorS);
    }

    // ─── Тик — обновляем анимации ─────────────────────────────────────────────

    @EventTarget
    public void a(TickEvent event) {
        LivingEntity target = getTarget();
        boolean hasTarget = target != null;

        // Плавное появление/исчезновение
        float targetAnim = hasTarget ? 1.0f : 0.0f;
        showAnim = MathUtil.c(showAnim, targetAnim, 8.0f);

        if (hasTarget) {
            cachedTarget = target;
        } else if (showAnim < 0.01f) {
            cachedTarget = null;
        }

        // Вращение ромба
        diamondAngle = (diamondAngle + 2.5f) % 360.0f;

        // Вращение кружка
        circleAngle = (circleAngle + 1.2f) % 360.0f;
    }

    // ─── Рендер ───────────────────────────────────────────────────────────────

    @EventTarget
    public void a(DrawEvent event) {
        if (cachedTarget == null || showAnim < 0.01f) return;
        if (aM_.player == null || aM_.level == null) return;

        String mode = modeS.c();

        switch (mode) {
            case "Теги"   -> renderTags(event);
            case "Кружок" -> renderCircle(event);
            case "Ромб"   -> renderDiamond(event);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Режим ТЕГИ — 2D плашка над головой
    // ─────────────────────────────────────────────────────────────────────────

    private void renderTags(DrawEvent event) {
        if (!event.b() || event.i() == null) return;

        LivingEntity target = cachedTarget;
        float partial = event.g();

        Vec3 pos = MathUtil.a(target, partial)
                .add(0, target.getBbHeight() + 0.35, 0);
        Vector2f screen = ProjectUtil.a(pos.x, pos.y, pos.z);
        if (!ProjectUtil.a(screen)) return;

        float alpha   = showAnim;
        int   primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        int   color   = applyHitFlash(primary, target, partial);

        // Имя
        String name  = target.getName().getString();
        float  hp    = (float)(target.getHealth() / target.getMaxHealth());
        String hpStr = (int) target.getHealth() + " HP";
        float  dist  = (float) aM_.player.distanceTo(target);
        String info  = hpStr + "  " + (int) dist + "m";

        float fontSize   = 7.5f;
        float padding    = 3.0f;
        float nameW      = Fonts.e.a(name, fontSize);
        float infoW      = Fonts.e.a(info, fontSize);
        float lineH      = Fonts.e.a(fontSize);
        float totalW     = Math.max(nameW, infoW) + padding * 2.0f;
        float totalH     = lineH * 2.0f + padding * 3.0f;

        float x = screen.x() - totalW / 2.0f;
        float y = screen.y();

        // Фон
        int bg = ColorUtil.a(0, 0, 0, (int)(150 * alpha));
        event.d().a(event.i(), x, y, totalW, totalH, 4.0f, bg);

        // HP-бар
        int hpColor   = lerpColor(ColorUtil.a(220, 50, 50, 255), ColorUtil.a(80, 200, 80, 255), hp);
        int hpColorA  = ColorUtil.a(hpColor, alpha);
        event.d().a(event.i(), x + padding, y + padding,
                (totalW - padding * 2.0f) * hp, 2.0f, 1.0f, hpColorA);

        float textY = y + padding + 2.0f + padding * 0.5f;

        // Имя
        int nameColor = ColorUtil.a(applyHitFlash(color, target, partial), alpha);
        Fonts.e.a(event.i(), name, x + (totalW - nameW) / 2.0f, textY, fontSize, nameColor);

        // Инфо
        int infoColor = ColorUtil.a(ColorUtil.a(180, 180, 180, 255), alpha);
        Fonts.e.a(event.i(), info, x + (totalW - infoW) / 2.0f, textY + lineH + padding * 0.5f, fontSize, infoColor);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Режим КРУЖОК — горизонтальный круг у ног
    // ─────────────────────────────────────────────────────────────────────────

    private void renderCircle(DrawEvent event) {
        if (!event.c()) return;

        LivingEntity target = cachedTarget;
        float partial       = event.g();

        Vec3  pos   = MathUtil.a(target, partial);
        float alpha = showAnim;
        float r     = target.getBbWidth() * 0.6f + 0.1f;

        int primary  = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        int color    = applyHitFlash(primary, target, partial);
        int colorA   = ColorUtil.a(color, alpha);

        net.minecraft.world.phys.AABB ring = new net.minecraft.world.phys.AABB(
                pos.x - r, pos.y - 0.02,
                pos.z - r,
                pos.x + r, pos.y + 0.02,
                pos.z + r
        );

        // Рисуем через Draw3DProcessor — AABB обводка как кольцо
        event.e().a(event.h(), ring, colorA, 1.2f);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Режим РОМБ — вращающийся ромб (2D billboard поверх 3D)
    // ─────────────────────────────────────────────────────────────────────────

    private void renderDiamond(DrawEvent event) {
        if (!event.b() || event.i() == null) return;

        LivingEntity target  = cachedTarget;
        float        partial = event.g();

        Vec3 center = MathUtil.a(target, partial)
                .add(0, target.getBbHeight() / 2.0, 0);
        Vector2f screen = ProjectUtil.a(center.x, center.y, center.z);
        if (!ProjectUtil.a(screen)) return;

        float alpha   = showAnim;
        float dist    = (float) aM_.player.distanceTo(target);
        // Размер ромба зависит от дистанции
        float size    = Math.max(6.0f, 18.0f - dist * 0.4f) * alpha;

        int   primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        int   color   = applyHitFlash(primary, target, partial);

        float cx = screen.x();
        float cy = screen.y();
        float rad = (float) Math.toRadians(diamondAngle);

        // Четыре вершины ромба
        float[] px = new float[4];
        float[] py = new float[4];
        for (int i = 0; i < 4; i++) {
            float angle = rad + (float) Math.toRadians(i * 90);
            px[i] = cx + (float) Math.cos(angle) * size;
            py[i] = cy + (float) Math.sin(angle) * size;
        }

        // Рисуем 4 стороны как тонкие прямоугольники
        for (int i = 0; i < 4; i++) {
            int   next = (i + 1) % 4;
            int   c    = ColorUtil.a(color, alpha);
            drawLine2D(event, px[i], py[i], px[next], py[next], 1.5f, c);
        }
    }

    // ─── Утилиты ─────────────────────────────────────────────────────────────

    /** Возвращает таргет из модуля Aura, или null. */
    private LivingEntity getTarget() {
        try {
            Aura aura = Delta.h().d().t().B();
            if (aura != null && aura.m() && aura.s() != null) {
                LivingEntity t = aura.s();
                if (t.isAlive() && !t.isRemoved()) return t;
            }
        } catch (Exception ignored) {}

        // Запасной вариант: crosshair target
        if (aM_.crosshairPickEntity instanceof LivingEntity le && le.isAlive()) {
            return le;
        }
        return null;
    }

    /**
     * Если включён redHit и цель сейчас hurt — смешиваем цвет с красным.
     */
    private int applyHitFlash(int color, LivingEntity target, float partial) {
        if (!redHit.c()) return color;
        float hurt = Math.max(0.0f, target.hurtTime - partial);
        float t    = (float) Math.sin(hurt * Math.PI / 10.0);
        if (t <= 0.0f) return color;
        return lerpColor(color, ColorUtil.a(255, 50, 50, 255), t);
    }

    /** Линейная интерполяция двух ARGB цветов. */
    private static int lerpColor(int from, int to, float t) {
        return ColorUtil.a(from, to, t);
    }

    /**
     * Рисует линию между двумя 2D точками через Draw2DProcessor
     * как тонкий выровненный прямоугольник.
     */
    private void drawLine2D(DrawEvent event, float x1, float y1, float x2, float y2, float width, int color) {
        float dx  = x2 - x1;
        float dy  = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.5f) return;

        float nx = -dy / len * (width * 0.5f);
        float ny =  dx / len * (width * 0.5f);

        // Ограничивающий прямоугольник линии
        float minX = Math.min(Math.min(x1 + nx, x1 - nx), Math.min(x2 + nx, x2 - nx));
        float minY = Math.min(Math.min(y1 + ny, y1 - ny), Math.min(y2 + ny, y2 - ny));
        float maxX = Math.max(Math.max(x1 + nx, x1 - nx), Math.max(x2 + nx, x2 - nx));
        float maxY = Math.max(Math.max(y1 + ny, y1 - ny), Math.max(y2 + ny, y2 - ny));

        event.d().a(event.i(), minX, minY, maxX - minX, maxY - minY, 0.0f, color);
    }
}
