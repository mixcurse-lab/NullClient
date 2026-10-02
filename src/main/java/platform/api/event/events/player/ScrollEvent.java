package platform.api.event.events.player;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;

public class ScrollEvent extends Event implements IEvent {
    private final double a;
    private final double b;

    @Generated
    public ScrollEvent(double horizontal, double vertical) {
        this.a = horizontal;
        this.b = vertical;
    }

    @Generated
    public double b() {
        return this.a;
    }

    @Generated
    public double c() {
        return this.b;
    }
}



