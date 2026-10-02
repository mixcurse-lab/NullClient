package platform.client.features.modules.combat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Generated;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import static platform.api.module.Interface.aM_;
import platform.api.event.interfaces.EventTarget;
import platform.api.event.events.client.TickEvent;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.SliderSetting;
import platform.client.Delta;
import platform.client.utils.render.ColorUtil;
import platform.api.utils.notification.Notification;
import platform.inject.invokers.MinecraftInvoker;

@ModuleRegister(a = "Lunge Boost", b = "Буст копьём: свапает слоты и бьёт для активации Рывка (Lunge)", c = Category.Combat)
public class LungeBoost extends Module {
    private Phase phase = Phase.SAFE_SLOT;
    private int phaseTimer = 0;
    private int cachedSafeSlot = -1;
    private int cachedSpearSlot = -1;
    private boolean started = false;
    private int errorCooldown = 0;

    public final SliderSetting boostInterval = new SliderSetting("Интервал буста", 5.0f, 1.0f, 20.0f, 1.0f);
    public final SliderSetting delayBeforeHit = new SliderSetting("Задержка удара", 0.0f, 0.0f, 10.0f, 1.0f);
    public final BooleanSetting onlyMoving = new BooleanSetting("Только в движении", false);
    public final BooleanSetting packetMode = new BooleanSetting("Пакетно", false);

    @Generated
    public SliderSetting r() { return this.boostInterval; }

    @Generated
    public SliderSetting s() { return this.delayBeforeHit; }

    @Generated
    public BooleanSetting t() { return this.onlyMoving; }

    @Generated
    public BooleanSetting u() { return this.packetMode; }

    public LungeBoost() {
        a(this.boostInterval, this.delayBeforeHit, this.onlyMoving, this.packetMode);
    }

    @Override
    public void c() {
        super.c();
        int spearSlot = this.cachedSpearSlot;
        int safeSlot = this.cachedSafeSlot;
        reset();
        if (aM_.player != null && aM_.player.getInventory().getSelectedSlot() == spearSlot) {
            select(aM_.player.getInventory(), safeSlot != -1 ? safeSlot : 0);
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (aM_.player == null || aM_.gameMode == null) {
            return;
        }
        if (this.errorCooldown > 0) {
            --this.errorCooldown;
        }
        if (this.phaseTimer > 0) {
            --this.phaseTimer;
            return;
        }
        if (this.onlyMoving.c().booleanValue() && !isMoving()) {
            return;
        }
        Inventory inv = aM_.player.getInventory();
        this.cachedSpearSlot = findSpear(inv);
        if (this.cachedSpearSlot == -1) {
            notify("Копьё с Рывком не найдено");
            reset();
            return;
        }
        this.cachedSafeSlot = findSafeSlot(inv, this.cachedSpearSlot);
        if (this.cachedSafeSlot == -1) {
            notify("Нет безопасного слота для свапа");
            reset();
            return;
        }
        if (!this.started && this.phase == Phase.SAFE_SLOT) {
            this.started = true;
            this.phase = Phase.SPEAR_SLOT;
        }
        switch (this.phase) {
            case SAFE_SLOT -> {
                select(inv, this.cachedSafeSlot);
                this.phaseTimer = Math.max(1, this.boostInterval.c().intValue());
                this.phase = Phase.SPEAR_SLOT;
            }
            case SPEAR_SLOT -> {
                select(inv, this.cachedSpearSlot);
                if (this.delayBeforeHit.c().intValue() <= 0) {
                    attack();
                    this.phase = Phase.SAFE_SLOT;
                    break;
                }
                this.phaseTimer = this.delayBeforeHit.c().intValue();
                this.phase = Phase.HIT;
            }
            case HIT -> {
                attack();
                this.phase = Phase.SAFE_SLOT;
            }
        }
    }

    private void reset() {
        this.phase = Phase.SAFE_SLOT;
        this.phaseTimer = 0;
        this.cachedSafeSlot = -1;
        this.cachedSpearSlot = -1;
        this.started = false;
    }

    private boolean isMoving() {
        double dx = aM_.player.getDeltaMovement().x;
        double dz = aM_.player.getDeltaMovement().z;
        return Math.abs(dx) >= 0.03d || Math.abs(dz) >= 0.03d || aM_.player.input.keyPresses.forward() || aM_.player.input.keyPresses.backward() || aM_.player.input.keyPresses.left() || aM_.player.input.keyPresses.right();
    }

    private void select(Inventory inv, int slot) {
        inv.setSelectedSlot(slot);
        aM_.player.connection.send(new ServerboundSetCarriedItemPacket(inv.getSelectedSlot()));
    }

    private void attack() {
        ((MinecraftInvoker) aM_).invokeDoAttack();
    }

    private static int findSpear(Inventory inv) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (!itemId.getPath().endsWith("_spear")) {
                continue;
            }
            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                if (entry.getKey().unwrapKey().map(key -> key.identifier().getPath().equals("lunge")).orElse(false)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int findSafeSlot(Inventory inv, int spearSlot) {
        int fallback = -1;
        for (int i = 0; i < 9; ++i) {
            if (i == spearSlot) {
                continue;
            }
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                return i;
            }
            if (stack.has(DataComponents.FOOD) || stack.getUseAnimation() != ItemUseAnimation.NONE) {
                continue;
            }
            ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (modifiers != null && modifiers.modifiers().stream().anyMatch(entry -> entry.attribute().equals(Attributes.ATTACK_SPEED))) {
                continue;
            }
            fallback = i;
        }
        return fallback;
    }

    private void notify(String message) {
        if (this.errorCooldown > 0) {
            return;
        }
        this.errorCooldown = 60;
        Delta.h().d().m().a(new Notification("Q", ColorUtil.a(255, 85, 85, 255), j() + ": " + message, 2000));
    }

    private enum Phase {
        SAFE_SLOT,
        SPEAR_SLOT,
        HIT
    }
}
