package platform.api.event.events.render;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;

public class HotbarEvent extends Event implements IEvent {
    private final int a;

    @Generated
    public HotbarEvent(int slot) {
        this.a = slot;
    }

    @Generated
    public int b() {
        return this.a;
    }
}



