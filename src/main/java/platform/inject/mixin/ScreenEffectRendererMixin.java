package platform.inject.mixin;

import platform.client.Delta;
import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.event.events.render.RemovalsEvent;
import platform.client.features.modules.movement.NoPush;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {
    @ModifyExpressionValue(method = {"submit"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;getViewBlockingState(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;")})
    private BlockState noPushBlocksHideInWallOverlay(BlockState original) {
        NoPush noPush = (NoPush) Delta.h().d().t().O();
        if (noPush != null && noPush.m() && noPush.q().a("Блоков").c().booleanValue()) {
            return null;
        }
        return original;
    }

    @ModifyExpressionValue(method = {"submit"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;getViewBlockingState(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;")})
    private BlockState delta$removalsPumpkin(BlockState original) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.PUMPKIN);
        EventManager.a((IEvent) event);
        if (event.a()) {
            return null;
        }
        return original;
    }

    @ModifyExpressionValue(method = {"submit"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")})
    private boolean delta$removalsWater(boolean original) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.WATER);
        EventManager.a((IEvent) event);
        if (event.a()) {
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(method = {"submit"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isOnFire()Z")})
    private boolean delta$removalsFire(boolean original) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.FIRE);
        EventManager.a((IEvent) event);
        if (event.a()) {
            return false;
        }
        return original;
    }
}
