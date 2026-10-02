package platform.inject.mixin;


import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.event.events.player.PushEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({FishingHook.class})
public class FishingHookMixin {
    @Inject(method = {"pullEntity"}, at = {@At("HEAD")}, cancellable = true)
    private void pullHookedEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof LocalPlayer) {
            PushEvent event = new PushEvent(PushEvent.a.FISHING_HOOK);
            EventManager.a((IEvent) event);
            if (event.a()) {
                ci.cancel();
            }
        }
    }
}

