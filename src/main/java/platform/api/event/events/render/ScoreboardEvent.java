package platform.api.event.events.render;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.network.chat.Component;

public class ScoreboardEvent extends Event implements IEvent {
    private Component a;

    @Generated
    public void a(Component title) {
        this.a = title;
    }

    @Generated
    public Component b() {
        return this.a;
    }

    public ScoreboardEvent(Component title) {
        this.a = title;
    }
}



