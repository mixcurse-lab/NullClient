package platform.client.features.modules.combat;

import platform.api.handlers.UseableHandler;
import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.player.InventoryUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;
import platform.client.features.modules.combat.Aura;
import platform.client.features.modules.combat.TriggerBot;

import platform.api.module.setting.BindSetting;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.SliderSetting;
import java.util.List;
import lombok.Generated;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import platform.client.utils.rotation.Look;
import platform.client.utils.rotation.Rotation;
import platform.client.utils.text.ChatUtil;
import platform.inject.invokers.MultiPlayerGameModeInvoker;

@ModuleRegister(a = "Mace Helper", b = "Автоматизирует действия при использовании булавы", c = Category.Combat)
public class MaceHelper extends Module {
    private int swapBackDelay = 0;
    private boolean swapped = false;
    private int[] prevSlot = {-1, -1};

    public final BooleanSetting boostDamage = new BooleanSetting("Усиление урона", true);
    public final BooleanSetting autoSwap = new BooleanSetting("Авто-переключение булавы", true);
    public final SliderSetting swapDistance = new SliderSetting("Дистанция свапа", 6.0f, 1.0f, 10.0f, 0.5f);
    public final SliderSetting minFallDist = new SliderSetting("Мин. высота падения", 3.5f, 1.0f, 10.0f, 0.5f);
    public final SliderSetting maxFallDist = new SliderSetting("Макс. высота падения", 20.0f, 5.0f, 50.0f, 0.5f);
    public final BooleanSetting onlyWithTarget = new BooleanSetting("Только с целью", true);
    public final BooleanSetting swapBack = new BooleanSetting("Возвращать слот", true);
    public final SliderSetting swapBackTicks = new SliderSetting("Задержка возврата (тики)", 3.0f, 0.0f, 20.0f, 1.0f);
    public final BindSetting windChargeBind = new BindSetting("Кинуть заряд ветра", -1).a(this::throwWindCharge);
    public final SliderSetting throwDelay = new SliderSetting("Задержка броска заряда", 2.0f, 0.0f, 10.0f, 1.0f);

    private WindPhase windPhase = WindPhase.IDLE;
    private int windRotationTicks = 0;
    private long lastWindThrowMs = 0L;
    private float savedWindYaw;
    private float savedWindPitch;

    private enum WindPhase {
        IDLE,
        ROTATING
    }

    @Generated
    public BooleanSetting q() { return this.boostDamage; }

    @Generated
    public BooleanSetting r() { return this.autoSwap; }

    @Generated
    public int s() { return this.swapBackDelay; }

    @Generated
    public boolean t() { return this.swapped; }

    @Generated
    public int[] u() { return this.prevSlot; }

    public MaceHelper() {
        a(this.boostDamage, this.autoSwap, this.swapDistance, this.minFallDist, this.maxFallDist, this.onlyWithTarget, this.swapBack, this.swapBackTicks, this.windChargeBind, this.throwDelay);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.windPhase == WindPhase.ROTATING) {
            handleWindRotation();
        }
        if (!this.autoSwap.c().booleanValue()) {
            handleSwapBack();
            return;
        }

        List<UseableHandler.a> tasks = Delta.h().d().v().b().a();
        int hotbar = InventoryUtil.a(Items.MACE, true);
        int slotMace = InventoryUtil.a(Items.MACE, false);

        if (slotMace == -1) {
            handleSwapBack();
            return;
        }

        Aura aura = Delta.h().d().t().B();
        TriggerBot triggerBot = Delta.h().d().t().X();
        boolean fromAura = aura.s() != null;
        LivingEntity target = fromAura ? aura.s() : triggerBot.s();

        boolean shouldSwap = false;

        if (target != null && target.isAlive()) {
            double dist = Math.hypot(target.getX() - aM_.player.getX(), target.getZ() - aM_.player.getZ());
            double fallDist = aM_.player.fallDistance;
            boolean hasTarget = !this.onlyWithTarget.c().booleanValue() || target != null;
            boolean notOnGround = !aM_.player.onGround();
            boolean falling = fallDist > 0.0f;
            boolean inRange = dist <= this.swapDistance.c().doubleValue();
            boolean enoughHeight = fallDist >= this.minFallDist.c().doubleValue();
            boolean notTooHigh = fallDist <= this.maxFallDist.c().doubleValue();
            boolean notBlocking = !target.isBlocking();
            boolean maceNotHeld = !MaceUtil.a();
            boolean noCooldown = !aM_.player.getCooldowns().isOnCooldown(Items.MACE.getDefaultInstance());
            boolean noWindCharge = tasks.isEmpty() || ((UseableHandler.a) tasks.getFirst()).a().getItem() != Items.WIND_CHARGE;
            boolean landingValid = MaceUtil.a(aM_.player, (Level) aM_.level).map(pos -> {
                return aM_.player.getY() - pos.y >= this.minFallDist.c().doubleValue() && aM_.player.getY() + aM_.player.getDeltaMovement().y > pos.y;
            }).orElse(false);

            shouldSwap = hasTarget && notOnGround && falling && inRange && enoughHeight && notTooHigh && notBlocking && maceNotHeld && noCooldown && noWindCharge && landingValid;
        }

        if (shouldSwap && !this.swapped) {
            int currentSlot = aM_.player.getInventory().getSelectedSlot();
            int maceSlot = hotbar != -1 ? hotbar : slotMace;

            if (currentSlot != maceSlot) {
                this.prevSlot[0] = currentSlot;
                this.prevSlot[1] = maceSlot;
                this.swapped = true;
                this.swapBackDelay = 0;

                if (hotbar == -1) {
                    Delta.h().d().v().a().a(maceSlot, currentSlot, 1);
                } else {
                    aM_.player.getInventory().setSelectedSlot(maceSlot);
                }
            }
        } else if (!shouldSwap && this.swapped) {
            handleSwapBack();
        }

        handleSwapBack();
    }

    private void throwWindCharge() {
        if (this.windPhase != WindPhase.IDLE || aM_.player == null || aM_.gui.screen() != null) {
            return;
        }
        if (System.currentTimeMillis() - this.lastWindThrowMs < 100L) {
            return;
        }
        if (InventoryUtil.a(Items.WIND_CHARGE, false) == -1) {
            ChatUtil.a("&cMace Helper&7 - заряда ветра нет в инвентаре");
            return;
        }
        if (aM_.player.getCooldowns().isOnCooldown(Items.WIND_CHARGE.getDefaultInstance())) {
            ChatUtil.a("&cMace Helper&7 - заряд ветра на кулдауне");
            return;
        }
        this.lastWindThrowMs = System.currentTimeMillis();
        this.savedWindYaw = Look.b();
        this.savedWindPitch = Look.c();
        this.windRotationTicks = 0;
        this.windPhase = WindPhase.ROTATING;
    }

    private void handleWindRotation() {
        Delta.h().d().k().a(new Rotation(Look.b(), 90.0f), 180.0f, 1, 3);
        this.windRotationTicks++;
        boolean rotationReady = aM_.player.getXRot() >= 80.0f && this.windRotationTicks >= Math.max(0, this.throwDelay.c().intValue());
        if (rotationReady) {
            useWindChargeNow();
            return;
        }
        if (this.windRotationTicks > 10) {
            this.windPhase = WindPhase.IDLE;
        }
    }

    private void useWindChargeNow() {
        Inventory inv = aM_.player.getInventory();
        int selected = inv.getSelectedSlot();
        int hotbarSlot = InventoryUtil.a(Items.WIND_CHARGE, true);
        int anySlot = InventoryUtil.a(Items.WIND_CHARGE, false);
        if (anySlot == -1) {
            this.windPhase = WindPhase.IDLE;
            return;
        }
        boolean inventorySwap = hotbarSlot == -1;
        int chargeSlot = inventorySwap ? anySlot : hotbarSlot;
        if (inventorySwap) {
            Delta.h().d().v().a().a(chargeSlot, selected, 1);
        } else if (chargeSlot != selected) {
            inv.setSelectedSlot(chargeSlot);
            aM_.player.connection.send(new ServerboundSetCarriedItemPacket(inv.getSelectedSlot()));
        }
        ((MultiPlayerGameModeInvoker) aM_.gameMode).invokeStartPrediction(aM_.level, sequence -> {
            return new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, aM_.player.getYRot(), aM_.player.getXRot());
        });
        if (inventorySwap) {
            Delta.h().d().v().a().a(selected, chargeSlot, 1);
        } else if (chargeSlot != selected) {
            inv.setSelectedSlot(selected);
            aM_.player.connection.send(new ServerboundSetCarriedItemPacket(inv.getSelectedSlot()));
        }
        Delta.h().d().k().a(new Rotation(this.savedWindYaw, this.savedWindPitch), 180.0f, 0, 3);
        this.windPhase = WindPhase.IDLE;
    }

    private void handleSwapBack() {
        if (!this.swapped) return;

        if (this.swapBack.c().booleanValue() && this.prevSlot[0] != -1) {
            this.swapBackDelay++;
            if (this.swapBackDelay >= this.swapBackTicks.c().intValue()) {
                int currentSlot = aM_.player.getInventory().getSelectedSlot();
                if (currentSlot == this.prevSlot[1]) {
                    if (this.prevSlot[1] > 8) {
                        Delta.h().d().v().a().a(this.prevSlot[0], this.prevSlot[1], 1);
                    } else {
                        aM_.player.getInventory().setSelectedSlot(this.prevSlot[0]);
                    }
                }
                this.swapped = false;
                this.prevSlot = new int[]{-1, -1};
                this.swapBackDelay = 0;
            }
        } else if (!this.swapBack.c().booleanValue()) {
            this.swapped = false;
            this.prevSlot = new int[]{-1, -1};
            this.swapBackDelay = 0;
        }
    }

    @Override
    public void c() {
        super.c();
        if (this.swapped && this.swapBack.c().booleanValue() && this.prevSlot[0] != -1) {
            int currentSlot = aM_.player.getInventory().getSelectedSlot();
            if (currentSlot == this.prevSlot[1]) {
                if (this.prevSlot[1] > 8) {
                    Delta.h().d().v().a().a(this.prevSlot[0], this.prevSlot[1], 1);
                } else {
                    aM_.player.getInventory().setSelectedSlot(this.prevSlot[0]);
                }
            }
        }
        this.swapped = false;
        this.prevSlot = new int[]{-1, -1};
        this.swapBackDelay = 0;
        this.windPhase = WindPhase.IDLE;
    }
}