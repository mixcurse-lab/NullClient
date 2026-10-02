package platform.client.features.modules.movement;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.PacketEvent;

import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.player.Input;

@ModuleRegister(a = "No Crouch", b = "Убирает замедление от приседания на вашей стороне", c = Category.Movement)
public class NoCrouch extends Module {
    private boolean b;

    @EventTarget
    public void a(InputEvent e) {
        boolean sneaking = e.e();
        if (sneaking != this.b) {
            Input input = aM_.player.input.keyPresses;
            aM_.player.connection.send(new ServerboundPlayerInputPacket(new Input(input.forward(), input.backward(), input.left(), input.right(), input.jump(), sneaking, input.sprint())));
            this.b = sneaking;
        }
        e.c(false);
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.b() && this.b && event.d() instanceof ServerboundPlayerInputPacket) {
            Input input = ((ServerboundPlayerInputPacket) event.d()).input();
            if (!input.shift()) {
                event.a(true);
            }
        }
    }
}


