package platform.inject.mixin;

import platform.client.Delta;
import platform.api.handlers.RotationProcessor;
import platform.client.utils.rotation.Look;
import platform.client.utils.rotation.Rotation;
import baritone.api.BaritoneAPI;
import baritone.behavior.LookBehavior;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = LookBehavior.class, remap = false)
public abstract class BaritoneLookMixin {

    @Unique private boolean delta$wasSilentBeforeBaritone;
    @Unique private float delta$pendingYaw;
    @Unique private boolean delta$hasPendingYaw;

    @Unique
    private Object delta$getTarget() {
        try {

            for (java.lang.reflect.Field f : LookBehavior.class.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals("Target") || f.getName().equals("target")) {
                    f.setAccessible(true);
                    return f.get(this);
                }
            }
            java.lang.reflect.Field f = LookBehavior.class.getDeclaredField("target");
            f.setAccessible(true);
            return f.get(this);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Unique
    private boolean delta$shouldHide() {

        try {
            return !BaritoneAPI.getSettings().freeLook.value;
        } catch (Throwable ignored) {
            return true;
        }
    }

    @WrapOperation(method = "onPlayerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setYRot(F)V"))
    private void delta$wrapYaw(LocalPlayer instance, float yaw, Operation<Void> original) {
        Object tgt = delta$getTarget();
        if (tgt == null || !delta$shouldHide()) {
            original.call(instance, yaw);
            return;
        }

        try {
            Look look = Delta.h().d().k().a();
            if (look != null && !look.a()) {
                delta$wasSilentBeforeBaritone = false;
            } else {
                delta$wasSilentBeforeBaritone = true;
            }
        } catch (Throwable ignored) {}
        delta$pendingYaw = yaw;
        delta$hasPendingYaw = true;

    }

    @WrapOperation(method = "onPlayerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setXRot(F)V"))
    private void delta$wrapPitch(LocalPlayer instance, float pitch, Operation<Void> original) {
        Object tgt = delta$getTarget();
        if (tgt == null || !delta$shouldHide()) {
            original.call(instance, pitch);
            return;
        }
        if (delta$hasPendingYaw) {
            float yaw = delta$pendingYaw;
            delta$hasPendingYaw = false;
            try {

                Delta.h().d().k().a(new Rotation(yaw, pitch), 180.0f, 1, 1);
            } catch (Throwable ignored) {
                original.call(instance, pitch);

                try { instance.setYRot(yaw); } catch (Throwable ignored2) {}
            }
        } else {

            try {
                Delta.h().d().k().a(new Rotation(instance.getYRot(), pitch), 180.0f, 1, 1);
            } catch (Throwable ignored) {
                original.call(instance, pitch);
            }
        }
    }

    @WrapOperation(method = "pig", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setYRot(F)V"), remap = false)
    private void delta$wrapPigYaw(LocalPlayer instance, float yaw, Operation<Void> original) {
        Object tgt = delta$getTarget();
        if (tgt == null || !delta$shouldHide()) {
            original.call(instance, yaw);
            return;
        }
        try {
            Delta.h().d().k().a(new Rotation(yaw, instance.getXRot()), 180.0f, 1, 1);
        } catch (Throwable ignored) {
            original.call(instance, yaw);
        }
    }

    @Inject(method = "onPlayerUpdate", at = @At("TAIL"), remap = false)
    private void delta$onTail(baritone.api.event.events.PlayerUpdateEvent event, CallbackInfo ci) {
        if (delta$getTarget() != null) return;

        try {
            Look look = Delta.h().d().k().a();
            if (look == null || !look.a()) return;
            if (RotationProcessor.b() != RotationProcessor.a.IDLE) return;
            if (!delta$wasSilentBeforeBaritone) {
                look.a(false);
            }
        } catch (Throwable ignored) {}
    }
}
