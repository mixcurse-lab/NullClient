package platform.inject.mixin;

import platform.api.event.events.render.DrawEvent;
import platform.client.Delta;
import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.module.Interface;
import platform.api.event.events.player.HandEvent;
import platform.api.event.events.other.RatioEvent;
import platform.api.event.events.render.RemovalsEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import platform.client.utils.render.LevelProjection;
@Mixin({GameRenderer.class})
public class GameRendererMixin implements Interface {
    @ModifyArg(method = {"renderLevel"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setProjectionMatrix(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/ProjectionType;)V", ordinal = 0))
    private GpuBufferSlice delta$captureLevelProjection(GpuBufferSlice slice) {
        LevelProjection.set(slice);
        return slice;
    }

    @Inject(method = {"render"}, at = @At("TAIL"))
    private void delta$drawScreenSpaceBatch(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        Delta.h().d().l().a();
    }

    @Inject(method = {"bobHurt"}, at = {@At("HEAD")}, cancellable = true)
    private void delta$removalsHurtCam(CallbackInfo ci) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.HURT_CAM);
        EventManager.a((IEvent) event);
        if (event.a()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = {"renderLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getEffectBlendFactor(Lnet/minecraft/core/Holder;F)F"))
    private float delta$removalsNausea(float original) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.NAUSEA);
        EventManager.a((IEvent) event);
        if (event.a()) {
            return 0.0f;
        }
        return original;
    }

    @Inject(method = {"renderLevel"}, at = {@At("TAIL")})
    private void onRenderLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        PoseStack matrixStack = new PoseStack();
        float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
        EventManager.a((IEvent) new DrawEvent(matrixStack, tickDelta, DrawEvent.a.D3D));
    }

    @ModifyArg(method = {"renderLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Projection;setupPerspective(FFFFF)V"), index = 3)
    private float delta$aspectRatioHud(float width) {
        RatioEvent event = new RatioEvent(width / Minecraft.getInstance().getWindow().getHeight());
        EventManager.a((IEvent) event);
        return Minecraft.getInstance().getWindow().getHeight() * event.b();
    }

    @Inject(method = {"renderLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V"))
    private void delta$preRenderHand(DeltaTracker deltaTracker, CallbackInfo ci) {
        EventManager.a((IEvent) new HandEvent(HandEvent.a.PRE));
    }

    @Inject(method = {"renderLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V", shift = At.Shift.AFTER))
    private void delta$postRenderHand(DeltaTracker deltaTracker, CallbackInfo ci) {
        EventManager.a((IEvent) new HandEvent(HandEvent.a.POST));
    }
}

