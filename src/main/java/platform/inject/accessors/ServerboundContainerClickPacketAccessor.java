package platform.inject.accessors;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ServerboundContainerClickPacket.class})
public interface ServerboundContainerClickPacketAccessor {
    @Accessor("containerId")
    int getSyncId();

    @Accessor("slotNum")
    short getSlot();

    @Accessor("slotNum")
    @Mutable
    void setSlot(short i);
}

