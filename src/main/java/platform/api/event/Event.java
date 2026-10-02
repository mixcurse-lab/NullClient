package platform.api.event;

import platform.api.event.interfaces.Cancellable;
import platform.api.event.interfaces.IEvent;

public class Event implements IEvent, Cancellable {
    private boolean a;

    protected Event() {
    }

    @Override
    public boolean a() {
        return this.a;
    }

    @Override
    public void a(boolean state) {
        this.a = state;
    }
}



