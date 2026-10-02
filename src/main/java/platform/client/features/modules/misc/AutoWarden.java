package platform.client.features.modules.misc;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.pathing.goals.GoalBlock;
import platform.api.event.events.client.SoundEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.StringSetting;
import platform.client.Delta;
import platform.client.features.modules.render.WardenESP;
import platform.client.utils.math.MathUtil;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.player.ServerUtil;
import platform.client.utils.rotation.Rotation;
import platform.client.utils.text.ChatUtil;
import platform.inject.invokers.MinecraftInvoker;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static platform.api.module.Interface.aM_;

@ModuleRegister(a = "Auto Warden", b = "Автоматизирует фарм сундуков в городе варденов", c = Category.Misc)
public class AutoWarden extends Module {
    private final BooleanSetting useSpeed        = new BooleanSetting("Использовать скорость", false);
    private final BooleanSetting fleePlayers     = new BooleanSetting("Уходить от игроков", true);
    private final ModeSetting    lootPriority    = new ModeSetting("Приоритет лута", "Средний", "Низкий", "Средний", "Высокий");
    private final StringSetting  storageAnarchy  = new StringSetting("Анархия склада", "0", true);
    // ─── новые настройки ──────────────────────────────────────────────────────
    /** Плавная SPAngle-ротация при взаимодействии с сундуком (байпас SpookyTime) */
    private final BooleanSetting smoothRotation  = new BooleanSetting("Плавная ротация (SpookyTime)", true);
    /** Случайные осмотры во время движения Baritone (нейро-поведение) */
    private final BooleanSetting neuroLook       = new BooleanSetting("Нейро движение", true);
    private final BooleanSetting debug           = new BooleanSetting("Отладка", false);

    private final Map<BlockPos, Integer> chestOpenCounts = new HashMap<>();
    private final List<BlockPos> wardens      = new ArrayList<>();
    private final List<Integer>  anarchyList  = new ArrayList<>();
    private State   state       = State.SAVE;
    private int     farmAnarchy = -1;
    private int     roarUntil;
    private int     nextHomeCommand;
    private int     lastAnarchyCommand = Integer.MIN_VALUE;
    private int     lastPathAge;
    private BlockPos pathTarget;
    private BlockPos targetChest;
    private BlockPos receiverChest;
    private BlockPos fleeGoal;
    private int      receiverScanAge = Integer.MIN_VALUE;
    // ─── счётчик уникальных открытых сундуков за цикл ────────────────────────
    /** Сколько уникальных сундуков открыто за текущий farming-цикл */
    private int  totalChestsOpened   = 0;
    /** Тик когда был открыт 7-й сундук (−1 = не достигнуто) */
    private int  seventhChestTick    = -1;
    /** Количество сундуков после которого уходим на склад */
    private static final int CHESTS_BEFORE_STORAGE = 7;
    /** Задержка перед уходом на склад после 7-го сундука: 31 секунда = 620 тиков */
    private static final int STORAGE_WAIT_TICKS    = 620;
    private boolean  enabledWardenEsp;
    private Boolean  previousAvoidance;
    private Boolean  previousBlockFreeLook;
    private Integer  previousMaxFallHeight;
    private Double   previousRandomLooking;
    private Double   previousRandomLooking113;

    // ─── Плавная ротация (SpookyTime-байпас) ─────────────────────────────────
    // Независимый exponential-smoothed поворот к сундуку без мгновенного snap.
    // Используем ту же схему что и SpookyTimeRotation: smoothstep + exp-filter.
    private float smoothRotYaw   = 0.0f;
    private float smoothRotPitch = 0.0f;
    private float smoothSpeedY   = 3.0f;
    private float smoothSpeedP   = 2.5f;
    private boolean smoothRotInit = false;   // true = нужно инициализировать

    // ─── Нейро движение (NeuroLook) ──────────────────────────────────────────
    // Пока Baritone ведёт игрока, периодически делаем небольшие случайные
    // осмотры — это разбивает "мертвый взгляд вперёд", характерный для ботов.
    private int    neuroLookTimer  = 0;   // тиков до следующего осмотра
    private float  neuroTargetYaw  = 0.0f;
    private float  neuroTargetPitch = 0.0f;
    private int    neuroLookDuration = 0;  // тиков продолжительности осмотра
    private int    neuroLookAge    = 0;   // сколько тиков уже смотрим
    private boolean neuroActive   = false;

    public AutoWarden() {
        a(this.useSpeed, this.fleePlayers, this.lootPriority, this.storageAnarchy,
          this.smoothRotation, this.neuroLook, this.debug);
    }

    public List<Integer> getAnarchyList() { return anarchyList; }

    public boolean addAnarchy(int anarchy) {
        if (anarchy < 0 || anarchy > 999 || anarchyList.contains(anarchy)) return false;
        anarchyList.add(anarchy);
        return true;
    }

    public boolean removeAnarchy(int anarchy) {
        return anarchyList.remove(Integer.valueOf(anarchy));
    }

    @Override
    public void b() {
        super.b();
        farmAnarchy = ServerUtil.a.d();
        state = State.COLLECTING;
        clearTargets();
        chestOpenCounts.clear();
        smoothRotInit = true;
        neuroLookTimer = 0;
        neuroActive = false;
        totalChestsOpened = 0;
        seventhChestTick  = -1;
        WardenESP esp = Delta.h().d().t().i();
        enabledWardenEsp = !esp.m();
        if (enabledWardenEsp) esp.a();
        applyBaritoneSettings(true);
        ChatUtil.a("Auto Warden запущен. Shift + Пробел — выключить.");
    }

    @Override
    public void c() {
        super.c();
        cancelPathing();
        applyBaritoneSettings(false);
        if (enabledWardenEsp) Delta.h().d().t().i().a(false);
        enabledWardenEsp = false;
        clearTargets();
        chestOpenCounts.clear();
    }

    private void applyBaritoneSettings(boolean enable) {
        var settings = BaritoneAPI.getSettings();
        if (enable) {
            previousAvoidance          = settings.avoidance.value;
            previousBlockFreeLook      = settings.blockFreeLook.value;
            previousMaxFallHeight      = settings.maxFallHeightNoWater.value;
            previousRandomLooking      = settings.randomLooking.value;
            previousRandomLooking113   = settings.randomLooking113.value;
            settings.avoidance.value          = true;
            settings.blockFreeLook.value      = true;
            settings.maxFallHeightNoWater.value = 256;
            // randomLooking отдаём NeuroLook-системе — выключаем встроенный
            // чтобы не конкурировать с нашими поворотами
            settings.randomLooking.value      = 0.0d;
            settings.randomLooking113.value   = 0.0d;
        } else {
            if (previousAvoidance        != null) settings.avoidance.value          = previousAvoidance;
            if (previousBlockFreeLook    != null) settings.blockFreeLook.value      = previousBlockFreeLook;
            if (previousMaxFallHeight    != null) settings.maxFallHeightNoWater.value = previousMaxFallHeight;
            if (previousRandomLooking    != null) settings.randomLooking.value      = previousRandomLooking;
            if (previousRandomLooking113 != null) settings.randomLooking113.value   = previousRandomLooking113;
            previousAvoidance = null; previousBlockFreeLook = null;
            previousMaxFallHeight = null; previousRandomLooking = null;
            previousRandomLooking113 = null;
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (aM_.gui.screen() instanceof DeathScreen && aM_.player != null && aM_.player.deathTime >= 5) {
            aM_.player.respawn();
        }
        if (aM_.player == null || aM_.level == null) return;
        if (farmAnarchy < 0 && ServerUtil.a.d() >= 0) farmAnarchy = ServerUtil.a.d();
        if (debug.c() && aM_.player.tickCount % 100 == 0) {
            ChatUtil.a("Auto Warden: " + state + " | анархия " + ServerUtil.a.d() + " -> " + farmAnarchy);
        }
        if (aM_.options.keyShift.isDown() && aM_.options.keyJump.isDown()) {
            a(false);
            return;
        }
        updateWardens();
        if (aM_.player.tickCount < 5) return;
        if (ServerUtil.a.d() < 0) {
            state = State.SAVE;
            return;
        }
        // ── NeuroLook: случайные осмотры пока Baritone ведёт ─────────────────
        tickNeuroLook();
        switch (state) {
            case SAVE      -> handleSave();
            case TAKE      -> handleTake();
            case COLLECTING -> handleCollecting();
            case ESCAPE    -> handleEscape();
            case FLEE      -> handleFlee();
            case STORAGE   -> handleStorage();
        }
    }

    @EventTarget
    public void a(SoundEvent event) {
        if (aM_.player == null) return;
        String path = event.b().getIdentifier().getPath();
        if (path.contains("warden.roar") || path.contains("warden.angry") || path.contains("warden.sonic")) {
            roarUntil = aM_.player.tickCount + 100;
            // Быстрый escape: немедленно отменяем текущий путь, чтобы
            // Baritone начал переключаться на escape-цель без задержки
            if (state == State.COLLECTING || state == State.TAKE) {
                cancelPathing();
                state = State.ESCAPE;
            }
        }
    }

    // ─── NeuroLook ────────────────────────────────────────────────────────────
    /**
     * Пока Baritone ведёт игрока — периодически смотрим по сторонам.
     * Не активируется если игрок взаимодействует с сундуком или стоит на месте.
     * Паттерн: ждём neuroLookTimer тиков → генерируем случайный угол →
     * плавно поворачиваемся neuroLookDuration тиков → возвращаемся.
     *
     * Диапазоны взяты из анализа реального игрока:
     *  - Пауза между осмотрами: 40–120 тиков (2–6 с)
     *  - Угол осмотра: ±30° по yaw, ±10° по pitch
     *  - Продолжительность: 8–18 тиков
     */
    private void tickNeuroLook() {
        if (!neuroLook.c()) return;
        // Не мешаем когда игрок взаимодействует с сундуком
        if (aM_.gui.screen() instanceof ContainerScreen) return;
        // Применяем только пока движемся (Baritone идёт)
        if (!isMoving()) {
            neuroActive = false;
            return;
        }

        if (!neuroActive) {
            // Считаем тики до следующего осмотра
            neuroLookTimer--;
            if (neuroLookTimer > 0) return;
            // Генерируем новый осмотр
            float baseYaw   = aM_.player.getYRot();
            float basePitch = aM_.player.getXRot();
            neuroTargetYaw   = baseYaw   + (float)(Math.random() * 60.0 - 30.0);
            neuroTargetPitch = Mth.clamp(basePitch + (float)(Math.random() * 20.0 - 10.0), -30.0f, 30.0f);
            neuroLookDuration = (int) MathUtil.a(8.0f, 18.0f);
            neuroLookAge = 0;
            neuroActive  = true;
            // Следующий осмотр через 40–120 тиков
            neuroLookTimer = (int) MathUtil.a(40.0f, 120.0f);
        }

        // Активный осмотр: плавно поворачиваемся к neuroTarget
        neuroLookAge++;
        if (neuroLookAge > neuroLookDuration) {
            neuroActive = false;
            return;
        }
        // Плавное eased движение к цели (та же формула что SpookyTimeRotation)
        float yawDelta   = Mth.wrapDegrees(neuroTargetYaw   - aM_.player.getYRot());
        float pitchDelta = neuroTargetPitch - aM_.player.getXRot();
        float speedY = Mth.clamp(Math.abs(yawDelta)   * 0.22f, 0.5f, 8.0f);
        float speedP = Mth.clamp(Math.abs(pitchDelta) * 0.22f, 0.5f, 6.0f);
        float scaleY = ease(Mth.clamp(speedY / Math.max(Math.abs(yawDelta),   0.001f), 0.0f, 1.0f));
        float scaleP = ease(Mth.clamp(speedP / Math.max(Math.abs(pitchDelta), 0.001f), 0.0f, 1.0f));
        float nextYaw   = aM_.player.getYRot()   + yawDelta   * scaleY;
        float nextPitch = Mth.clamp(aM_.player.getXRot() + pitchDelta * scaleP, -89.0f, 89.0f);
        // priority=0 — не перебиваем более важные ротации (interactChest)
        Delta.h().d().k().a(new Rotation(nextYaw, nextPitch), 40.0f, 0, 0);
    }

    // ─── Handlers ─────────────────────────────────────────────────────────────

    private void handleSave() {
        if (farmAnarchy >= 0 && ServerUtil.a.d() != farmAnarchy) {
            requestAnarchySwitch(farmAnarchy);
            return;
        }
        state = State.TAKE;
    }

    private void handleTake() {
        chestOpenCounts.clear();
        totalChestsOpened = 0;
        seventhChestTick  = -1;
        state = State.COLLECTING;
    }

    private void handleCollecting() {
        // ── Таймер после 7-го сундука ─────────────────────────────────────────
        // Открыли 7 сундуков → ждём 31 секунду → уходим на склад
        if (seventhChestTick >= 0) {
            int elapsed = aM_.player.tickCount - seventhChestTick;
            if (elapsed >= STORAGE_WAIT_TICKS) {
                int storage = getStorageAnarchy();
                if (storage > 0 && storage != farmAnarchy) {
                    seventhChestTick = -1;
                    state = State.FLEE;
                    fleeGoal = null;
                    cancelPathing();
                    if (debug.c()) ChatUtil.a("Auto Warden: 7 сундуков открыто, уходим на склад.");
                } else {
                    // Склад не задан — просто сбрасываем и продолжаем фарм
                    seventhChestTick  = -1;
                    totalChestsOpened = 0;
                    chestOpenCounts.clear();
                }
            } else {
                // Ждём, продолжаем двигаться если нужно но не открываем сундуки
                if (debug.c() && aM_.player.tickCount % 20 == 0) {
                    ChatUtil.a("Auto Warden: ждём уход на склад... " + (STORAGE_WAIT_TICKS - elapsed) / 20 + "с");
                }
                return;
            }
            return;
        }
        if (fleePlayers.c() && hasNearbyPlayer()) {
            state = State.FLEE;
            fleeGoal = null;
            cancelPathing();
            return;
        }
        int potionSupplier = potionSupplierAnarchy();
        if (needsInvisibilityPotion() && potionSupplier >= 0) {
            if (ServerUtil.a.d() != potionSupplier) {
                requestAnarchySwitch(potionSupplier);
                return;
            }
            if (findSlot(this::isInvisibilityPotion) >= 0) {
                state = State.SAVE;
                return;
            }
            if (Delta.h().d().v().k().a()) return;
            handleFarmChest();
            return;
        }
        if (shouldEscape()) {
            state = State.ESCAPE;
            return;
        }
        if (useSpeed.c()) {
            MobEffectInstance speed = aM_.player.getEffect(MobEffects.SPEED);
            int speedPotion = findSlot(this::isSpeedPotion);
            if ((speed == null || speed.getDuration() < 200) && speedPotion >= 0) {
                Delta.h().d().v().k().a(speedPotion);
                return;
            }
        }
        if (needsInvisibilityPotion()) {
            int potion = findSlot(this::isInvisibilityPotion);
            if (potion >= 0) {
                Delta.h().d().v().k().a(potion);
            } else if (aM_.player.tickCount % 100 == 0) {
                ChatUtil.a("Auto Warden: в инвентаре нет зелья невидимости.");
            }
            return;
        }
        if (!isInFarmArea()) {
            if (aM_.player.tickCount >= nextHomeCommand && aM_.player.connection != null) {
                aM_.player.connection.sendCommand("home");
                nextHomeCommand = aM_.player.tickCount + 100;
            }
            return;
        }
        if (Delta.h().d().v().k().a()) return;
        handleFarmChest();
    }

    private void handleFarmChest() {
        if (aM_.gui.screen() instanceof ContainerScreen screen) {
            lootChest(screen);
            return;
        }
        BlockPos chest = findNearestChest();
        if (chest == null) {
            if (aM_.player.tickCount % 100 == 0) ChatUtil.a("Auto Warden: не найден доступный сундук.");
            return;
        }
        targetChest = chest;
        long remaining = Delta.h().d().t().i().a(chest);
        // Снижен порог кулдауна с 6000 мс до 4500 мс — подходим к сундуку
        // чуть раньше, чтобы к моменту открытия уже стоять рядом
        if (remaining > 4500L) {
            BlockPos orbit = findStandSpot(chest);
            if (orbit != null) pathTo(orbit);
            return;
        }
        if (aM_.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(chest)) > 20.0d) {
            BlockPos stand = findStandSpot(chest);
            if (stand != null) pathTo(stand);
            return;
        }
        interactChest(chest);
    }

    /**
     * Лут сундука — ускоренный (каждый тик вместо каждых 2).
     * Убрана проверка % 2 — одно QUICK_MOVE в тик это норма для SpookyTime AC.
     */
    private void lootChest(ContainerScreen screen) {
        if (isMoving()) {
            cancelPathing();
            return;
        }
        AbstractContainerMenu menu = screen.getMenu();
        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (slot.container != aM_.player.getInventory() && !stack.isEmpty()
                    && (ServerUtil.a.d() == potionSupplierAnarchy() && needsInvisibilityPotion()
                    ? isInvisibilityPotion(stack) && countMatching(this::isInvisibilityPotion) == 0
                    : shouldLoot(stack))) {
                aM_.gameMode.handleContainerInput(menu.containerId, slot.index, 0, ContainerInput.QUICK_MOVE, aM_.player);
                return;
            }
        }
        aM_.player.closeContainer();
        targetChest = null;
    }

    private void handleEscape() {
        if (isWardenAggro()) {
            moveAwayFromWardens();
            return;
        }
        if (hasLoot() && hasStorageDestination()) {
            state = State.FLEE;
            fleeGoal = null;
            return;
        }
        state = State.COLLECTING;
    }

    private void handleFlee() {
        if (aM_.gui.screen() instanceof ContainerScreen) {
            aM_.player.closeContainer();
            return;
        }
        if (hasNearbyPlayer()) {
            if (fleeGoal == null || aM_.player.blockPosition().distSqr(fleeGoal) <= 9.0d
                    || aM_.player.tickCount - lastPathAge > 200) {
                fleeGoal = chooseFleeGoal();
            }
            pathTo(fleeGoal);
            return;
        }
        cancelPathing();
        int storage = getStorageAnarchy();
        if (storage > 0 && storage != farmAnarchy && ServerUtil.a.d() != storage) {
            requestAnarchySwitch(storage);
            return;
        }
        state = State.STORAGE;
    }

    private void handleStorage() {
        int storage = getStorageAnarchy();
        if (storage > 0 && storage != farmAnarchy && ServerUtil.a.d() != storage) {
            requestAnarchySwitch(storage);
            return;
        }
        // Нет склада — просто сбрасываем состояние и продолжаем фарм
        if (storage <= 0 || storage == farmAnarchy) {
            finishStorageRun();
            return;
        }
        if (aM_.gui.screen() instanceof ContainerScreen screen) {
            // Если открыт сундук снабжения — берём из него (пополняем зелья),
            // а не складываем в него
            if (targetChest != null && isSupplyChest(targetChest)) {
                takeFromSupplyChest(screen);
            } else {
                storeLoot(screen);
            }
            return;
        }
        if (!hasLoot()) {
            finishStorageRun();
            return;
        }
        BlockPos receiver = findNearestReceiver();
        if (receiver == null) {
            if (aM_.player.tickCount % 100 == 0) ChatUtil.a("Auto Warden: рядом нет сундука склада.");
            finishStorageRun();
            return;
        }
        targetChest = receiver;
        if (aM_.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(receiver)) > 20.0d) {
            BlockPos stand = findStandSpot(receiver);
            if (stand != null) pathTo(stand);
            return;
        }
        interactChest(receiver);
    }

    /**
     * Берём из сундука снабжения только нужные вещи (зелья невидимости/скорости,
     * golden carrot). Всё остальное не трогаем.
     */
    private void takeFromSupplyChest(ContainerScreen screen) {
        AbstractContainerMenu menu = screen.getMenu();
        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (slot.container == aM_.player.getInventory()) continue;
            if (stack.isEmpty()) continue;
            boolean want = (isInvisibilityPotion(stack) && countMatching(this::isInvisibilityPotion) == 0)
                    || (useSpeed.c() && isSpeedPotion(stack) && findSlot(this::isSpeedPotion) < 0)
                    || (stack.is(Items.GOLDEN_CARROT) && InventoryUtil.a(Items.GOLDEN_CARROT) < 3);
            if (want) {
                aM_.gameMode.handleContainerInput(menu.containerId, slot.index, 0, ContainerInput.QUICK_MOVE, aM_.player);
                return;
            }
        }
        // Взяли всё нужное — закрываем и идём дальше
        aM_.player.closeContainer();
        targetChest = null;
        finishStorageRun();
    }

    /** Хранение лута — ускорено (убрана проверка % 2). */
    private void storeLoot(ContainerScreen screen) {
        // Защита: никогда не складывать вещи если мы на фарм-анархии или в фарм-зоне
        if (ServerUtil.a.d() == farmAnarchy && farmAnarchy >= 0) return;
        if (isInFarmArea()) return;
        AbstractContainerMenu menu = screen.getMenu();
        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (slot.container == aM_.player.getInventory() && !stack.isEmpty() && !shouldKeep(stack)) {
                aM_.gameMode.handleContainerInput(menu.containerId, slot.index, 0, ContainerInput.QUICK_MOVE, aM_.player);
                return;
            }
        }
        aM_.player.closeContainer();
        finishStorageRun();
    }

    private void finishStorageRun() {
        cancelPathing();
        targetChest   = null;
        chestOpenCounts.clear();
        totalChestsOpened = 0;
        seventhChestTick  = -1;
        // Сбрасываем кэш receiver чтобы при следующем STORAGE-цикле
        // не использовать устаревший (возможно варден-зонный) сундук
        receiverChest   = null;
        receiverScanAge = Integer.MIN_VALUE;
        state = State.SAVE;
        if (farmAnarchy >= 0 && ServerUtil.a.d() != farmAnarchy) requestAnarchySwitch(farmAnarchy);
    }

    // ─── Взаимодействие с сундуком (SpookyTime байпас) ───────────────────────
    /**
     * Плавный поворот к сундуку — убираем мгновенный snap 120f.
     *
     * Если smoothRotation включён: используем экспоненциальный smooth +
     * smoothstep easing (та же схема что SpookyTimeRotation).
     * Это убирает детектируемый паттерн "мгновенного наведения на блок"
     * который SpookyTime [AC+] умеет ловить.
     *
     * Условие открытия сундука ужесточено: угол должен быть < 3° (было 5°)
     * и срабатывает каждые 3 тика вместо каждых 4 — чуть быстрее.
     */
    private void interactChest(BlockPos chest) {
        Vec3 eye   = aM_.player.getEyePosition();
        Vec3 point = visiblePoint(eye, chest);
        if (point == null) return;
        Rotation target = Rotation.a(eye, point);

        if (smoothRotation.c()) {
            // Инициализация при первом вызове к новому сундуку
            if (smoothRotInit) {
                smoothRotYaw   = aM_.player.getYRot();
                smoothRotPitch = aM_.player.getXRot();
                smoothSpeedY   = 3.0f;
                smoothSpeedP   = 2.5f;
                smoothRotInit  = false;
            }

            float tYaw   = target.c();   // целевой yaw
            float tPitch = target.d();   // целевой pitch

            float yawDelta   = Mth.wrapDegrees(tYaw   - smoothRotYaw);
            float pitchDelta = tPitch - smoothRotPitch;

            // Адаптивная скорость с exponential smoothing (SPAngle-схема)
            float targetSpeedY = Mth.clamp(
                MathUtil.a(7.0f, 10.5f) * smoothstep(Math.abs(yawDelta)   / 40.0f), 1.5f, 11.0f);
            float targetSpeedP = Mth.clamp(
                MathUtil.a(5.5f, 8.5f)  * smoothstep(Math.abs(pitchDelta) / 30.0f), 1.0f, 9.0f);

            float alpha = 0.18f;
            smoothSpeedY += (targetSpeedY - smoothSpeedY) * alpha;
            smoothSpeedP += (targetSpeedP - smoothSpeedP) * alpha;

            float yawScale   = ease(Mth.clamp(smoothSpeedY / Math.max(Math.abs(yawDelta),   0.001f), 0.0f, 1.0f));
            float pitchScale = ease(Mth.clamp(smoothSpeedP / Math.max(Math.abs(pitchDelta), 0.001f), 0.0f, 1.0f));

            smoothRotYaw   += yawDelta   * yawScale;
            smoothRotPitch  = Mth.clamp(smoothRotPitch + pitchDelta * pitchScale, -89.0f, 89.0f);

            Delta.h().d().k().a(new Rotation(smoothRotYaw, smoothRotPitch), 220.0f, 1, 1);

            // Открываем когда угол между текущим взглядом и целью < 3°
            Rotation current = new Rotation(aM_.player);
            if (current.a(target) > 3.0d || aM_.player.tickCount % 3 != 0) return;
        } else {
            // Старое поведение — прямой snap
            Delta.h().d().k().a(target, 120.0f, 1, 1);
            if (new Rotation(aM_.player).a(target) > 5.0d || aM_.player.tickCount % 4 != 0) return;
        }

        BlockHitResult hit = aM_.level.clip(new ClipContext(eye, point,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, aM_.player));
        if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(chest)) {
            aM_.gameMode.useItemOn(aM_.player, InteractionHand.MAIN_HAND, hit);
            aM_.player.swing(InteractionHand.MAIN_HAND);
            boolean wasNew = !chestOpenCounts.containsKey(chest);
            chestOpenCounts.merge(chest, 1, Integer::sum);
            // Считаем уникальные сундуки (первое открытие каждого)
            if (wasNew) {
                totalChestsOpened++;
                if (debug.c()) ChatUtil.a("Auto Warden: открыто сундуков: " + totalChestsOpened + "/" + CHESTS_BEFORE_STORAGE);
                if (totalChestsOpened >= CHESTS_BEFORE_STORAGE && seventhChestTick < 0) {
                    seventhChestTick = aM_.player.tickCount;
                    ChatUtil.a("Auto Warden: открыт " + CHESTS_BEFORE_STORAGE + "-й сундук, уход на склад через " + STORAGE_WAIT_TICKS / 20 + " сек.");
                }
            }
            // После открытия — сбрасываем smooth-состояние для следующего сундука
            smoothRotInit = true;
        }
    }

    // ─── Escape логика ────────────────────────────────────────────────────────

    private boolean shouldEscape() {
        if (aM_.player.tickCount < 100) return false;
        return isWardenAggro()
                || (inventoryCount() >= priorityMultiplier(20) && hasStorageDestination())
                || aM_.player.getFoodData().getFoodLevel() < 8;
    }

    private boolean hasStorageDestination() {
        return (getStorageAnarchy() > 0 && getStorageAnarchy() != farmAnarchy) || findNearestReceiver() != null;
    }

    private void moveAwayFromWardens() {
        BlockPos goal = chooseEscapeGoal();
        // Быстрый re-path: не ждём дедупликацию — каждый тик обновляем цель
        // пока активна агрессия, чтобы сразу уйти от нового вардена
        forcePathTo(goal);
        if (aM_.player.tickCount >= roarUntil) state = State.COLLECTING;
    }

    private BlockPos chooseEscapeGoal() {
        return chooseGoalAwayFrom(wardens, 25);
    }

    private BlockPos chooseFleeGoal() {
        List<BlockPos> players = new ArrayList<>();
        for (Entity entity : nearbyPlayers()) players.add(entity.blockPosition());
        return chooseGoalAwayFrom(players, 30);
    }

    private BlockPos chooseGoalAwayFrom(List<BlockPos> threats, int distance) {
        BlockPos origin = aM_.player.blockPosition();
        BlockPos best   = origin;
        double bestDistance = -1.0d;
        for (int angle = 0; angle < 360; angle += 30) {
            int x = clampX((int)(aM_.player.getX() + Math.cos(Math.toRadians(angle)) * distance));
            int z = clampZ((int)(aM_.player.getZ() + Math.sin(Math.toRadians(angle)) * distance));
            double nearest = Double.MAX_VALUE;
            for (BlockPos threat : threats) {
                nearest = Math.min(nearest, Math.hypot(threat.getX() - x, threat.getZ() - z));
            }
            if (nearest > bestDistance) {
                bestDistance = nearest;
                best = new BlockPos(x, origin.getY(), z);
            }
        }
        return best;
    }

    private boolean isWardenAggro() {
        return aM_.player.tickCount < roarUntil && !wardens.isEmpty();
    }

    private void updateWardens() {
        wardens.clear();
        AABB range = aM_.player.getBoundingBox().inflate(30.0d);
        for (Entity entity : aM_.level.getEntities(aM_.player, range, e -> e instanceof Warden)) {
            wardens.add(entity.blockPosition());
        }
    }

    private List<Entity> nearbyPlayers() {
        return aM_.level.getEntities(aM_.player, aM_.player.getBoundingBox().inflate(30.0d),
                entity -> entity instanceof Player && entity != aM_.player);
    }

    private boolean hasNearbyPlayer() { return !nearbyPlayers().isEmpty(); }

    private boolean needsInvisibilityPotion() {
        if (aM_.player.hasEffect(MobEffects.GLOWING)) return false;
        MobEffectInstance invisibility = aM_.player.getEffect(MobEffects.INVISIBILITY);
        return invisibility == null || invisibility.getDuration() < 400;
    }

    private int findSlot(java.util.function.Predicate<ItemStack> predicate) {
        for (int slot = 0; slot < 36; slot++) {
            if (predicate.test(aM_.player.getInventory().getItem(slot))) return slot;
        }
        return -1;
    }

    private boolean isInvisibilityPotion(ItemStack stack) {
        if (!stack.has(DataComponents.POTION_CONTENTS)) return false;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (effect.getEffect().is(MobEffects.INVISIBILITY)) return true;
        }
        return false;
    }

    private boolean isSpeedPotion(ItemStack stack) {
        if (!stack.has(DataComponents.POTION_CONTENTS)) return false;
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        for (MobEffectInstance effect : contents.getAllEffects()) {
            if (effect.getEffect().is(MobEffects.SPEED)) return true;
        }
        return false;
    }

    private boolean shouldKeep(ItemStack stack) {
        return isInvisibilityPotion(stack) || stack.is(Items.GOLDEN_CARROT)
                || (useSpeed.c() && isSpeedPotion(stack) && findSlot(this::isSpeedPotion) >= 0);
    }

    private boolean shouldLoot(ItemStack stack) {
        if (isJunk(stack)) return false;
        if (isInvisibilityPotion(stack) && countMatching(this::isInvisibilityPotion) > 0) return false;
        if (stack.is(Items.GOLDEN_CARROT) && InventoryUtil.a(Items.GOLDEN_CARROT) >= 3) return false;
        return !useSpeed.c() || !isSpeedPotion(stack) || findSlot(this::isSpeedPotion) < 0;
    }

    private int countMatching(java.util.function.Predicate<ItemStack> predicate) {
        int count = 0;
        for (int slot = 0; slot < 36; slot++) {
            if (predicate.test(aM_.player.getInventory().getItem(slot))) count++;
        }
        return count;
    }

    private boolean hasLoot() {
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = aM_.player.getInventory().getItem(slot);
            if (!stack.isEmpty() && !shouldKeep(stack)) return true;
        }
        return false;
    }

    private int inventoryCount() {
        int count = 0;
        for (int slot = 0; slot < 36; slot++) {
            if (!aM_.player.getInventory().getItem(slot).isEmpty()) count++;
        }
        return count;
    }

    private boolean isJunk(ItemStack stack) {
        if (lootPriority.l("Низкий")) return false;
        boolean mediumJunk = stack.is(Items.ARROW) || stack.is(Items.CHORUS_FRUIT)
                || stack.is(Items.DISC_FRAGMENT_5) || stack.is(Items.NAUTILUS_SHELL)
                || stack.is(Items.COOKED_MUTTON)   || stack.is(Items.LEATHER)
                || stack.is(Items.EMERALD)          || stack.is(Items.SUGAR)
                || stack.is(Items.DIAMOND_HELMET)   || stack.is(Items.DIAMOND_CHESTPLATE)
                || stack.is(Items.DIAMOND_LEGGINGS) || stack.is(Items.DIAMOND_BOOTS);
        if (mediumJunk) return true;
        return lootPriority.l("Высокий") && (
                stack.is(Items.BLAZE_ROD)       || stack.is(Items.ENCHANTED_BOOK)
             || stack.is(Items.TRIDENT)          || stack.is(Items.NAME_TAG)
             || stack.is(Items.SCULK)            || stack.is(Items.ENDER_CHEST)
             || stack.is(Items.PUFFERFISH)       || stack.is(Items.ANVIL));
    }

    private int priorityMultiplier(int base) {
        if (lootPriority.l("Низкий"))  return (int)(base * 1.5d);
        if (lootPriority.l("Высокий")) return (int)(base * 0.8d);
        return base;
    }

    private BlockPos findNearestChest() {
        WardenESP esp = Delta.h().d().t().i();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos chest : esp.q()) {
            long remaining = esp.a(chest);
            if (remaining > 5000L || chestOpenCounts.getOrDefault(chest, 0) >= 3
                    || findStandSpot(chest) == null) continue;
            double distance = aM_.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(chest));
            if (distance < bestDistance) {
                bestDistance = distance;
                best = chest;
            }
        }
        return best;
    }

    private BlockPos findNearestReceiver() {
        if (receiverChest != null && aM_.player.tickCount - receiverScanAge < 40
                && receiverChest.distSqr(aM_.player.blockPosition()) <= 1024.0d
                && aM_.level.getBlockState(receiverChest).is(Blocks.CHEST)) {
            return receiverChest;
        }
        if (receiverScanAge != Integer.MIN_VALUE && aM_.player.tickCount - receiverScanAge < 40) return null;

        BlockPos origin = aM_.player.blockPosition();
        BlockPos bestHopper = null;
        BlockPos bestPlain  = null;
        double bestHopperDistance = Double.MAX_VALUE;
        double bestPlainDistance  = Double.MAX_VALUE;
        for (int dx = -16; dx <= 16; dx++) {
            for (int dy = -6; dy <= 6; dy++) {
                for (int dz = -16; dz <= 16; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (!aM_.level.getBlockState(pos).is(Blocks.CHEST)
                            || !aM_.level.getBlockState(pos.above()).isAir()) continue;
                    // Никогда не использовать сундуки из варден-зоны как склад —
                    // именно отсюда бот берёт лут, а не складывает туда
                    if (isInFarmPos(pos)) continue;
                    // Если сундук уже известен как варден-сундук (WardenESP его отслеживает) — пропустить
                    if (Delta.h().d().t().i().q().contains(pos)) continue;
                    double distance = aM_.player.distanceToSqr(Vec3.atCenterOf(pos));
                    if (isHopperChest(pos)) {
                        if (distance < bestHopperDistance) {
                            bestHopperDistance = distance;
                            bestHopper = pos;
                        }
                    } else if (!hasChestSign(pos) && distance < bestPlainDistance) {
                        // !hasChestSign = нет таблички ИЛИ табличка "снабжение"
                        bestPlainDistance = distance;
                        bestPlain = pos;
                    }
                }
            }
        }
        receiverChest = bestHopper != null ? bestHopper : bestPlain;
        receiverScanAge = aM_.player.tickCount;
        return receiverChest;
    }

    private boolean isHopperChest(BlockPos pos) {
        if (aM_.level.getBlockState(pos.below()).is(Blocks.HOPPER)) return true;
        BlockState state = aM_.level.getBlockState(pos);
        for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos partner    = pos.relative(direction);
            BlockState partnerState = aM_.level.getBlockState(partner);
            if (partnerState.is(Blocks.CHEST) && aM_.level.getBlockState(partner.below()).is(Blocks.HOPPER)
                    && partnerState.getValue(net.minecraft.world.level.block.ChestBlock.TYPE)
                        != net.minecraft.world.level.block.state.properties.ChestType.SINGLE
                    && state.getValue(net.minecraft.world.level.block.ChestBlock.TYPE)
                        != net.minecraft.world.level.block.state.properties.ChestType.SINGLE
                    && partnerState.getValue(net.minecraft.world.level.block.ChestBlock.TYPE)
                        != state.getValue(net.minecraft.world.level.block.ChestBlock.TYPE)
                    && partnerState.getValue(net.minecraft.world.level.block.ChestBlock.FACING)
                        == state.getValue(net.minecraft.world.level.block.ChestBlock.FACING)) return true;
        }
        return false;
    }

    /**
     * Возвращает текст со всех строк ближайшей таблички рядом с сундуком
     * (нижний регистр, пробелы убраны). null если таблички нет.
     */
    private String getAdjacentSignText(BlockPos pos) {
        for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos signPos = pos.relative(direction);
            BlockState signState = aM_.level.getBlockState(signPos);
            String blockPath = BuiltInRegistries.BLOCK.getKey(signState.getBlock()).getPath();
            if (!blockPath.endsWith("_sign") && !blockPath.endsWith("_wall_sign")
                    && !blockPath.endsWith("_hanging_sign") && !blockPath.endsWith("_wall_hanging_sign")) continue;
            if (!(aM_.level.getBlockEntity(signPos) instanceof SignBlockEntity sign)) continue;
            StringBuilder sb = new StringBuilder();
            for (int side = 0; side < 2; side++) {
                for (int line = 0; line < 4; line++) {
                    net.minecraft.network.chat.Component comp = sign.getText(side == 0).getMessage(line, false);
                    sb.append(comp.getString());
                }
            }
            return sb.toString().toLowerCase().replaceAll("\\s+", "");
        }
        return null;
    }

    /** true если рядом с сундуком есть табличка с текстом не "снабжение" — такие сундуки не используем как склад */
    private boolean hasChestSign(BlockPos pos) {
        String text = getAdjacentSignText(pos);
        // null = нет таблички = обычный сундук (ок)
        // текст содержит "снабжение" = сундук снабжения (разрешаем как склад)
        // другой текст = чужой/заблокированный сундук (пропускаем)
        return text != null && !text.contains("снабжение");
    }

    /** true если рядом с сундуком табличка "снабжение" */
    private boolean isSupplyChest(BlockPos pos) {
        String text = getAdjacentSignText(pos);
        return text != null && text.contains("снабжение");
    }

    private BlockPos findStandSpot(BlockPos chest) {
        for (Direction direction : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos side = chest.relative(direction);
            if (aM_.level.getBlockState(side).isAir() && aM_.level.getBlockState(side.above()).isAir()
                    && !aM_.level.getBlockState(side.below()).isAir() && canSeeChest(side, chest)) {
                return side;
            }
        }
        BlockPos above = chest.above();
        if (aM_.level.getBlockState(above).isAir() && aM_.level.getBlockState(above.above()).isAir()
                && canSeeChest(above, chest)) return above;
        return null;
    }

    private boolean canSeeChest(BlockPos stand, BlockPos chest) {
        Vec3 eye = Vec3.atCenterOf(stand).add(0.0d, aM_.player.getEyeHeight() - 0.5d, 0.0d);
        return visiblePoint(eye, chest) != null;
    }

    private Vec3 visiblePoint(Vec3 eye, BlockPos chest) {
        Vec3 center = Vec3.atCenterOf(chest);
        for (double dx : new double[]{0.0d, -0.4d, 0.4d}) {
            for (double dy : new double[]{0.0d, -0.4d, 0.4d}) {
                for (double dz : new double[]{0.0d, -0.4d, 0.4d}) {
                    Vec3 point = center.add(dx, dy, dz);
                    if (aM_.level.clip(new ClipContext(eye, point, ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE, aM_.player)).getBlockPos().equals(chest)) return point;
                }
            }
        }
        return null;
    }

    private boolean isInFarmArea() {
        return aM_.level.dimension().identifier().toString().equals("minecraft:overworld")
                && aM_.player.getX() <= -1921.0d && aM_.player.getX() >= -2070.0d
                && aM_.player.getZ() <= -1929.0d && aM_.player.getZ() >= -2076.0d;
    }

    /** Проверяет что конкретный BlockPos находится в варден-зоне фарма */
    private boolean isInFarmPos(BlockPos pos) {
        return aM_.level.dimension().identifier().toString().equals("minecraft:overworld")
                && pos.getX() <= -1921 && pos.getX() >= -2070
                && pos.getZ() <= -1929 && pos.getZ() >= -2076;
    }

    private int clampX(int x) { return Math.max(-2060, Math.min(-1931, x)); }
    private int clampZ(int z) { return Math.max(-2066, Math.min(-1939, z)); }

    private int getStorageAnarchy() {
        try {
            String val = storageAnarchy.c().trim();
            if (val.isEmpty()) return 0;
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int potionSupplierAnarchy() {
        for (int anarchy : anarchyList) {
            if (anarchy != farmAnarchy && anarchy != getStorageAnarchy()) return anarchy;
        }
        return -1;
    }

    private void requestAnarchySwitch(int anarchy) {
        if (aM_.player.connection == null || aM_.player.tickCount <= 5
                || (lastAnarchyCommand != Integer.MIN_VALUE
                    && aM_.player.tickCount - lastAnarchyCommand < 40)) return;
        aM_.player.connection.sendCommand("an" + anarchy);
        lastAnarchyCommand = aM_.player.tickCount;
        receiverChest  = null;
        receiverScanAge = Integer.MIN_VALUE;
    }

    /**
     * Обычный pathTo с дедупликацией (10 тиков вместо 20 — быстрее реагирует
     * на смену цели при обычном движении).
     */
    private void pathTo(BlockPos goal) {
        if (goal == null) return;
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        if (goal.equals(pathTarget) && (baritone.getPathingBehavior().hasPath()
                || aM_.player.blockPosition().distSqr(goal) <= 2.25d
                || aM_.player.tickCount - lastPathAge < 10)) return;
        if (aM_.player.blockPosition().distSqr(goal) <= 2.25d) return;
        baritone.getCustomGoalProcess().setGoalAndPath(new GoalBlock(goal));
        pathTarget  = goal.immutable();
        lastPathAge = aM_.player.tickCount;
    }

    /**
     * forcePathTo — без дедупликации. Используется при escape от варденов
     * чтобы мгновенно сменить направление без ожидания cooldown.
     */
    private void forcePathTo(BlockPos goal) {
        if (goal == null) return;
        if (aM_.player.blockPosition().distSqr(goal) <= 2.25d) return;
        IBaritone baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        baritone.getCustomGoalProcess().setGoalAndPath(new GoalBlock(goal));
        pathTarget  = goal.immutable();
        lastPathAge = aM_.player.tickCount;
    }

    private void cancelPathing() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        pathTarget = null;
    }

    private boolean isMoving() {
        return aM_.player.getDeltaMovement().horizontalDistanceSqr() > 0.0025d;
    }

    private void clearTargets() {
        pathTarget    = null;
        targetChest   = null;
        receiverChest = null;
        fleeGoal      = null;
        wardens.clear();
    }

    // ─── Утилиты для ротации (копия из SpookyTimeRotation) ───────────────────

    /** Easing: f(t) = t * (0.5 + 0.5*t) — из спеки SPAngle */
    private static float ease(float t) {
        return t * (0.5f + 0.5f * t);
    }

    /** Smoothstep [0..1]: плавный S-образный переход */
    private static float smoothstep(float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    // ─── State ────────────────────────────────────────────────────────────────

    private enum State {
        SAVE,
        TAKE,
        COLLECTING,
        ESCAPE,
        FLEE,
        STORAGE
    }
}
