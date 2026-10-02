package platform.client.features.modules.player;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;

@ModuleRegister(a = "Death Coords", b = "Выводит координаты последней смерти", c = Category.Player)
public class DeathCoords extends Module {
    @EventTarget
    public void a(TickEvent event) {
        if (aM_.player.deathTime == 1) {
            ChatUtil.a((Object) String.format("Вы погибли на координатах: &c[%d, %d, %d]", Integer.valueOf(aM_.player.blockPosition().getX()), Integer.valueOf(aM_.player.blockPosition().getY()), Integer.valueOf(aM_.player.blockPosition().getZ())));
        }
    }
}





