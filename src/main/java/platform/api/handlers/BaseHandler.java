package platform.api.handlers;

import platform.api.handlers.Handler_2;
import platform.api.event.EventManager;

public class BaseHandler {
    public BaseHandler() {
        if (!getClass().isAnnotationPresent(Handler_2.class)) {
            throw new IllegalStateException("Обработчик " + getClass().getSimpleName() + " должен иметь @Handler!");
        }
        EventManager.a(this);
    }
}


