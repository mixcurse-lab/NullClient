package platform.api.event.events.render;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

public class WorldRenderEvent extends Event implements IEvent {
    private final float b;

    public WorldRenderEvent(float partialTicks) {
        this.b = partialTicks;
    }

    public float b() {
        return this.b;
    }
}
