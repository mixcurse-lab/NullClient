package platform.client.utils.aura.rotation;

import static platform.api.module.Interface.aM_;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import platform.client.Delta;
import platform.client.features.modules.combat.AuraUtil;
import platform.client.utils.aura.AuraContext;
import platform.client.utils.math.MathUtil;
import platform.client.utils.rotation.Rotation;


/**
 * MXRotation — байпас под MX AntiCheat (Mineland Xcalibur / ML-based).
 *
 * MX использует Bi-LSTM RNN который анализирует последовательности ротаций.
 * Он смотрит на:
 *  - Распределение yawDelta/pitchDelta (дисперсия должна быть как у человека)
 *  - Автокорреляцию: следующий шаг не должен быть линейно предсказуем
 *  - Ускорение (2-я производная): у человека оно случайно, не гладко
 *  - Корреляцию поворот↔атака: человек иногда бьёт не точно в цель
 *  - GCD-compliance: обязательно
 *  - Энтропию: высокая = хорошо (непредсказуемость)
 *
 * Стратегии обхода:
 *  1. Velocity accumulator — скорость накапливается случайными толчками,
 *     а не пересчитывается каждый тик. Это даёт нелинейное ускорение.
 *  2. Pre-generated noise table (256 значений, Brown noise) — каждый
 *     тик берём следующее значение. Brown noise = интеграл white noise,
 *     что близко к реальным движениям руки.
 *  3. Micro-correction drift — иногда специально чуть промахиваемся и
 *     корректируем. Человек делает это постоянно.
 *  4. Decorrelated yaw/pitch — скорость по pitch обновляется независимо
 *     от yaw, с другим шагом и другим noise. Bi-LSTM детектирует если
 *     они слишком коррелированы.
 *  5. Attack desync — иногда слегка поворачиваемся ПОСЛЕ атаки, а не
 *     строго ДО. ML замечает идеальную синхронность поворот→атака.
 */
public class MXRotation extends AuraRotation {

    // ─── Brown noise таблица ──────────────────────────────────────────────────
    private static final int NOISE_LEN = 256;
    private final float[] noiseY;   // yaw noise
    private final float[] noiseP;   // pitch noise (независимый)
    private int noiseIdxY = 0;
    private int noiseIdxP = 0;

    // ─── Velocity accumulator ─────────────────────────────────────────────────
    private float velY = 0.0f;   // текущая скорость по yaw (°/тик)
    private float velP = 0.0f;   // текущая скорость по pitch (°/тик)

    // ─── Micro-correction state ───────────────────────────────────────────────
    private float microOffsetY = 0.0f;   // текущее смещение от цели
    private float microOffsetP = 0.0f;
    private int   microTtlY    = 0;      // тиков до следующей коррекции
    private int   microTtlP    = 0;

    // ─── Смена цели ───────────────────────────────────────────────────────────
    private LivingEntity lastTarget;
    private int          reactDelay = 0;

    // ─── Pitch lag ────────────────────────────────────────────────────────────
    private int pitchLag    = 4;
    private int pitchLagTtl = 0;

    // ─── Attack desync ────────────────────────────────────────────────────────
    // После атаки делаем небольшую паузу перед тем как снова точно навестись
    private int desyncTicks = 0;

    // ─── конструктор ─────────────────────────────────────────────────────────

    public MXRotation(AuraContext ctx) {
        super(ctx);
        noiseY = generateBrownNoise(NOISE_LEN, 1.2f);
        noiseP = generateBrownNoise(NOISE_LEN, 0.9f);
    }

    @Override
    public String name() {
        return "МХ";
    }

    // ─── основная логика ─────────────────────────────────────────────────────

    @Override
    public void rotate() {
        if (aM_.player == null || ctx.target == null) return;

        LivingEntity target   = ctx.target;
        boolean      attacking = ctx.timers[3] >= 0.0f;

        // ── 1. Смена цели ─────────────────────────────────────────────────────
        if (target != lastTarget) {
            lastTarget    = target;
            reactDelay    = (int) MathUtil.a(3.0f, 6.0f);
            velY          = 0.0f;
            velP          = 0.0f;
            microOffsetY  = 0.0f;
            microOffsetP  = 0.0f;
        }

        // ── 2. Pitch lag ──────────────────────────────────────────────────────
        if (--pitchLagTtl <= 0) {
            pitchLag    = (int) MathUtil.a(3.0f, 7.0f);
            pitchLagTtl = (int) MathUtil.a(12.0f, 28.0f);
        }
        int   lagIdx     = Mth.clamp(pitchLag - ctx.ticks, 0, 29);
        float yawTarget  = ctx.yawToTarget;
        float pitchTarget = ctx.pitchHistory[lagIdx];

        // ── 3. Micro-correction drift ─────────────────────────────────────────
        // Периодически вводим небольшое смещение от цели, потом корректируем.
        // Это создаёт паттерн "промахнуться → поправить" как у человека.
        if (--microTtlY <= 0) {
            // 40% шанс ввести смещение, 60% — убрать его
            microOffsetY  = (Math.random() < 0.4)
                            ? (float)(Math.random() - 0.5) * MathUtil.a(0.8f, 2.5f)
                            : 0.0f;
            microTtlY     = (int) MathUtil.a(8.0f, 20.0f);
        }
        if (--microTtlP <= 0) {
            microOffsetP  = (Math.random() < 0.35)
                            ? (float)(Math.random() - 0.5) * MathUtil.a(0.5f, 1.8f)
                            : 0.0f;
            microTtlP     = (int) MathUtil.a(6.0f, 18.0f);
        }

        // Проверяем что смещение не уводит за хитбокс
        float adjTarget = yawTarget + microOffsetY;
        float adjPitch  = Mth.clamp(pitchTarget + microOffsetP, -89.0f, 90.0f);
        if (!AuraUtil.a(adjTarget, adjPitch, ctx.reach, target, true)) {
            adjTarget = yawTarget;
            adjPitch  = Mth.clamp(pitchTarget, -89.0f, 90.0f);
        }

        // ── 4. Дельты ─────────────────────────────────────────────────────────
        float yawDelta   = Mth.wrapDegrees(adjTarget   - aM_.player.getYRot());
        float pitchDelta = adjPitch - aM_.player.getXRot();

        // ── 5. Attack desync ──────────────────────────────────────────────────
        if (desyncTicks > 0) {
            desyncTicks--;
        }
        // После атаки — небольшой "рывок" в сторону перед возвратом к цели
        // (имитирует отдачу/swing-motion)
        boolean wasAttacking = ctx.timers[3] == 0.0f;   // только что ударили
        if (wasAttacking && desyncTicks == 0) {
            desyncTicks  = (int) MathUtil.a(1.0f, 3.0f);
        }
        float desyncMult = (desyncTicks > 0) ? MathUtil.a(0.6f, 0.85f) : 1.0f;

        // ── 6. Velocity accumulator с Brown noise ─────────────────────────────
        // Целевая скорость = пропорциональна ошибке (с кривой ускорения)
        float errY = Math.abs(yawDelta);
        float errP = Math.abs(pitchDelta);

        // Функция ускорения: быстро разгоняется, но не линейно
        float targetVelY = accelerationCurve(errY) * MathUtil.a(0.82f, 1.18f);
        float targetVelP = accelerationCurve(errP) * 0.75f * MathUtil.a(0.80f, 1.20f);

        // Brown noise добавляет случайные "толчки" к скорости
        float brownY = noiseY[noiseIdxY] * 0.4f;
        float brownP = noiseP[noiseIdxP] * 0.35f;
        noiseIdxY = (noiseIdxY + 1) % NOISE_LEN;
        noiseIdxP = (noiseIdxP + 1) % NOISE_LEN;

        // Экспоненциальный фильтр скорости (alpha разный для разгона и торможения)
        float alphaAccel  = 0.22f;   // разгон медленнее
        float alphaDecel  = 0.35f;   // торможение быстрее (не проскакиваем)
        float alphaY = (targetVelY > velY) ? alphaAccel : alphaDecel;
        float alphaP = (targetVelP > velP) ? alphaAccel : alphaDecel;

        velY += (targetVelY - velY) * alphaY;
        velP += (targetVelP - velP) * alphaP;

        // Финальная скорость = filtered velocity + brown noise + desync
        float effVelY = (velY + brownY) * desyncMult;
        float effVelP = (velP + brownP) * desyncMult;

        // В фазе реакции — почти стоим
        if (reactDelay > 0) {
            effVelY *= 0.1f;
            effVelP *= 0.08f;
            reactDelay--;
        }

        // ── 7. Шаг с GCD-коррекцией ───────────────────────────────────────────
        float amountY = (errY > 0.1f) ? Mth.clamp(effVelY / errY, 0.001f, 1.0f) : 1.0f;
        float amountP = (errP > 0.1f) ? Mth.clamp(effVelP / errP, 0.001f, 1.0f) : 1.0f;

        float finalYaw   = AuraUtil.a(aM_.player.getYRot(), adjTarget, amountY);
        float finalPitch = Mth.clamp(
            AuraUtil.a(aM_.player.getXRot(), adjPitch, amountP),
            -89.0f, 90.0f
        );

        // ── 8. Аварийный шаг на атаке если совсем не попадаем ─────────────────
        if (attacking && ctx.timers[8] <= 0.0f
                && !AuraUtil.a(finalYaw, finalPitch, ctx.reach, target, true)) {
            float eY = (errY > 0.1f) ? Mth.clamp(Math.min(velY * 2.5f, 18.0f) / errY, 0.001f, 1.0f) : 1.0f;
            float eP = (errP > 0.1f) ? Mth.clamp(Math.min(velP * 2.0f, 15.0f) / errP, 0.001f, 1.0f) : 1.0f;
            finalYaw   = AuraUtil.a(aM_.player.getYRot(), yawTarget,  eY);
            finalPitch = Mth.clamp(AuraUtil.a(aM_.player.getXRot(), adjPitch, eP), -89.0f, 90.0f);
        }

        // ── 9. Отправка ───────────────────────────────────────────────────────
        float turnSpeed = Math.max(effVelY, effVelP) * 1.5f + MathUtil.a(0.5f, 2.5f);
        turnSpeed       = Mth.clamp(turnSpeed, 3.0f, 220.0f);
        Delta.h().d().k().a(new Rotation(finalYaw, finalPitch), turnSpeed, 1, 1);
    }

    // ─── Утилиты ──────────────────────────────────────────────────────────────

    /**
     * Нелинейная кривая ускорения.
     * err=0  → 0 °/тик
     * err=5  → ~2.5 °/тик  (медленно вблизи цели)
     * err=20 → ~8   °/тик  (средняя скорость)
     * err=50 → ~13  °/тик  (быстро издалека, не снап)
     */
    private static float accelerationCurve(float err) {
        // f(x) = maxSpeed * (1 - exp(-k*x))
        float maxSpeed = MathUtil.a(12.0f, 15.0f);
        float k        = 0.065f;
        return maxSpeed * (1.0f - (float)Math.exp(-k * err));
    }

    /**
     * Генерирует Brown noise (интеграл White noise) нормализованный к [-amplitude, +amplitude].
     * Brown noise имеет спектральную плотность 1/f² — близко к реальным движениям руки.
     */
    private static float[] generateBrownNoise(int len, float amplitude) {
        float[] buf    = new float[len];
        float   accum  = 0.0f;
        float   maxAbs = 1e-6f;
        for (int i = 0; i < len; i++) {
            accum  += (float)(Math.random() * 2.0 - 1.0) * 0.1f;
            buf[i]  = accum;
            maxAbs  = Math.max(maxAbs, Math.abs(accum));
        }
        // Нормализуем к [-amplitude, +amplitude]
        for (int i = 0; i < len; i++) {
            buf[i] = (buf[i] / maxAbs) * amplitude;
        }
        return buf;
    }
}
