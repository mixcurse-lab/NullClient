package platform.inject.accessors;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ServerboundMovePlayerPacket.class})
public interface ServerboundMovePlayerPacketAccessor {
    @Accessor("yRot")
    float getYaw();

    @Accessor("xRot")
    float getPitch();
}

