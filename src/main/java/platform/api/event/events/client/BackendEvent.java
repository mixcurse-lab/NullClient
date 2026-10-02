package platform.api.event.events.client;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import platform.client.utils.bridge.network.Packet;
import lombok.Generated;

public class BackendEvent extends Event implements IEvent {
    private final Packet a;
    private final Phase b;

    public enum Phase {
        RECEIVE,
        CLOSE
    }

    @Generated
    public Packet d() {
        return this.a;
    }

    @Generated
    public Phase e() {
        return this.b;
    }

    public BackendEvent(Packet packet, Phase type) {
        this.a = packet;
        this.b = type;
    }

    public BackendEvent(Phase type) {
        this.b = type;
        this.a = null;
    }

    public boolean b() {
        return this.b == Phase.RECEIVE;
    }

    public boolean c() {
        return this.b == Phase.CLOSE;
    }
}



