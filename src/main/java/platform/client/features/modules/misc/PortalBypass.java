package platform.client.features.modules.misc;

import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.PortalEvent;

@ModuleRegister(a = "Portal Bypass", b = "Позволяет открывать окна, находясь в портале", c = Category.Misc)
public class PortalBypass extends Module {
    @EventTarget
    public void a(PortalEvent event) {
        event.b(false);
    }
}








