package platform.inject.mixin;

import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.client.utils.inject.ILivingEntity;
import platform.api.event.events.player.JumpEvent;
import platform.api.event.events.player.PushEvent;
import platform.api.event.events.player.WillLandEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import platform.inject.invokers.EntityMovementInvoker;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin implements ILivingEntity {
    @Inject(method = {"jumpFromGround"}, at = {@At("HEAD")}, cancellable = true)
    public void jump(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity)(Object) this;
        if (livingEntity instanceof LocalPlayer) {
            JumpEvent event = new JumpEvent(livingEntity);
            EventManager.a((IEvent) event);
            if (event.a()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = {"travel"}, at = {@At("TAIL")})
    private void travel(Vec3 movementInput, CallbackInfo ci) {
        EntityMovementInvoker entityMovementInvoker = (EntityMovementInvoker)(Object) this;
        if (entityMovementInvoker instanceof LocalPlayer) {
            LivingEntity livingEntity = (LivingEntity)(Object) this;
            if (!livingEntity.isInWater() && !livingEntity.isInLava() && !livingEntity.isFallFlying()) {
                double gravity = livingEntity.getGravity();
                double predictedNextYDelta = (((Entity)(Object) this).getDeltaMovement().y - gravity) * 0.98d;
                Vec3 verticalAttempt = new Vec3(0.0d, predictedNextYDelta, 0.0d);
                Vec3 allowedVertical = entityMovementInvoker.getAdjustMovementForCollisions(verticalAttempt);
                boolean willLand = predictedNextYDelta < 0.0d && allowedVertical.y != predictedNextYDelta;
                EventManager.a((IEvent) new WillLandEvent(willLand));
            }
        }
    }

    @Inject(method = {"isPushable"}, at = {@At("HEAD")}, cancellable = true)
    private void removePushFromEntity(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object) this;
        if (entity instanceof LocalPlayer) {
            PushEvent event = new PushEvent(PushEvent.a.ENTITIES);
            EventManager.a((IEvent) event);
            if (event.a()) {
                cir.setReturnValue(false);
            }
        }
    }
}

