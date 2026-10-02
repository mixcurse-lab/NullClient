package platform.api.event.events.player;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;

public class WillLandEvent extends Event implements IEvent {
    private final boolean a;

    @Generated
    public WillLandEvent(boolean willLand) {
        this.a = willLand;
    }

    @Generated
    public boolean b() {
        return this.a;
    }
}



