package platform.api.event.events.render;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;

public class GammaEvent extends Event implements IEvent {
    private double a;

    @Generated
    public void a(double gamma) {
        this.a = gamma;
    }

    @Generated
    public GammaEvent(double gamma) {
        this.a = gamma;
    }

    @Generated
    public double b() {
        return this.a;
    }
}



