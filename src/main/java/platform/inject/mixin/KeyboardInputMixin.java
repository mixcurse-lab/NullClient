package platform.inject.mixin;

import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.event.events.player.InputEvent;
import platform.client.utils.player.MoveUtil;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import platform.inject.accessors.ClientInputAccessor;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
    @Inject(method = {"tick"}, at = {@At("RETURN")})
    private void delta$onInputTick(CallbackInfo ci) {
        KeyboardInput input = (KeyboardInput) (Object) this;
        Input keys = input.keyPresses;
        float forward = (keys.forward() ? 1.0f : 0.0f) - (keys.backward() ? 1.0f : 0.0f);
        float strafe = (keys.left() ? 1.0f : 0.0f) - (keys.right() ? 1.0f : 0.0f);
        InputEvent event = new InputEvent(forward, strafe, keys.jump(), keys.shift());
        EventManager.a((IEvent) event);
        MoveUtil.a(event);
        input.keyPresses = new Input(
                event.b() > 0.0f, event.b() < 0.0f,
                event.c() > 0.0f, event.c() < 0.0f,
                event.d(), event.e(),
                keys.sprint()
        );
        ((ClientInputAccessor) input).setMoveVector(new Vec2(event.c(), event.b()).normalized());
    }
}
