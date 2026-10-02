package platform.client.features.modules.player;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;

import net.minecraft.client.gui.screens.DeathScreen;

@ModuleRegister(a = "Auto Respawn", b = "Автоматически возрождает персонажа после смерти", c = Category.Player)
public class AutoRespawn extends Module {
    @EventTarget
    public void a(TickEvent event) {
        if ((aM_.gui.screen() instanceof DeathScreen) && aM_.player.deathTime >= 5) {
            aM_.player.respawn();
        }
    }
}





