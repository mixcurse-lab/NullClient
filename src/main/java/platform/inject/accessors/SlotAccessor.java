package platform.inject.accessors;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Slot.class})
public interface SlotAccessor {
    @Accessor("container")
    Container getInventory();

    @Accessor("x")
    @Mutable
    void setX(int i);

    @Accessor("y")
    @Mutable
    void setY(int i);
}
