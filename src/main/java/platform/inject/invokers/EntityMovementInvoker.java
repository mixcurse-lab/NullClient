package platform.inject.invokers;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({Entity.class})
public interface EntityMovementInvoker {
    @Invoker("collide")
    Vec3 getAdjustMovementForCollisions(Vec3 movement);
}

