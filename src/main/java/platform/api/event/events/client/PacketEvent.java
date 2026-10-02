package platform.api.event.events.client;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.network.protocol.Packet;

public class PacketEvent extends Event implements IEvent {
    public enum Type {
        SEND,
        RECEIVE
    }

    @Generated
    public Packet<?> d() {
        return this.packet;
    }

    @Generated
    public Type e() {
        return this.type;
    }

    private final Packet<?> packet;
    private final Type type;

    public PacketEvent(Packet<?> packet, Type type) {
        this.packet = packet;
        this.type = type;
    }

    public boolean b() {
        return this.type == Type.SEND;
    }

    public boolean c() {
        return this.type == Type.RECEIVE;
    }
}



