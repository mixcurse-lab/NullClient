package platform.client.utils.aura.rotation;

import static platform.api.module.Interface.aM_;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import platform.client.Delta;
import platform.client.features.modules.combat.AuraUtil;
import platform.client.utils.aura.AuraContext;
import platform.client.utils.math.MathUtil;
import platform.client.utils.rotation.Rotation;

/**
 * SpookyTimeRotation — Deluxe-style режимы по дистанции.
 *
 * dist > 2.9 блока  → HEAD SHAKE режим:
 *   Тряска по yaw (±4–8°), pitch почти не меняется (только к голове).
 *   Имитирует как человек ведёт взгляд при преследовании далёкой цели.
 *
 * dist < 2.9 блока  → MULTIPOINT режим:
 *   Курсор (cx,cy,cz) плывёт между случайными точками хитбокса.
 *   Yaw и pitch двигаются вместе к этой точке.
 *
 * Не двигаемся (скорость < порога) → STATIC режим:
 *   Pitch заморожен, бьём в одну зафиксированную точку.
 *   Yaw продолжает плавно следить за целью.
 *
 * Всегда:
 *  - GCD через AuraUtil.a(start, end, amount), amount = easeFactor/err.
 *  - Рандомизированная скорость (обновляется раз в 20–40 тиков).
 *  - Pitch drop когда цель дальше reach+1.5.
 *  - Hold on target loss 50мс.
 */
public class SpookyTimeRotation extends AuraRotation {

    // ─── multipoint cursor ────────────────────────────────────────────────────
    private float cx = 0.5f, cy = 0.75f, cz = 0.5f;
    private float dX = 0.5f, dY = 0.75f, dZ = 0.5f;
    private int   repickIn   = 0;
    private int   invisTicks = 0;
    private float lead       = 1.0f;

    // ─── static mode (не двигаемся) ───────────────────────────────────────────
    private float frozenPitch    = Float.NaN;   // зафиксированный pitch
    private float frozenYawOff   = 0.0f;         // зафиксированный yaw-offset от цели

    // ─── head shake ───────────────────────────────────────────────────────────
    private float shakeAmp  = 0.0f;
    private float shakeFreq = 180.0f;   // мс на период
    private int   shakeTtl  = 0;

    // ─── hold on loss ─────────────────────────────────────────────────────────
    private float holdYaw   = Float.NaN;
    private float holdPitch = Float.NaN;
    private long  lostAtMs  = -1L;

    // ─── смена цели ───────────────────────────────────────────────────────────
    private LivingEntity lastTarget;
    private int          reactDelay = 0;

    // ─── pitch lag ────────────────────────────────────────────────────────────
    private int pitchLag    = 4;
    private int pitchLagTtl = 0;

    // ─── рандомизированная скорость ───────────────────────────────────────────
    private float speedRand    = 1.0f;
    private int   speedRandTtl = 0;

    // ─── pitch drop вне reach ─────────────────────────────────────────────────
    private float dropPitch  = 0.0f;
    private float dropTarget = 0.0f;

    // ─────────────────────────────────────────────────────────────────────────

    public SpookyTimeRotation(AuraContext ctx) {
        super(ctx);
    }

    @Override
    public String name() {
        return "СпукиТайм";
    }

    @Override
    public void rotate() {
        if (aM_.player == null) return;

        // ── hold on target loss ───────────────────────────────────────────────
        if (ctx.target == null) {
            if (!Float.isNaN(holdYaw)) {
                long now = System.currentTimeMillis();
                if (lostAtMs < 0) lostAtMs = now;
                if (now - lostAtMs < 50L) {
                    Delta.h().d().k().a(new Rotation(holdYaw, holdPitch), 220.0f, 1, 1);
                } else {
                    holdYaw = Float.NaN; holdPitch = Float.NaN; lostAtMs = -1L;
                }
            }
            return;
        }
        lostAtMs = -1L;

        LivingEntity target   = ctx.target;
        boolean      attacking = ctx.timers[3] >= 0.0f;
        long         ms        = System.currentTimeMillis();

        // ── смена цели ────────────────────────────────────────────────────────
        if (target != lastTarget) {
            lastTarget  = target;
            reactDelay  = (int) MathUtil.a(2.0f, 4.0f);
            dropPitch   = 0.0f;
            dropTarget  = 0.0f;
            frozenPitch = Float.NaN;
            repickIn    = 0;
            cx = 0.5f; cy = 0.75f; cz = 0.5f;
        }

        // ── рандомизированная скорость ────────────────────────────────────────
        if (--speedRandTtl <= 0) {
            speedRand    = MathUtil.a(0.82f, 1.18f);
            speedRandTtl = (int) MathUtil.a(20.0f, 40.0f);
        }

        // ── pitch lag ─────────────────────────────────────────────────────────
        if (--pitchLagTtl <= 0) {
            pitchLag    = (int) MathUtil.a(3.0f, 6.0f);
            pitchLagTtl = (int) MathUtil.a(10.0f, 22.0f);
        }

        // ── геометрия цели ────────────────────────────────────────────────────
        AABB box  = target.getBoundingBox();
        Vec3 eye  = aM_.player.getEyePosition();
        Vec3 cent = box.getCenter();
        float dist = (float) eye.distanceTo(cent);

        // ── pitch drop вне reach ──────────────────────────────────────────────
        float reachLimit = ctx.reach + 1.5f;
        if (dist > reachLimit) {
            float over = Math.min(dist - reachLimit, 3.0f);
            dropTarget  = 3.0f + (over / 3.0f) * 2.0f;
        } else {
            dropTarget = 0.0f;
        }
        dropPitch += (dropTarget - dropPitch) * 0.15f;

        // ── определяем двигается ли игрок ─────────────────────────────────────
        Vec3 vel      = aM_.player.getDeltaMovement();
        boolean moving = (vel.x * vel.x + vel.z * vel.z) > 0.0025;   // ~0.05 блока/тик

        // ═════════════════════════════════════════════════════════════════════
        // ВЫБОР РЕЖИМА
        // ═════════════════════════════════════════════════════════════════════

        float finalYaw;
        float finalPitch;

        if (!moving) {
            // ── STATIC MODE: не двигаемся ─────────────────────────────────────
            // Фиксируем точку прицела при первом входе в режим
            if (Float.isNaN(frozenPitch)) {
                // Берём текущий pitch как зафиксированный
                frozenPitch  = aM_.player.getXRot();
                // Небольшое случайное смещение yaw от центра цели
                frozenYawOff = MathUtil.a(-1.5f, 1.5f);
            }

            // Yaw плавно следит за целью + фиксированное смещение
            float yawTgt  = ctx.yawToTarget + frozenYawOff;
            float yawDelta = Mth.wrapDegrees(yawTgt - aM_.player.getYRot());
            float easeY   = easeFactor(Math.abs(yawDelta)) * speedRand;
            if (reactDelay > 0) { easeY *= 0.15f; reactDelay--; }

            float nextYaw = aM_.player.getYRot() + yawDelta * easeY;
            finalYaw   = AuraUtil.a(aM_.player.getYRot(), nextYaw, 1.0f);
            // Pitch заморожен — не меняем
            finalPitch = Mth.clamp(
                AuraUtil.a(aM_.player.getXRot(), frozenPitch, 1.0f), -89.0f, 90.0f);

        } else {
            // Сбрасываем frozen состояние когда начали двигаться
            frozenPitch  = Float.NaN;
            frozenYawOff = 0.0f;

            if (dist > 2.9f) {
                // ── HEAD SHAKE MODE: цель дальше 2.9 ──────────────────────────
                // Pitch идёт к голове цели (верхней части хитбокса)
                // Yaw трясётся вокруг направления на цель

                // Обновляем параметры тряски
                if (--shakeTtl <= 0) {
                    shakeAmp  = MathUtil.a(4.0f, 8.0f);
                    shakeFreq = MathUtil.a(120.0f, 240.0f);   // мс
                    shakeTtl  = (int) MathUtil.a(8.0f, 18.0f);
                }

                // Pitch к голове: верхняя 15% хитбокса
                Vec3  headPt    = new Vec3(cent.x, box.maxY - box.getYsize() * 0.12, cent.z);
                Rotation toHead = Rotation.a(eye, headPt);
                float pitchTgt  = Mth.clamp(toHead.d() + dropPitch, -89.0f, 90.0f);

                // Yaw: направление на цель + синусоидальная тряска
                float shake = (float)(Math.sin(ms / shakeFreq * 2.0 * Math.PI) * shakeAmp);
                // При атаке убираем тряску чтобы попасть
                if (attacking) shake *= 0.15f;
                float yawTgt  = ctx.yawToTarget + shake;

                // Проверяем что тряска не уводит за хитбокс
                if (!AuraUtil.a(yawTgt, pitchTgt, ctx.reach, target, true)) {
                    yawTgt = ctx.yawToTarget + Mth.clamp(shake, -1.5f, 1.5f);
                }

                float yawDelta   = Mth.wrapDegrees(yawTgt  - aM_.player.getYRot());
                float pitchDelta = pitchTgt - aM_.player.getXRot();

                float easeY = easeFactor(Math.abs(yawDelta))   * speedRand;
                float easeP = easeFactor(Math.abs(pitchDelta)) * speedRand * 0.6f; // pitch медленнее
                if (attacking) { easeY = Math.min(easeY * 1.4f + 0.06f, 0.85f); easeP = Math.min(easeP * 1.4f, 0.75f); }
                if (reactDelay > 0) { easeY *= 0.15f; easeP *= 0.12f; reactDelay--; }

                float nextYaw   = aM_.player.getYRot() + yawDelta   * easeY;
                float nextPitch = Mth.clamp(aM_.player.getXRot() + pitchDelta * easeP, -89.0f, 90.0f);

                finalYaw   = AuraUtil.a(aM_.player.getYRot(), nextYaw,   1.0f);
                finalPitch = Mth.clamp(AuraUtil.a(aM_.player.getXRot(), nextPitch, 1.0f), -89.0f, 90.0f);

            } else {
                // ── MULTIPOINT MODE: цель ближе 2.9 ──────────────────────────
                // Курсор плывёт между случайными точками хитбокса

                // Repick
                boolean vis = cursorVis(target, box, eye);
                invisTicks = vis ? 0 : invisTicks + 1;
                if (--repickIn <= 0 || invisTicks >= 3) {
                    repick(target, box, eye);
                    invisTicks = 0;
                    repickIn   = (int) MathUtil.a(8.0f, 18.0f);
                }

                // Cursor дрейф со средней скоростью
                float cs = MathUtil.a(0.09f, 0.17f);
                cx += (dX - cx) * cs;
                cy += (dY - cy) * cs * MathUtil.a(0.85f, 1.15f);
                cz += (dZ - cz) * cs * MathUtil.a(0.85f, 1.15f);

                // Velocity prediction
                Vec3 tVel = target.getDeltaMovement();
                AABB pbox = box.move(tVel.x * lead * 0.8, tVel.y * lead * 0.45, tVel.z * lead * 0.8);

                float fx = Mth.clamp(cx, 0.03f, 0.97f);
                float fy = Mth.clamp(cy, 0.04f, 0.96f);
                float fz = Mth.clamp(cz, 0.03f, 0.97f);

                Vec3     aimPt = new Vec3(
                    Mth.lerp(fx, pbox.minX, pbox.maxX),
                    Mth.lerp(fy, pbox.minY, pbox.maxY),
                    Mth.lerp(fz, pbox.minZ, pbox.maxZ));
                Rotation aimed = Rotation.a(eye, aimPt);

                float yawTgt   = aimed.c();
                float pitchTgt = Mth.clamp(aimed.d() + dropPitch, -89.0f, 90.0f);

                int lagIdx = Mth.clamp(pitchLag - ctx.ticks, 0, 29);
                // В мультипоинт режиме pitch управляется курсором, не историей
                // Но добавляем лёгкий питч-лаг через смешение
                pitchTgt = Mth.lerp(0.25f, ctx.pitchHistory[lagIdx], pitchTgt);

                float yawDelta   = Mth.wrapDegrees(yawTgt   - aM_.player.getYRot());
                float pitchDelta = pitchTgt - aM_.player.getXRot();

                float easeY = easeFactor(Math.abs(yawDelta))   * speedRand;
                float easeP = easeFactor(Math.abs(pitchDelta)) * speedRand;
                if (attacking) { easeY = Math.min(easeY * 1.4f + 0.06f, 0.85f); easeP = Math.min(easeP * 1.4f + 0.05f, 0.75f); }
                if (reactDelay > 0) { easeY *= 0.15f; easeP *= 0.12f; reactDelay--; }

                float nextYaw   = aM_.player.getYRot() + yawDelta   * easeY;
                float nextPitch = Mth.clamp(aM_.player.getXRot() + pitchDelta * easeP, -89.0f, 90.0f);

                finalYaw   = AuraUtil.a(aM_.player.getYRot(), nextYaw,   1.0f);
                finalPitch = Mth.clamp(AuraUtil.a(aM_.player.getXRot(), nextPitch, 1.0f), -89.0f, 90.0f);

                // Pitch clamp по хитбоксу
                float minP = 90f, maxP = -90f;
                for (double bx2 : new double[]{box.minX, box.maxX})
                for (double by  : new double[]{box.minY, box.maxY})
                for (double bz2 : new double[]{box.minZ, box.maxZ}) {
                    double dx = bx2 - eye.x, dy = by - eye.y, dz = bz2 - eye.z;
                    double h  = Math.hypot(dx, dz);
                    if (h < 1e-6) continue;
                    float pc = (float)(-Math.toDegrees(Math.atan2(dy, h)));
                    if (pc < minP) minP = pc;
                    if (pc > maxP) maxP = pc;
                }
                if (minP < maxP)
                    finalPitch = Mth.clamp(finalPitch, minP - 1.0f, maxP + 1.0f);
            }

            // ── аварийный шаг на атаке ────────────────────────────────────────
            if (attacking && ctx.timers[8] <= 0.0f
                    && !AuraUtil.a(finalYaw, finalPitch, ctx.reach, target, true)) {
                float yawErr = Math.abs(Mth.wrapDegrees(ctx.yawToTarget - aM_.player.getYRot()));
                float pitErr = Math.abs(ctx.pitchToTarget - aM_.player.getXRot());
                float eTotal = (float) Math.hypot(yawErr, pitErr);
                float eScale = Math.min(eTotal, 18.0f) / Math.max(eTotal, 0.001f);
                float eY = aM_.player.getYRot()
                           + Mth.wrapDegrees(ctx.yawToTarget - aM_.player.getYRot()) * eScale;
                float eP = Mth.clamp(
                    aM_.player.getXRot() + (ctx.pitchToTarget - aM_.player.getXRot()) * eScale,
                    -89.0f, 90.0f);
                finalYaw   = AuraUtil.a(aM_.player.getYRot(), eY, 1.0f);
                finalPitch = Mth.clamp(AuraUtil.a(aM_.player.getXRot(), eP, 1.0f), -89.0f, 90.0f);
            }
        }

        // ── hold & send ───────────────────────────────────────────────────────
        holdYaw   = finalYaw;
        holdPitch = finalPitch;
        Delta.h().d().k().a(new Rotation(finalYaw, finalPitch), 220.0f, 1, 1);
    }

    // ─── утилиты ──────────────────────────────────────────────────────────────

    private static float easeFactor(float absDiff) {
        float f = 0.16f + (1.0f - (float) Math.exp(-absDiff / 24.0f)) * 0.38f;
        return Mth.clamp(f, 0.10f, 0.54f);
    }

    private boolean cursorVis(LivingEntity t, AABB box, Vec3 eye) {
        Vec3 p = new Vec3(
            Mth.lerp(cx, box.minX, box.maxX),
            Mth.lerp(cy, box.minY, box.maxY),
            Mth.lerp(cz, box.minZ, box.maxZ));
        Rotation r = Rotation.a(eye, p);
        return AuraUtil.a(r.c(), r.d(), ctx.reach, t, false);
    }

    private void repick(LivingEntity t, AABB box, Vec3 eye) {
        lead = MathUtil.a(0.6f, 1.4f);

        float band = MathUtil.a(0f, 1f);
        float yMin = band < 0.40f ? 0.65f : (band < 0.75f ? 0.32f : 0.04f);
        float yMax = Math.min(yMin + (band < 0.40f ? 0.28f : 0.34f), 1f);

        for (int i = 0; i < 7; i++) {
            float tx = MathUtil.a(0.12f, 0.88f);
            float ty = MathUtil.a(yMin, yMax);
            float tz = MathUtil.a(0.12f, 0.88f);
            Vec3  p  = new Vec3(
                Mth.lerp(tx, box.minX, box.maxX),
                Mth.lerp(ty, box.minY, box.maxY),
                Mth.lerp(tz, box.minZ, box.maxZ));
            Rotation r = Rotation.a(eye, p);
            if (AuraUtil.a(r.c(), r.d(), ctx.reach, t, false)) {
                dX = tx; dY = ty; dZ = tz; return;
            }
        }
        dX = 0.5f; dZ = 0.5f;
        dY = Mth.clamp((float)(eye.y - box.minY)
             / Math.max((float) box.getYsize(), 1e-4f), 0.1f, 0.9f);
    }
}
