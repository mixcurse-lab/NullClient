package platform.api.event.events.player;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;

public class PortalEvent extends Event implements IEvent {
    private boolean a;

    @Generated
    public void b(boolean inPortal) {
        this.a = inPortal;
    }

    @Generated
    public PortalEvent(boolean inPortal) {
        this.a = inPortal;
    }

    @Generated
    public boolean b() {
        return this.a;
    }
}



