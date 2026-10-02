package platform.api.event.events.client;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;

public class TextVisitEvent extends Event implements IEvent {
    private String a;

    @Generated
    public void a(String text) {
        this.a = text;
    }

    @Generated
    public TextVisitEvent(String text) {
        this.a = text;
    }

    @Generated
    public String b() {
        return this.a;
    }
}



