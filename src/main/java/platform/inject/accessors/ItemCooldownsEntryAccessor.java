package platform.inject.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.world.item.ItemCooldowns$CooldownInstance")
public interface ItemCooldownsEntryAccessor {
    @Invoker("endTime")
    int getEndTime();

    @Invoker("startTime")
    int getStartTime();
}
