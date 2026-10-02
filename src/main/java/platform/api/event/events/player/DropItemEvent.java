package platform.api.event.events.player;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;

public class DropItemEvent extends Event implements IEvent {
    private int a;

    @Generated
    public DropItemEvent(int slot) {
        this.a = slot;
    }

    @Generated
    public int b() {
        return this.a;
    }
}



