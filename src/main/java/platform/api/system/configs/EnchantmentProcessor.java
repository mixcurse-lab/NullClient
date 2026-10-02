package platform.api.system.configs;

import platform.api.utils.auction.ItemFilter;
import platform.api.utils.auction.ItemType;
import platform.api.system.configs.EnchantmentCondition;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.resources.ResourceKey;

public class EnchantmentProcessor implements ItemFilter {
    private final List<EnchantmentCondition> a = new ArrayList();

    @Generated
    public List<EnchantmentCondition> b() {
        return this.a;
    }

    public EnchantmentProcessor a(EnchantmentCondition condition) {
        this.a.add(condition);
        return this;
    }

    public EnchantmentProcessor a(ResourceKey<Enchantment> key) {
        return a(new EnchantmentCondition(key));
    }

    public EnchantmentProcessor a(ResourceKey<Enchantment> key, int requiredLevel) {
        return a(new EnchantmentCondition(key, requiredLevel));
    }

    public EnchantmentProcessor b(ResourceKey<Enchantment> key) {
        return a(new EnchantmentCondition(key, 0, ItemType.DENY));
    }

    public EnchantmentProcessor b(ResourceKey<Enchantment> key, int maxLevel) {
        return a(new EnchantmentCondition(key, maxLevel, ItemType.DENY));
    }

    public EnchantmentProcessor a() {
        return b(Enchantments.THORNS).b(Enchantments.KNOCKBACK).b(Enchantments.BINDING_CURSE).b(Enchantments.VANISHING_CURSE);
    }

    @Override
    public boolean a(ItemStack stack) {
        return this.a.stream().allMatch(condition -> {
            return condition.a(stack);
        });
    }
}



