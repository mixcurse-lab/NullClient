package platform.inject.accessors;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("noJumpDelay")
    int getJumpingCooldown();

    @Accessor("noJumpDelay")
    @Mutable
    void setJumpingCooldown(int cooldown);
}

