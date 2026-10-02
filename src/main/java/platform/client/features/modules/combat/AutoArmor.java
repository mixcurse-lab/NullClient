package platform.client.features.modules.combat;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.BooleanSetting;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;

@ModuleRegister(a = "Auto Armor", b = "Автоматически надевает лучшую броню из инвентаря и мешков", c = Category.Combat)
public class AutoArmor extends Module implements Interface {
    private final BooleanSetting b = new BooleanSetting("Не в движении", true);
    private final EquipmentSlot[] c = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private int d;

    public AutoArmor() {
        a(this.b);
    }

    @Override
    public void b() {
        super.b();
        this.d = 0;
    }

    @Override
    public void c() {
        super.c();
        this.d = 0;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (!Delta.h().d().v().a().a().isEmpty()) {
            return;
        }
        this.d++;
        boolean urgent = a(aM_.player.getItemBySlot(EquipmentSlot.HEAD)) || a(aM_.player.getItemBySlot(EquipmentSlot.CHEST)) || a(aM_.player.getItemBySlot(EquipmentSlot.LEGS)) || a(aM_.player.getItemBySlot(EquipmentSlot.FEET));
        if (!urgent) {
            if (this.d % 2 != 0) {
                return;
            }
            if (this.b.c().booleanValue() && aM_.player.getDeltaMovement().horizontalDistanceSqr() > 9.99999713651348E-5d) {
                return;
            }
        }
        for (int armorIndex = 0; armorIndex < this.c.length && !a(this.c[armorIndex], armorIndex); armorIndex++) {
        }
    }

    private boolean a(EquipmentSlot slot, int armorIndex) {
        ItemStack current = aM_.player.getItemBySlot(slot);
        boolean low = a(current);
        double best = low ? b(current) + 20 : c(current);
        int bestSlot = -1;
        int bestBundle = -1;
        for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
            ItemStack stack = aM_.player.getInventory().getItem(inventorySlot);
            if (a(stack, slot)) {
                double value = low ? b(stack) : c(stack);
                if (value > best) {
                    best = value;
                    bestSlot = inventorySlot;
                    bestBundle = -1;
                }
            }
            BundleContents contents = (BundleContents) stack.get(DataComponents.BUNDLE_CONTENTS);
            if (contents != null) {
                for (int bundleIndex = 0; bundleIndex < contents.size(); bundleIndex++) {
                    ItemStack bundled = contents.items().get(bundleIndex).create();
                    if (a(bundled, slot)) {
                        double value2 = low ? b(bundled) : c(bundled);
                        if (value2 > best) {
                            best = value2;
                            bestSlot = inventorySlot;
                            bestBundle = bundleIndex;
                        }
                    }
                }
            }
        }
        if (bestSlot == -1) {
            return false;
        }
        if (bestBundle != -1) {
            aM_.player.connection.send(new ServerboundSelectBundleItemPacket(bestSlot < 9 ? 36 + bestSlot : bestSlot, bestBundle));
        }
        Delta.h().d().v().a().b(bestSlot, armorIndex, 1);
        return true;
    }

    private boolean a(ItemStack stack) {
        return !stack.isEmpty() && stack.getMaxDamage() > 0 && b(stack) < 41;
    }

    private boolean a(ItemStack stack, EquipmentSlot slot) {
        Equippable equippable;
        return (stack.isEmpty() || (equippable = (Equippable) stack.get(DataComponents.EQUIPPABLE)) == null || equippable.slot() != slot || (slot == EquipmentSlot.CHEST && stack.is(Items.ELYTRA))) ? false : true;
    }

    private int b(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    private double c(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0d;
        }
        double score = EnchantmentHelper.getItemEnchantmentLevel(aM_.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION), stack);
        ItemAttributeModifiers modifiers = (ItemAttributeModifiers) stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers != null) {
            for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                if (entry.attribute() == Attributes.ARMOR || entry.attribute() == Attributes.ARMOR_TOUGHNESS) {
                    score += entry.modifier().amount();
                }
            }
        }
        return score;
    }
}


