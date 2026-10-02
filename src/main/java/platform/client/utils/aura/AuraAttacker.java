package platform.client.utils.aura;

import static platform.api.module.Interface.aM_;

import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import platform.client.Delta;
import platform.client.features.modules.combat.MaceUtil;
import platform.client.ui.screen.GUIScreen;
import platform.client.features.modules.combat.AuraUtil;
import platform.inject.accessors.LocalPlayerAccessor;
import platform.client.utils.math.MathUtil;
import platform.client.utils.player.InventoryUtil;


public class AuraAttacker {

    private final AuraProcessor aura;

    public AuraAttacker(AuraProcessor aura) {
        this.aura = aura;
    }


    public void restoreSlots() {
        if (aura.shieldBreak.c().booleanValue() && aura.target != null && aura.target.isBlocking()) {
            return;
        }
        if (aura.slots[0] != -1) {
            aM_.player.getInventory().setSelectedSlot(aura.slots[0]);
            aura.slots[0] = -1;
        }
        if (aura.slots[1] != -1 && Delta.h().d().v().a().a().isEmpty()) {
            Delta.h().d().v().a().a(aM_.player.getInventory().getSelectedSlot(), aura.slots[1], 1);
            aura.slots[1] = -1;
        }
    }


    public boolean canAttack() {
        if (aura.skipWhen.a("Используется предмет") != null && aura.skipWhen.a("Используется предмет").c().booleanValue() && aM_.player.isUsingItem() && aM_.player.getUseItemRemainingTicks() > 0 && aura.ctx.ticks >= 8) {
            aura.ctx.ticks = 8;
            return false;
        }
        if ((aura.skipWhen.a("Открыт контейнер") != null && aura.skipWhen.a("Открыт контейнер").c().booleanValue() && aM_.gui.screen() != null && !(aM_.gui.screen() instanceof GUIScreen)) || !AuraUtil.a(aura.target, aura.reachSetting.c().floatValue())) {
            return false;
        }
        if (Delta.h().d().t().H().t()) {
            if (aM_.player.getCooldowns().isOnCooldown(aM_.player.getMainHandItem())) {
                return false;
            }
        } else if (aM_.player.fallDistance > 1.5f) {
            if (aM_.player.getCooldowns().isOnCooldown(aM_.player.getMainHandItem()) || aura.ctx.ticks <= 3) {
                return false;
            }
        } else if (MaceUtil.a()) {
            if (aM_.player.getCooldowns().isOnCooldown(aM_.player.getMainHandItem()) || aM_.player.getAttackStrengthScale(0.5f) < 0.9f) {
                return false;
            }
        } else if (aM_.player.getAttackStrengthScale(0.5f) < 0.9f || aura.ctx.ticks < 10) {
            return false;
        }
        return AuraUtil.c() || (aura.adaptive.c().booleanValue() && aM_.player.onGround() && !aM_.player.input.keyPresses.jump()) || !AuraUtil.b();
    }


    public void attack() {
        if (!AuraUtil.a(aM_.player.getYRot(), aM_.player.getXRot(), aura.reachSetting.c().floatValue(), aura.target, !aura.skipWhen.a("Враг за стеной").c().booleanValue())) {
            return;
        }
        if (aura.shieldBreak.c().booleanValue() && aura.target.isBlocking()) {
            if (aM_.player.getMainHandItem().getItem() instanceof AxeItem) {
                aM_.gameMode.attack(aM_.player, aura.target);
                aM_.player.swing(InteractionHand.MAIN_HAND);
            }
            int hotbar = findAxe(0, 9);
            if (hotbar != -1) {
                if (aM_.player.getInventory().getSelectedSlot() != hotbar) {
                    if (aura.slots[0] == -1) {
                        aura.slots[0] = aM_.player.getInventory().getSelectedSlot();
                    }
                    aM_.player.getInventory().setSelectedSlot(hotbar);
                }
            } else {
                int inventory = findAxe(9, 36);
                if (inventory != -1 && aura.slots[1] == -1 && Delta.h().d().v().a().a().isEmpty()) {
                    aura.slots[1] = inventory;
                    Delta.h().d().v().a().a(inventory, aM_.player.getInventory().getSelectedSlot(), 1);
                }
            }
        }
        if (canAttack()) {
            boolean skip = false;
            if ((Delta.h().d().t().H().t() || (aM_.player.fallDistance > 2.0f && Delta.h().d().t().H().r().c().booleanValue())) && InventoryUtil.b(Items.MACE) != -1) {
                if (aM_.player.fallDistance < 1.5f) {
                    return;
                }
                double landDist = ((Double) MaceUtil.a(aM_.player, (Level) aM_.level).map(pos -> {
                    return Double.valueOf(pos.distanceTo(aura.target.position()));
                }).orElse(Double.valueOf(33.0d))).doubleValue();
                boolean hitNow = landDist > 2.0d;
                if ((!aura.landedCrit && !MaceUtil.b() && Delta.h().d().t().H().q().c().booleanValue() && !hitNow) || !MaceUtil.a() || aM_.player.isFallFlying()) {
                    return;
                } else {
                    skip = true;
                }
            }
            if (((LocalPlayerAccessor) (Object) aM_.player).getWasSprinting() && !aM_.player.isInWater() && !aM_.player.isInLava() && !aM_.player.isSwimming() && !aM_.player.onGround() && !skip) {
                // СпукиТайм: легитный сброс спринта — только через таймер паузы движения,
                // без мгновенного пакета STOP_SPRINTING. Ванильный клиент тоже делает это через
                // movement-tick, а не отдельным пакетом. smartSprint-путь: выставляем таймер и
                // ждём следующего тика когда wasSprinting уже сброшен движком.
                boolean spookyMode = aura.ctx.mode.equals("СпукиТайм") || aura.ctx.mode.equals("МХ");
                if (!aura.smartSprint.c().booleanValue() && !spookyMode) {
                    ((LocalPlayerAccessor) (Object) aM_.player).setWasSprinting(false);
                    aM_.player.setSprinting(false);
                    aM_.player.connection.send(new ServerboundPlayerCommandPacket(aM_.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                    aura.ctx.timers[0] = 1.0f;
                } else {
                    aura.ctx.timers[0] = 1.0f;
                    if (((LocalPlayerAccessor) (Object) aM_.player).getWasSprinting()) {
                        return;
                    }
                }
            }
            if (aM_.gameMode != null) {
                aura.ctx.timers[3] = 0.0f;
                aM_.gameMode.attack(aM_.player, aura.target);
                aM_.player.swing(InteractionHand.MAIN_HAND);
                aura.ctx.ticks = 0;
                aura.ctx.timers[5] = MathUtil.a(8.0f, 10.0f);
                aura.ctx.timers[9] = (int) MathUtil.a(9.0f, 13.0f);
                if (aura.ctx.timers[2] == -1.0f) {
                    aura.ctx.timers[4] = (int) MathUtil.a(30.0f, 35.0f);
                }
                aura.ctx.timers[2] += 1.0f;
            }
        }
    }

    private int findAxe(int from, int to) {
        for (int i = from; i < to; i++) {
            if (aM_.player.getInventory().getItem(i).getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }
}
