package platform.api.system.configs;

import platform.api.event.EventManager;

import platform.api.module.Interface;

public class BaseProcessor implements Interface {
    public void setup() {}

    public void unSetup() {}

    public BaseProcessor() {
        EventManager.a(this);
    }
}


