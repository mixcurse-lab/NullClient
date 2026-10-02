package platform.inject.mixin;

import static platform.api.module.Interface.aM_;

import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.event.events.player.KeyEvent;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void delta$keyPress(long windowPointer, int action, net.minecraft.client.input.KeyEvent event, CallbackInfo ci) {
        if (aM_.gui.screen() == null) {
            KeyEvent deltaEvent = new KeyEvent(event.key(), event.scancode(), action, event.modifiers());
            EventManager.a((IEvent) deltaEvent);
            if (deltaEvent.a()) {
                ci.cancel();
            }
        }
    }
}
