package platform.inject.accessors;


import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({Connection.class})
public interface ConnectionAccessor {
    @Invoker("sendPacket")
    void sendWithoutEvent(Packet<?> packet, ChannelFutureListener callback, boolean flush);
}
