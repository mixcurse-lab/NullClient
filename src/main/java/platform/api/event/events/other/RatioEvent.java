package platform.api.event.events.other;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;

public class RatioEvent extends Event implements IEvent {
    private float a;

    @Generated
    public RatioEvent(float ratio) {
        this.a = ratio;
    }

    @Generated
    public void a(float ratio) {
        this.a = ratio;
    }

    @Generated
    public float b() {
        return this.a;
    }
}



