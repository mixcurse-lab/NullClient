package platform.client.features.modules.misc;

import static platform.api.module.Interface.aM_;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import platform.api.event.events.client.TickEvent;
import platform.api.event.events.player.ClickEvent;
import platform.api.event.events.player.KeyEvent;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.module.setting.BindSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;
import platform.client.Delta;
import platform.client.utils.rotation.Rotation;

@ModuleRegister(a = "Auto Cart", b = "Ставит рельсы и вагонетку с ТНТ в место падения стрелы и поджигает её выстрелом из лука", c = Category.Misc)
public class AutoCart extends Module {

    private static final int MAX_PREDICTION_TICKS = 80;
    private static final double ARROW_GRAVITY = 0.05d;
    private static final int MAX_PLACE_ATTEMPTS = 20;

    private final SliderSetting range = new SliderSetting("Дальность", 5.0f, 1.0f, 5.0f, 0.1f);
    private final SliderSetting chargeTicks = new SliderSetting("Тиков натяжки", 10.0f, 3.0f, 20.0f, 1.0f);
    private final ModeSetting typeRail = new ModeSetting("Рельса", "Обычная", "Обычная", "Энерго");
    private final BindSetting bowShootKey = new BindSetting("Клавиша выстрела", -1);

    private final Set<Integer> knownArrows = new HashSet<>();
    private final Set<Integer> processedArrows = new HashSet<>();

    // 0 - рельса, 1 - вагонетка с ТНТ, 2 - поджог выстрелом из лука
    private final Map<Integer, Integer> placeStages = new HashMap<>();
    private final Map<Integer, Integer> placeAttempts = new HashMap<>();
    private final Map<Integer, BlockPos> landingCache = new HashMap<>();

    private boolean bowShotQueued;
    private boolean bowCharging;
    private int bowChargeTime;
    private int bowPrevSlot = -1;
    private int igniteCartId = -1;

    public AutoCart() {
        a(this.range, this.chargeTicks, this.typeRail, this.bowShootKey);
    }

    @Override
    public void b() {
        super.b();
        knownArrows.clear();
        processedArrows.clear();
        placeStages.clear();
        placeAttempts.clear();
        landingCache.clear();
        resetBowState();
        cacheExistingArrows();
    }

    @Override
    public void c() {
        knownArrows.clear();
        processedArrows.clear();
        placeStages.clear();
        placeAttempts.clear();
        landingCache.clear();
        if (aM_.player != null && aM_.gameMode != null && aM_.player.isUsingItem()) {
            aM_.gameMode.releaseUsingItem(aM_.player);
            aM_.player.releaseUsingItem();
        }
        resetBowState();
        super.c();
    }

    @EventTarget
    public void a(KeyEvent event) {
        if (aM_.player == null || aM_.level == null || aM_.gameMode == null) return;
        if (event.d() != 1) return;
        if (aM_.gui.screen() != null) return;
        int key = bowShootKey.c().intValue();
        if (key == -1 || event.b() != key) return;

        bowShotQueued = true;
    }

    @EventTarget
    public void a(ClickEvent event) {
        if (aM_.player == null || aM_.level == null || aM_.gameMode == null) return;
        if (!event.b()) return;
        if (aM_.gui.screen() != null) return;
        int key = bowShootKey.c().intValue();
        if (key == -1 || event.h() != key) return;

        bowShotQueued = true;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (aM_.player == null || aM_.level == null || aM_.gameMode == null) return;
        aimIfIgniting();
        handleBowShoot();

        Set<Integer> aliveArrows = new HashSet<>();

        for (Entity entity : aM_.level.entitiesForRendering()) {
            if (!(entity instanceof AbstractArrow projectile) || projectile instanceof ThrownTrident) {
                continue;
            }
            if (projectile.getOwner() != aM_.player) continue;

            int id = projectile.getId();
            aliveArrows.add(id);
            knownArrows.add(id);

            if (processedArrows.contains(id)) continue;
            if (!hasRequiredItemsInHotbar()) continue;

            // точку установки считаем один раз; после посадки стрелы продолжаем
            // достраивать по кэшу, а если предсказать не удалось — строим прямо
            // у упавшей стрелы, чтобы точно успеть
            BlockPos placePos = landingCache.get(id);
            if (placePos == null) {
                BlockPos predicted;
                if (shouldProcessArrow(projectile)) {
                    predicted = predictLanding(projectile);
                } else {
                    predicted = resolvePlacePosAt(BlockPos.containing(projectile.position()));
                }
                if (predicted == null) continue;
                double rangeSq = range.c().floatValue() * range.c().floatValue();
                if (aM_.player.distanceToSqr(Vec3.atCenterOf(predicted)) > rangeSq) {
                    processedArrows.add(id);
                    continue;
                }
                placePos = predicted;
                landingCache.put(id, placePos);
            }

            processStages(id, placePos);
        }

        // выстрел ставится в очередь внутри цикла выше — стартуем его в тот же тик
        handleBowShoot();

        knownArrows.retainAll(aliveArrows);
        processedArrows.retainAll(aliveArrows);
        placeStages.keySet().retainAll(aliveArrows);
        placeAttempts.keySet().retainAll(aliveArrows);
        landingCache.keySet().retainAll(aliveArrows);
    }


    // стадии гоняются подряд без пауз: рельса и вагонетка ставятся моментально,
    // неудачные попытки просто повторяются на следующем тике до лимита
    private void processStages(int id, BlockPos placePos) {
        for (int guard = 0; guard < 3; guard++) {
            int stage = placeStages.getOrDefault(id, 0);
            switch (stage) {
                case 0 -> {
                    Item railItem = typeRail.l("Энерго") ? Items.POWERED_RAIL : Items.RAIL;
                    if (isRail(placePos) || placeRail(placePos, railItem)) {
                        placeStages.put(id, 1);
                        continue;
                    }
                    failAttempt(id);
                }
                case 1 -> {
                    if (hasTntMinecart(placePos)) {
                        placeStages.put(id, 2);
                        continue;
                    }
                    int cartSlot = findItemInHotbar(Items.TNT_MINECART);
                    if (cartSlot == -1) {
                        finishArrow(id);
                    } else {
                        placeTntMinecart(placePos, cartSlot);
                        restockMinecartSlot(cartSlot);
                        placeStages.put(id, 2);
                        continue;
                    }
                }
                default -> {
                    // вагонетка стоит — целимся в неё и автоматически стреляем:
                    // горящая стрела поджигает заряд
                    MinecartTNT cart = findTntMinecartEntity(placePos);
                    if (cart == null) {
                        finishArrow(id);
                    } else {
                        this.igniteCartId = cart.getId();
                        if (!bowCharging && !bowShotQueued && findItemInHotbar(Items.BOW) != -1) {
                            bowShotQueued = true;
                        }
                        finishArrow(id);
                    }
                }
            }
            break;
        }
    }

    private void failAttempt(int id) {
        int attempts = placeAttempts.merge(id, 1, Integer::sum);
        if (attempts >= MAX_PLACE_ATTEMPTS) {
            finishArrow(id);
        }
    }

    private void finishArrow(int id) {
        processedArrows.add(id);
        placeStages.remove(id);
        placeAttempts.remove(id);
        landingCache.remove(id);
    }


    private void aimIfIgniting() {
        if (this.igniteCartId == -1 || aM_.level == null || aM_.player == null) return;
        Entity cart = aM_.level.getEntity(this.igniteCartId);
        if (cart == null || !cart.isAlive()) {
            this.igniteCartId = -1;
            return;
        }
        Rotation r = Rotation.a(aM_.player.getEyePosition(), cart.getBoundingBox().getCenter());
        Delta.h().d().k().a(r, 220.0f, 1, 1);
    }


    private void handleBowShoot() {
        if (aM_.player == null || aM_.gameMode == null) return;

        if (bowCharging) {
            if (!aM_.player.getMainHandItem().is(Items.BOW)) {
                finishBowShot(false);
                return;
            }

            bowChargeTime++;
            if (bowChargeTime >= this.chargeTicks.c().intValue()) {
                finishBowShot(true);
            }
            return;
        }

        if (!bowShotQueued) return;
        bowShotQueued = false;

        int bowSlot = findItemInHotbar(Items.BOW);
        if (bowSlot == -1) return;

        bowPrevSlot = aM_.player.getInventory().getSelectedSlot();
        selectSlot(bowSlot);

        // выстрел чистыми пакетами: ваниль сама отменяет натяжку, когда ПКМ
        // отпущен, поэтому клиентское состояние использования не трогаем —
        // сервер засчитывает заряд по времени между USE_ITEM и RELEASE
        aM_.player.connection.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, aM_.player.getYRot(), aM_.player.getXRot()));
        bowCharging = true;
        bowChargeTime = 0;
    }


    private void finishBowShot(boolean releaseShot) {
        if (aM_.player == null || aM_.gameMode == null) {
            resetBowState();
            return;
        }

        if (releaseShot) {
            aM_.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
        }

        if (bowPrevSlot >= 0 && bowPrevSlot <= 8 && aM_.player.getInventory().getSelectedSlot() != bowPrevSlot) {
            selectSlot(bowPrevSlot);
        }

        resetBowState();
    }


    private void resetBowState() {
        bowShotQueued = false;
        bowCharging = false;
        bowChargeTime = 0;
        bowPrevSlot = -1;
        igniteCartId = -1;
    }


    private void cacheExistingArrows() {
        if (aM_.player == null || aM_.level == null) return;
        for (Entity entity : aM_.level.entitiesForRendering()) {
            if (!(entity instanceof AbstractArrow projectile) || projectile instanceof ThrownTrident) {
                continue;
            }
            if (projectile.getOwner() == aM_.player) {
                knownArrows.add(projectile.getId());
            }
        }
    }


    private void selectSlot(int slot) {
        aM_.player.getInventory().setSelectedSlot(slot);
        aM_.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
    }


    private boolean hasRequiredItemsInHotbar() {
        Item railItem = typeRail.l("Энерго") ? Items.POWERED_RAIL : Items.RAIL;
        return findItemInHotbar(railItem) != -1
                && findItemInHotbar(Items.TNT_MINECART) != -1;
    }


    private boolean shouldProcessArrow(AbstractArrow projectile) {
        return !projectile.onGround() && projectile.getDeltaMovement().lengthSqr() > 1.0e-4d;
    }


    private BlockPos predictLanding(AbstractArrow arrow) {
        if (aM_.level == null) return null;

        Vec3 pos = arrow.position();
        Vec3 motion = arrow.getDeltaMovement();

        for (int tick = 1; tick <= MAX_PREDICTION_TICKS; tick++) {
            Vec3 prevPos = pos;
            pos = pos.add(motion);
            motion = updateMotion(arrow, prevPos, motion);

            BlockHitResult hit = aM_.level.clip(new ClipContext(
                    prevPos, pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, arrow));
            if (hit.getType() != HitResult.Type.MISS) {
                return resolvePlacePos(hit);
            }
        }

        return null;
    }


    private Vec3 updateMotion(AbstractArrow arrow, Vec3 pos, Vec3 motion) {
        if (aM_.level == null) return motion;
        boolean inWater = aM_.level.getBlockState(BlockPos.containing(pos)).getFluidState().is(FluidTags.WATER);
        double drag = inWater ? 0.6d : 0.99d;
        return motion.scale(drag).add(0.0d, -ARROW_GRAVITY, 0.0d);
    }


    private BlockPos resolvePlacePos(BlockHitResult hit) {
        if (aM_.level == null) return null;

        BlockPos basePos = hit.getBlockPos();
        if (isRail(basePos)) return basePos;

        BlockPos preferred = hit.getDirection() == Direction.UP ? basePos.above() : basePos;
        if (canPlaceRailAt(preferred)) return preferred;

        BlockPos up = basePos.above();
        if (canPlaceRailAt(up)) return up;

        BlockPos down = basePos.below();
        if (canPlaceRailAt(down)) return down;

        return null;
    }


    private BlockPos resolvePlacePosAt(BlockPos basePos) {
        if (aM_.level == null) return null;

        if (isRail(basePos)) return basePos;
        if (canPlaceRailAt(basePos)) return basePos;

        BlockPos up = basePos.above();
        if (canPlaceRailAt(up)) return up;

        return null;
    }


    private boolean isRail(BlockPos pos) {
        return aM_.level != null && aM_.level.getBlockState(pos).is(BlockTags.RAILS);
    }


    private boolean canPlaceRailAt(BlockPos pos) {
        if (aM_.level == null) return false;

        if (isRail(pos)) return true;
        if (!aM_.level.getBlockState(pos).canBeReplaced()) return false;

        BlockPos support = pos.below();
        return !aM_.level.getBlockState(support).getCollisionShape(aM_.level, support).isEmpty();
    }


    private boolean placeRail(BlockPos placePos, Item railItem) {
        if (aM_.player == null || aM_.gameMode == null) return false;

        int railSlot = findItemInHotbar(railItem);
        if (railSlot == -1) return false;

        selectSlot(railSlot);

        BlockPos support = placePos.below();
        BlockHitResult hitResult = new BlockHitResult(
                new Vec3(placePos.getX() + 0.5d, placePos.getY(), placePos.getZ() + 0.5d),
                Direction.UP,
                support,
                false
        );

        InteractionResult result = aM_.gameMode.useItemOn(aM_.player, InteractionHand.MAIN_HAND, hitResult);
        if (result.consumesAction()) {
            aM_.player.swing(InteractionHand.MAIN_HAND);
            return true;
        }

        return isRail(placePos);
    }


    private void placeTntMinecart(BlockPos railPos, int cartSlot) {
        if (aM_.player == null || aM_.gameMode == null) return;

        selectSlot(cartSlot);

        BlockHitResult hitResult = new BlockHitResult(
                new Vec3(railPos.getX() + 0.5d, railPos.getY() + 0.15d, railPos.getZ() + 0.5d),
                Direction.UP,
                railPos,
                false
        );

        InteractionResult result = aM_.gameMode.useItemOn(aM_.player, InteractionHand.MAIN_HAND, hitResult);
        if (result.consumesAction()) {
            aM_.player.swing(InteractionHand.MAIN_HAND);
        }
    }


    private void restockMinecartSlot(int hotbarSlot) {
        if (aM_.player == null || aM_.gameMode == null) return;
        if (hotbarSlot < 0 || hotbarSlot > 8) return;
        if (aM_.player.getInventory().getItem(hotbarSlot).is(Items.TNT_MINECART)) return;

        int invSlot = -1;
        for (int i = 9; i < 36; i++) {
            if (aM_.player.getInventory().getItem(i).is(Items.TNT_MINECART)) {
                invSlot = i;
                break;
            }
        }
        if (invSlot == -1) return;

        aM_.gameMode.handleContainerInput(aM_.player.inventoryMenu.containerId, invSlot, hotbarSlot, ContainerInput.SWAP, aM_.player);
    }


    private MinecartTNT findTntMinecartEntity(BlockPos railPos) {
        if (aM_.level == null) return null;

        AABB checkBox = new AABB(railPos).inflate(0.2d, 1.0d, 0.2d);
        for (Entity entity : aM_.level.getEntities((Entity) null, checkBox)) {
            if (entity instanceof MinecartTNT cart) {
                return cart;
            }
        }
        return null;
    }


    private boolean hasTntMinecart(BlockPos railPos) {
        return findTntMinecartEntity(railPos) != null;
    }


    private int findItemInHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            if (aM_.player.getInventory().getItem(i).is(item)) {
                return i;
            }
        }
        return -1;
    }
}
