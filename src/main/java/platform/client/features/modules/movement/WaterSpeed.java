package platform.client.features.modules.movement;

import static platform.api.module.Interface.aM_;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Input;
import platform.api.event.events.client.TickEvent;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.SliderSetting;
import platform.client.utils.math.MathUtil;

/**
 * WaterSpeed — ускорение в воде с байпасом под MX AntiCheat.
 *
 * MX в воде отслеживает:
 *  - Резкие скачки горизонтальной скорости (ожидается плавное нарастание)
 *  - Соотношение deltaMovement.y к горизонтальной скорости
 *    (слишком быстрый подъём при нулевом прыжке — флаг)
 *  - Статистику распределения скоростей по тикам (дисперсия, автокорреляция)
 *  - Длину нахождения в воде без смены скорости (повторяющийся паттерн)
 *
 * Байпас:
 *  1. Velocity accumulator — скорость накапливается постепенно через
 *     экспоненциальный фильтр, а не задаётся сразу целевым значением.
 *  2. Brown noise — небольшое случайное смещение к финальному вектору
 *     на каждый тик, имитирует "неровный" стиль плавания.
 *  3. Ограниченный Y — нейтрализуем только сопротивление воды,
 *     не поднимаем игрока явно вверх (anticheat флагует вертикальную аномалию).
 *  4. Jitter period — раз в несколько тиков делаем небольшой "провал"
 *     скорости, разрушая монотонный профиль (детектор повторяемости).
 *  5. Масштаб скорости — максимум ограничен разумными значениями
 *     (~0.20 м/тик + dolphin's grace), далеко от детект-порога.
 */
@ModuleRegister(a = "Water Speed", b = "Ускоряет передвижение в воде", c = Category.Movement)
public class WaterSpeed extends Module {

    // ─── Настройки ────────────────────────────────────────────────────────────
    // Диапазон намеренно мизерный — MX флагует >~1.5× ванильной скорости.
    private final SliderSetting speedMultiplier = new SliderSetting(
            "Множитель", 1.15f, 1.0f, 1.6f, 0.05f
    );
    private final BooleanSetting verticalBoost = new BooleanSetting(
            "Вертикальный буст", false
    );
    private final SliderSetting verticalStrength = (SliderSetting) new SliderSetting(
            "Сила подъёма", 0.012f, 0.005f, 0.030f, 0.001f
    ).a(() -> verticalBoost.c());

    // ─── Brown noise таблица ──────────────────────────────────────────────────
    private static final int NOISE_LEN = 128;
    private final float[] noiseX;
    private final float[] noiseZ;
    private int noiseIdx = 0;

    // ─── Velocity accumulator ─────────────────────────────────────────────────
    // Текущая накопленная горизонтальная скорость (°/тик → м/тик здесь м/тик)
    private float currentSpeed = 0.0f;

    // ─── Jitter ───────────────────────────────────────────────────────────────
    private int jitterTtl    = 0;   // тиков до следующего "провала"
    private int jitterActive = 0;   // тиков текущего "провала"

    // ─── Счётчик тиков в воде ────────────────────────────────────────────────
    private int waterTicks = 0;

    // ─── Конструктор ─────────────────────────────────────────────────────────
    public WaterSpeed() {
        a(speedMultiplier, verticalBoost, verticalStrength);
        noiseX = generateBrownNoise(NOISE_LEN, 0.006f);
        noiseZ = generateBrownNoise(NOISE_LEN, 0.006f);
    }

    // ─── Enable / Disable ────────────────────────────────────────────────────

    @Override
    public void b() {
        super.b();
        currentSpeed  = 0.0f;
        waterTicks    = 0;
        jitterTtl     = nextJitterInterval();
        jitterActive  = 0;
        noiseIdx      = 0;
    }

    @Override
    public void c() {
        super.c();
        currentSpeed  = 0.0f;
        waterTicks    = 0;
    }

    // ─── Основная логика ─────────────────────────────────────────────────────

    @EventTarget
    public void onTick(TickEvent event) {
        if (aM_.player == null || aM_.level == null) return;

        // Работаем только когда игрок в воде и движется
        if (!aM_.player.isInWater()) {
            // Сброс накопленной скорости при выходе из воды
            currentSpeed = 0.0f;
            waterTicks   = 0;
            return;
        }

        // Проверяем, есть ли горизонтальный ввод
        Input keys = aM_.player.input.keyPresses;
        boolean moving = keys.forward() || keys.backward() || keys.left() || keys.right();
        if (!moving) {
            // Плавный сброс скорости, а не мгновенный (MX смотрит на резкие нули)
            currentSpeed *= 0.72f;
            return;
        }

        waterTicks++;

        // ── 1. Jitter period ──────────────────────────────────────────────────
        // Раз в N тиков делаем небольшой "провал" скорости.
        // Нарушает монотонный профиль, который детектится как аномалия повторяемости.
        float jitterMult = 1.0f;
        if (jitterActive > 0) {
            // Во время провала — двигаемся чуть медленнее чем цель
            jitterMult = MathUtil.a(0.72f, 0.88f);
            jitterActive--;
        } else if (--jitterTtl <= 0) {
            jitterTtl    = nextJitterInterval();
            jitterActive = (int) MathUtil.a(1.0f, 3.0f);
            jitterMult   = MathUtil.a(0.72f, 0.88f);
        }

        // ── 2. Целевая скорость ───────────────────────────────────────────────
        // Базовая ванильная скорость плавания ≈ 0.13 м/тик (без эффектов)
        // Dolphin's Grace даёт ≈ 0.96 (очень быстро), Speed ≈ +0.04 каждый уровень
        float baseWaterSpeed = 0.13f;
        if (aM_.player.hasEffect(MobEffects.DOLPHINS_GRACE)) baseWaterSpeed = 0.27f;
        if (aM_.player.hasEffect(MobEffects.SPEED)) {
            int lvl = aM_.player.getEffect(MobEffects.SPEED).getAmplifier() + 1;
            baseWaterSpeed += 0.04f * lvl;
        }

        // Целевая = база * множитель, но не слишком агрессивно (MX детектит >2× базы)
        float mult   = speedMultiplier.c().floatValue();
        // Дополнительный случайный "шум" к множителю — разрушает точное кратное
        float mNoise = MathUtil.a(-0.02f, 0.02f);
        float target = Mth.clamp(baseWaterSpeed * (mult + mNoise), baseWaterSpeed, baseWaterSpeed * 1.55f);

        // ── 3. Velocity accumulator (экспоненциальный фильтр) ─────────────────
        // Очень медленный разгон — нарастание за ~8 тиков, а не мгновенно.
        float alpha = (target > currentSpeed) ? 0.10f : 0.25f;
        currentSpeed += (target - currentSpeed) * alpha;

        // Применяем jitter к накопленной скорости
        float effSpeed = currentSpeed * jitterMult;

        // ── 4. Brown noise ────────────────────────────────────────────────────
        float bx = noiseX[noiseIdx];
        float bz = noiseZ[noiseIdx];
        noiseIdx = (noiseIdx + 1) % NOISE_LEN;

        // ── 5. Построение вектора движения ───────────────────────────────────
        float forward   = aM_.player.input.getMoveVector().y;
        float sideways  = aM_.player.input.getMoveVector().x;
        double len = Math.hypot(forward, sideways);
        if (len < 1e-5) return;

        forward  = (float) (forward  / len);
        sideways = (float) (sideways / len);

        double rad = Math.toRadians(aM_.player.getYRot());
        double sinY = Math.sin(rad);
        double cosY = Math.cos(rad);

        double motionX = ((-forward * sinY) + (sideways * cosY)) * effSpeed + bx;
        double motionZ = (( forward * cosY) + (sideways * sinY)) * effSpeed + bz;

        // ── 6. Y — не трогаем без нужды (MX флагует любую Y-аномалию) ─────────
        double currentY = aM_.player.getDeltaMovement().y;
        double newY;

        if (verticalBoost.c()) {
            // Мизерный impульс вверх ТОЛЬКО при явном прыжке игрока.
            // Без условия MX флагует Y-аномалию даже при малых значениях.
            boolean jumping = keys.jump();
            if (jumping) {
                float vStr = verticalStrength.c().floatValue();
                // Добавляем крохотный импульс, ограниченный 0.08 м/тик
                newY = Math.min(currentY + MathUtil.a(vStr * 0.9f, vStr * 1.05f), 0.08f);
            } else {
                newY = currentY; // не трогаем
            }
        } else {
            newY = currentY; // Y полностью на ванили
        }

        aM_.player.setDeltaMovement(motionX, newY, motionZ);
    }

    // ─── Утилиты ──────────────────────────────────────────────────────────────

    /**
     * Случайный интервал между "провалами" скорости.
     * 8–18 тиков = 0.4–0.9 секунды. Достаточно редко чтобы не замедлять,
     * достаточно часто чтобы ломать автокорреляцию.
     */
    private static int nextJitterInterval() {
        return (int) MathUtil.a(8.0f, 18.0f);
    }

    /**
     * Brown noise (интеграл White noise), нормализованный к [-amplitude, +amplitude].
     * 1/f² спектральная плотность — ближе к реальным движениям тела в воде.
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
        // Нормализация
        for (int i = 0; i < len; i++) {
            buf[i] = (buf[i] / maxAbs) * amplitude;
        }
        return buf;
    }
}
