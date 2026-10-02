package platform.inject.invokers;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Level.class)
public interface LevelInvoker {
    @Invoker("getEntities")
    LevelEntityGetter<Entity> deltaGetEntities();
}
