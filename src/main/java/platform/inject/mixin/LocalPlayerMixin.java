package platform.inject.mixin;

import platform.client.Delta;
import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.module.Interface;
import platform.api.event.events.player.DropItemEvent;
import platform.api.event.events.player.MotionEvent;
import platform.api.event.events.player.SlowEvent;
import platform.api.event.events.client.TickEvent;
import platform.client.features.modules.movement.NoPush;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Inject(method = {"tick"}, at = {@At("HEAD")})
    private void delta$tick(CallbackInfo ci) {
        EventManager.a((IEvent) new TickEvent());
    }

    @Inject(method = {"sendPosition"}, at = {@At("HEAD")})
    private void delta$onSendPosition(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        EventManager.a((IEvent) new MotionEvent(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.onGround(), player.isShiftKeyDown(), player.isSprinting()));
    }

    @ModifyExpressionValue(method = {"modifyInput"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean delta$noSlow(boolean original) {
        if (!original) {
            return false;
        }
        SlowEvent event = new SlowEvent();
        EventManager.a((IEvent) event);
        return !event.a();
    }

    @Inject(method = {"moveTowardsClosestSpace"}, at = {@At("HEAD")}, cancellable = true)
    private void noPushBlocksSkipNudge(CallbackInfo ci) {
        NoPush noPush = (NoPush) Delta.h().d().t().O();
        if (noPush != null && noPush.m() && noPush.q().a("Блоков").c().booleanValue()) {
            ci.cancel();
        }
    }

    @Inject(method = {"drop"}, at = {@At("HEAD")}, cancellable = true)
    private void delta$onDrop(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        DropItemEvent dropItemEvent = new DropItemEvent(Interface.aM_.player.getInventory().getSelectedSlot());
        EventManager.a((IEvent) dropItemEvent);
        if (dropItemEvent.a()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
