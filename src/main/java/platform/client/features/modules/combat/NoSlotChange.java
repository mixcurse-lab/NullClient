package platform.client.features.modules.combat;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.PacketEvent;

import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

@ModuleRegister(a = "No Slot Change", b = "Не даёт серверу принудительно менять активный слот в хотбаре", c = Category.Combat)
public class NoSlotChange extends Module {
    @EventTarget
    public void a(PacketEvent event) {
        if (event.c() && (event.d() instanceof ClientboundSetHeldSlotPacket)) {
            aM_.player.connection.send(new ServerboundSetCarriedItemPacket(aM_.player.getInventory().getSelectedSlot()));
            event.a(true);
        }
    }
}


