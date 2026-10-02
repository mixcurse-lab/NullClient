package platform.client.utils.player;

import static platform.api.module.Interface.aM_;

import platform.api.module.Interface;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Generated;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class InventoryUtil implements Interface {
    @Generated
    private InventoryUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static ItemStack a(ItemStack stack) {
        Equippable equippable = (Equippable) stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.assetId().isPresent() && stack.getMaxDamage() > 0) {
            String percent = equippable.assetId().get().identifier().getPath();
            String percent2 = percent.substring(percent.lastIndexOf(95) + 1);
            if (percent2.matches("\\d+")) {
                ItemStack copy = stack.copy();
                copy.setDamageValue((copy.getMaxDamage() * (100 - Integer.parseInt(percent2))) / 100);
                return copy;
            }
        }
        return stack;
    }

    public static int a(Item item) {
        int count = 0;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = aM_.player.getInventory().getItem(slot);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static boolean a(ItemStack stack, Holder<Enchantment> enchantment, int minLevel) {
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack) >= minLevel;
    }

    public static boolean a(ItemStack stack, String needle) {
        return stack.getTooltipLines(Item.TooltipContext.of(aM_.level), aM_.player, TooltipFlag.NORMAL).stream().anyMatch(line -> {
            return line.getString().contains(needle);
        });
    }

    public static int a(Item item, boolean hotbar) {
        int end = hotbar ? 9 : 36;
        for (int i = 0; i < end; i++) {
            ItemStack stack = aM_.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static int b(Item item) {
        return a(item, false);
    }

    public static int a(Item item, boolean hotbar, boolean simple) {
        int end = hotbar ? 9 : 36;
        for (int i = 0; i < end; i++) {
            ItemStack stack = aM_.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item && (!simple || !stack.hasFoil())) {
                return i;
            }
        }
        return -1;
    }

    public static int a(ItemStack stack, boolean hotbar) {
        if (stack != null && !stack.isEmpty()) {
            int slotLimit = hotbar ? 9 : 36;
            for (int slotIndex = 0; slotIndex < slotLimit; slotIndex++) {
                ItemStack candidate = aM_.player.getInventory().getItem(slotIndex);
                if (!candidate.isEmpty() && candidate.getItem() == stack.getItem() && Objects.equals(candidate.get(DataComponents.ATTRIBUTE_MODIFIERS), stack.get(DataComponents.ATTRIBUTE_MODIFIERS))) {
                    return slotIndex;
                }
            }
            return -1;
        }
        return -1;
    }

    public static int b(ItemStack targetStack, boolean hotbar) {
        return -1;
    }

    public static int a(ItemStack bundleStack, ItemStack targetStack) {
        return -1;
    }

    public static int c(ItemStack targetStack, boolean hotbar) {
        return 0;
    }

    public static int c(Item item) {
        ItemAttributeModifiers modifiers;
        int fallbackSlot = -1;
        for (int i = 0; i < 45; i++) {
            if (i != 40 && aM_.player.getInventory().getItem(i).getItem() == item) {
                if (aM_.player.getInventory().getItem(i).has(DataComponents.ATTRIBUTE_MODIFIERS) && (modifiers = (ItemAttributeModifiers) aM_.player.getInventory().getItem(i).get(DataComponents.ATTRIBUTE_MODIFIERS)) != null && !modifiers.modifiers().isEmpty()) {
                    return i;
                }
                if (fallbackSlot == -1) {
                    fallbackSlot = i;
                }
            }
        }
        return fallbackSlot;
    }

    public static int a() {
        Holder.Reference<Enchantment> protection = aM_.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION);
        int bestSlot = -1;
        double bestScore = 0.0d;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = aM_.player.getInventory().getItem(slot);
            Equippable equippable = (Equippable) stack.get(DataComponents.EQUIPPABLE);
            if (equippable != null && equippable.slot() == EquipmentSlot.CHEST && !stack.isEmpty() && !stack.is(Items.ELYTRA)) {
                double score = EnchantmentHelper.getItemEnchantmentLevel(protection, stack);
                ItemAttributeModifiers mods = (ItemAttributeModifiers) stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
                if (mods != null) {
                    for (ItemAttributeModifiers.Entry entry : mods.modifiers()) {
                        Holder<Attribute> attribute = entry.attribute();
                        if (attribute == Attributes.ARMOR || attribute == Attributes.ARMOR_TOUGHNESS) {
                            score += entry.modifier().amount();
                        }
                    }
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestSlot = slot;
                }
            }
        }
        return bestSlot;
    }
}



