package platform.inject.mixin;

import static platform.api.module.Interface.aM_;

import platform.api.module.InterfaceC0020Opcode;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
    @Unique private Button delta$reconnectButton;

    private DisconnectedScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void delta$init(CallbackInfo ci) {
        ServerData server = aM_.getCurrentServer();
        if (server != null) {
            this.delta$reconnectButton = addRenderableWidget(Button.builder(Component.literal("Переподключиться"), btn -> {
                try {
                    ServerAddress address = ServerAddress.parseString(server.ip);
                    ConnectScreen.startConnecting(new TitleScreen(), aM_, address, server, false, null);
                } catch (Exception e) {
                }
            }).bounds(0, 0, InterfaceC0020Opcode.aN, 20).build());

            int maxY = children().stream().filter(c -> c instanceof Button && c != this.delta$reconnectButton)
                    .map(c -> ((Button) c).getY()).max(Integer::compareTo).orElse(this.height / 2);
            this.delta$reconnectButton.setPosition(this.width / 2 - 100, maxY + 24);
        }
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void delta$reposition(CallbackInfo ci) {
        if (this.delta$reconnectButton != null) {
            int maxY = children().stream().filter(c -> c instanceof Button && c != this.delta$reconnectButton)
                    .map(c -> ((Button) c).getY()).max(Integer::compareTo).orElse(this.height / 2);
            this.delta$reconnectButton.setPosition(this.width / 2 - 100, maxY + 24);
        }
    }
}
