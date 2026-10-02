package platform.inject.mixin;

import static platform.api.module.Interface.aM_;

import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.event.events.player.PushEvent;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Player.class})
public class PlayerMixin {
    @Inject(method = {"isPushedByFluid"}, at = {@At("HEAD")}, cancellable = true)
    private void isPushedByFluid(CallbackInfoReturnable<Boolean> cir) {
        if (aM_.player != null && ((Player) (Object) this).getUUID().equals(aM_.player.getUUID())) {
            PushEvent event = new PushEvent(PushEvent.a.FLUIDS);
            EventManager.a((IEvent) event);
            if (event.a()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = {"maybeBackOffFromEdge"}, at = {@At("HEAD")}, cancellable = true)
    private void maybeBackOffFromEdge(Vec3 vec3, MoverType moverType, CallbackInfoReturnable<Vec3> cir) {
        if (aM_.player != null && ((Player) (Object) this).getUUID().equals(aM_.player.getUUID())) {
            PushEvent event = new PushEvent(PushEvent.a.BLOCKS);
            EventManager.a((IEvent) event);
            if (event.a()) {
                cir.setReturnValue(vec3);
            }
        }
    }
}
