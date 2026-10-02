package platform.api.system.configs;

import static platform.api.module.Interface.aM_;
import platform.api.module.Interface;

import platform.api.system.configs.Condition;

import platform.client.utils.render.AnimationUtil;
import platform.api.utils.auction.ItemType;
import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.component.DataComponents;

public class EnchantmentCondition implements Condition {
    private final AnimationUtil a;
    private final ResourceKey<Enchantment> b;
    private ItemType c;
    private int d;

    @Override
    @Generated
    public AnimationUtil a() {
        return this.a;
    }

    @Generated
    public ResourceKey<Enchantment> i() {
        return this.b;
    }

    @Override
    @Generated
    public void a(ItemType type) {
        this.c = type;
    }

    @Override
    @Generated
    public ItemType h() {
        return this.c;
    }

    @Override
    @Generated
    public void a(int requiredLevel) {
        this.d = requiredLevel;
    }

    @Override
    @Generated
    public int g() {
        return this.d;
    }

    public EnchantmentCondition(ResourceKey<Enchantment> key) {
        this(key, 0, ItemType.ON);
    }

    public EnchantmentCondition(ResourceKey<Enchantment> key, int requiredLevel) {
        this(key, requiredLevel, ItemType.ON);
    }

    public EnchantmentCondition(ResourceKey<Enchantment> key, int requiredLevel, ItemType type) {
        this.a = new AnimationUtil();
        this.b = key;
        this.c = type;
        this.d = type == ItemType.ON ? c(requiredLevel) : requiredLevel;
    }

    @Override
    public String f() {
        return this.b.identifier().toString();
    }

    @Override
    public boolean b() {
        return this.c != ItemType.OFF;
    }

    @Override
    public void a(boolean enabled) {
        this.c = enabled ? ItemType.ON : ItemType.OFF;
    }

    @Override
    public boolean c() {
        return this.d > 0;
    }

    @Override
    public boolean d() {
        return this.c == ItemType.DENY;
    }

    @Override
    public void b(int delta) {
        if (this.c != ItemType.ON || this.d == 0) {
            return;
        }
        this.d = Math.max(1, c(this.d + delta));
    }

    public boolean a(ItemStack stack) {
        if (this.c == ItemType.OFF) {
            return true;
        }
        Holder.Reference class_6883VarMethod_46747 = Interface.aM_.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(this.b);
        ItemEnchantments enchantments = (ItemEnchantments) stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        int level = enchantments.getLevel(class_6883VarMethod_46747);
        int threshold = this.d == 0 ? 1 : this.d;
        if (this.c == ItemType.DENY) {
            return level < threshold;
        }
        return !enchantments.isEmpty() && level >= threshold;
    }

    @Override
    public String e() {
        return Component.translatable("enchantment." + this.b.identifier().getNamespace() + "." + this.b.identifier().getPath().replace("/", ".")).getString() + (this.d == 0 ? "" : " " + this.d);
    }

    private int c(int level) {
        int iMethod_8183;
        if (level <= 0) {
            return 0;
        }
        if (Interface.aM_.level == null || Interface.aM_.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(this.b).isEmpty()) {
            return level;
        }
        if (this.b.equals(Enchantments.SHARPNESS)) {
            iMethod_8183 = 7;
        } else {
            iMethod_8183 = (this.b.equals(Enchantments.PROTECTION) || this.b.equals(Enchantments.UNBREAKING)) ? 5 : ((Enchantment) Interface.aM_.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(this.b).value()).getMaxLevel();
        }
        return Math.max(1, Math.min(iMethod_8183, level));
    }
}



