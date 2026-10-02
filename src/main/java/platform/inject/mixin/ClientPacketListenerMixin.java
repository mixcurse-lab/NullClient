package platform.inject.mixin;

import platform.client.Delta;
import platform.api.module.Interface;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public abstract class ClientPacketListenerMixin implements Interface {
    @Inject(method = {"sendChat"}, at = {@At("HEAD")}, cancellable = true)
    private void sendChat(String content, CallbackInfo ci) {
        if (!Delta.h().d().t().noCommands().m()) {
            Delta.h().d().u().a(content, ci);
        }
    }
}
