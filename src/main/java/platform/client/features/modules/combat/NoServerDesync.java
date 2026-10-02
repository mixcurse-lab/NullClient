package platform.client.features.modules.combat;

import platform.api.module.Interface;

import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.PacketEvent;

import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

@ModuleRegister(a = "No Server Desync", b = "Не даёт серверу принудительно сбрасывать поворот вашей камеры", c = Category.Combat)
public class NoServerDesync extends Module {
    @EventTarget
    public void a(PacketEvent event) {
        if (event.c() && event.d() instanceof ClientboundPlayerPositionPacket packet) {
            if (packet.change().xRot() != 0.0f && packet.change().yRot() != 0.0f) {
                event.a(true);
            }
        }
    }
}


