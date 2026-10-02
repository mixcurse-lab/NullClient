package platform.inject.mixin;

import static platform.api.module.Interface.aM_;

import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.module.Interface;
import platform.api.event.events.player.ClickEvent;
import platform.api.event.events.player.KeyEvent;
import platform.api.event.events.player.LookEvent;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MouseHandler.class})
public abstract class MouseHandlerMixin {

    @Inject(method = {"lambda$setup$3"}, at = {@At("HEAD")}, cancellable = true)
    private void onMouseButton(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        int button = info.button();
        int modifiers = info.modifiers();
        if (aM_.gui.screen() == null || aM_.gui.screen() instanceof platform.client.ui.screen.GUIScreen) {
            EventManager.a((IEvent) new KeyEvent((button < 0 || button > 7) ? button : (-100) + button, 0, action, modifiers));
        }
        if (action == 1) {
            ClickEvent event = new ClickEvent(aM_.mouseHandler.getScaledXPos(aM_.getWindow()), aM_.mouseHandler.getScaledYPos(aM_.getWindow()), button, ClickEvent.a.PRESS);
            EventManager.a((IEvent) event);
            if (event.a()) {
                ci.cancel();
                return;
            }
            return;
        }
        if (action == 0) {
            ClickEvent event2 = new ClickEvent(aM_.mouseHandler.getScaledXPos(aM_.getWindow()), aM_.mouseHandler.getScaledYPos(aM_.getWindow()), button, ClickEvent.a.RELEASE);
            EventManager.a((IEvent) event2);
            if (event2.a()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = {"onScroll"}, at = {@At("HEAD")})
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (vertical == 0.0) {
            return;
        }
        int key = vertical > 0.0 ? -101 : -102;
        if (aM_.gui.screen() instanceof platform.client.ui.screen.GUIScreen gui) {
            gui.captureMouseBind(key);
            return;
        }
        if (aM_.gui.screen() != null) {
            return;
        }
        EventManager.a((IEvent) new KeyEvent(key, 0, 1, 0));
        EventManager.a((IEvent) new KeyEvent(key, 0, 0, 0));
    }

    @Inject(method = {"onMove"}, at = {@At("HEAD")}, cancellable = true)
    private void onCursorPos(long window, double x, double y, CallbackInfo ci) {
        ClickEvent event = new ClickEvent(aM_.mouseHandler.getScaledXPos(aM_.getWindow()), aM_.mouseHandler.getScaledYPos(aM_.getWindow()), 0, ClickEvent.a.DRAG);
        EventManager.a((IEvent) event);
        if (event.a()) {
            ci.cancel();
        }
    }

    @Redirect(method = {"turnPlayer"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void redirectChangeLookDirection(LocalPlayer player, double yaw, double pitch) {
        LookEvent event = new LookEvent(yaw, pitch);
        EventManager.a((IEvent) event);
        if (!event.a()) {
            player.turn(yaw, pitch);
        }
    }
}
