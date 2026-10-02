package platform.api.system.configs;


import java.util.stream.StreamSupport;
import lombok.Generated;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;

public class PotionCondition {
    private final Holder<MobEffect> a;
    private final int b;
    private final int c;

    @Generated
    public Holder<MobEffect> a() {
        return this.a;
    }

    @Generated
    public int b() {
        return this.b;
    }

    @Generated
    public int c() {
        return this.c;
    }

    public PotionCondition(Holder<MobEffect> effect, int requiredLevel, int requiredDuration) {
        if (effect == null) {
            throw new IllegalArgumentException("Effect cannot be null");
        }
        this.a = effect;
        this.b = requiredLevel;
        this.c = requiredDuration;
    }

    public boolean a(ItemStack stack) {
        PotionContents contents = (PotionContents) stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return false;
        }
        return StreamSupport.stream(contents.customEffects().spliterator(), false).anyMatch(effect -> {
            return effect.getEffect().equals(this.a) && effect.getAmplifier() + 1 == this.b && effect.getDuration() >= this.c;
        });
    }

    public String toString() {
        return "PotionCondition{effect=" + ((String) this.a.unwrapKey().map(k -> {
            return k.identifier().toString();
        }).orElse("unknown")) + ", requiredLevel=" + this.b + ", requiredDuration=" + this.c + "}";
    }
}



