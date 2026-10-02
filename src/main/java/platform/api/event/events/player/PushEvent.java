package platform.api.event.events.player;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;

public class PushEvent extends Event implements IEvent {
    private final a a;

    public enum a {
        BLOCKS,
        FLUIDS,
        ENTITIES,
        WORLD_BORDER,
        FISHING_HOOK
    }

    @Generated
    public a b() {
        return this.a;
    }

    public PushEvent(a type) {
        this.a = type;
    }
}



