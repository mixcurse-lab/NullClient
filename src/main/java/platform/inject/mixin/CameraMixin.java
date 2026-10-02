package platform.inject.mixin;

import platform.api.event.events.client.CameraPositionEvent;
import platform.client.Delta;
import platform.api.event.EventManager;
import platform.api.module.Interface;
import platform.api.event.interfaces.IEvent;
import platform.api.module.Module;
import platform.api.event.events.other.RatioEvent;
import platform.api.event.events.render.RemovalsEvent;
import platform.api.event.events.player.RotationEvent;
import platform.client.features.modules.movement.NoPush;
import platform.client.features.modules.render.Animations;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import platform.inject.accessors.CameraAccessor;

@Mixin({Camera.class})
public abstract class CameraMixin {
    @Unique
    private float delta$cachedPitch;
    @Unique
    private boolean delta$hasCachedPitch;

    @ModifyReturnValue(method = {"isDetached"}, at = {@At("RETURN")})
    private boolean isThirdPerson(boolean original) {
        Module m = Delta.h().d().t().h();
        if (m != null && m.m()) {
            return true;
        }
        return original;
    }

    @WrapOperation(method = {"alignWithEntity"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F")})
    private float delta$wrapViewYaw(Entity entity, float partialTicks, Operation<Float> original) {
        RotationEvent event = new RotationEvent(original.call(entity, partialTicks), entity.getViewXRot(partialTicks));
        EventManager.a((IEvent) event);
        this.delta$cachedPitch = event.b;
        this.delta$hasCachedPitch = true;
        return event.a;
    }

    @WrapOperation(method = {"alignWithEntity"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F")})
    private float delta$wrapViewPitch(Entity entity, float partialTicks, Operation<Float> original) {
        if (this.delta$hasCachedPitch) {
            this.delta$hasCachedPitch = false;
            return this.delta$cachedPitch;
        }
        return original.call(entity, partialTicks);
    }

    @ModifyArg(method = {"update"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setupPerspective(FFFFF)V"), index = 3)
    private float delta$aspectRatioWidth(float width) {
        RatioEvent event = new RatioEvent(width / Minecraft.getInstance().getWindow().getHeight());
        EventManager.a((IEvent) event);
        return Minecraft.getInstance().getWindow().getHeight() * event.b();
    }

    @ModifyArg(method = {"createProjectionMatrixForCulling"}, at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;perspective(FFFFZ)Lorg/joml/Matrix4f;"), index = 1)
    private float delta$aspectRatioCulling(float aspect) {
        RatioEvent event = new RatioEvent(aspect);
        EventManager.a((IEvent) event);
        return event.b();
    }

    @Inject(method = {"update"}, at = {@At("RETURN")})
    private void onUpdate(DeltaTracker deltaTracker, CallbackInfo ci) {
        Camera camera = (Camera)(Object) this;
        Entity focusedEntity = camera.entity();
        if (focusedEntity == null) return;
        CameraPositionEvent posEvent = new CameraPositionEvent(camera.position());
        EventManager.a((IEvent) posEvent);
        if (posEvent.a() && posEvent.b() != null) {
            Vec3 pos = posEvent.b();
            ((CameraAccessor) this).invokeSetPos(pos.x, pos.y, pos.z);
        }
        NoPush noPush = (NoPush) Delta.h().d().t().O();
        if (noPush != null && noPush.m() && noPush.q().a("Блоков").c().booleanValue()) {
            if (Interface.aM_.options.getCameraType() == CameraType.FIRST_PERSON) {
                Level level = focusedEntity.level();
                Vec3 eye = focusedEntity.getEyePosition(camera.getCameraEntityPartialTicks(deltaTracker));
                double targetY = eye.y;
                BlockPos eyeBlock = BlockPos.containing(eye);
                if (!level.getBlockState(eyeBlock).isViewBlocking(level, eyeBlock)) {
                    Vec3 bodyPoint = focusedEntity.position().add(0.0D, focusedEntity.getBbHeight() / 2.0D, 0.0D);
                    BlockPos bodyBlock = BlockPos.containing(bodyPoint);
                    if (level.getBlockState(bodyBlock).isViewBlocking(level, bodyBlock)) {
                        targetY = bodyBlock.getY() + 0.5D;
                    }
                }
                Vec3 current = camera.position();
                double movedY = current.y + (targetY - current.y) * 0.35D;
                ((CameraAccessor) this).invokeSetPos(current.x, movedY, current.z);
            }
        }
    }

    @Inject(method = {"getFluidInCamera"}, at = {@At("HEAD")}, cancellable = true)
    private void getSubmergedFluidState(CallbackInfoReturnable<FogType> ci) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.WATER);
        EventManager.a((IEvent) event);
        if (event.a()) {
            ci.setReturnValue(FogType.NONE);
        }
    }

    @Inject(method = {"getMaxZoom"}, at = {@At("HEAD")}, cancellable = true)
    private void delta$removalsClip(float desiredCameraDistance, CallbackInfoReturnable<Float> cir) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.CLIP);
        EventManager.a((IEvent) event);
        Animations animations = Delta.h().d().t().Q();
        if (animations.m()) {
            desiredCameraDistance *= animations.u().c();
        }
        if (event.a() || animations.m()) {
            cir.setReturnValue(desiredCameraDistance);
        }
    }
}

