package platform.inject.mixin;

import static platform.api.module.Interface.aM_;

import platform.client.Delta;
import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.event.events.render.HandAnimationEvent;
import platform.api.event.events.render.HandViewEvent;
import platform.api.handlers.RotationProcessor;
import platform.client.utils.rotation.Look;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @ModifyExpressionValue(method = {"submitHandsWithItems"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot(F)F"))
    private float delta$armPitch(float xRot) {
        return Delta.h().d().k().b() == RotationProcessor.a.IDLE || aM_.player == null ? xRot : Look.c();
    }

    @ModifyExpressionValue(method = {"submitHandsWithItems"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewXRot(F)F"))
    private float delta$handPitch(float viewXRot, @Local(argsOnly = true) float frameInterp) {
        return Delta.h().d().k().b() == RotationProcessor.a.IDLE || aM_.player == null ? viewXRot : Mth.lerp(frameInterp, aM_.player.xBobO, aM_.player.xBob);
    }

    @ModifyExpressionValue(method = {"submitHandsWithItems"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getViewYRot(F)F"))
    private float delta$handYaw(float viewYRot, @Local(argsOnly = true) float frameInterp) {
        return Delta.h().d().k().b() == RotationProcessor.a.IDLE || aM_.player == null ? viewYRot : Mth.lerp(frameInterp, aM_.player.yBobO, aM_.player.yBob);
    }

    @Inject(method = {"submitArmWithItem"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
    private void delta$onSubmitArm(AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack, net.minecraft.world.item.ItemStack itemStack, float inverseArmHeight, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        EventManager.a((IEvent) new HandViewEvent(poseStack, itemStack, hand));
    }

    @WrapOperation(method = {"submitArmWithItem"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;swingArm(FLcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/entity/HumanoidArm;)V", ordinal = 2))
    private void delta$wrapHandAnimation(ItemInHandRenderer instance, float swingProgress, PoseStack matrices, int armX, net.minecraft.world.entity.HumanoidArm arm, Operation<Void> original, @Local(argsOnly = true) InteractionHand hand, @Local(argsOnly = true, index = 7) float inverseArmHeight) {
        matrices.translate(-armX * 0.56f, 0.52f + 0.6f * inverseArmHeight, 0.72f);
        HandAnimationEvent event = new HandAnimationEvent(matrices, hand, swingProgress, armX);
        EventManager.a((IEvent) event);
        if (!event.a()) {
            matrices.translate(armX * 0.56f, -0.52f + inverseArmHeight * -0.6f, -0.72f);
            original.call(instance, swingProgress, matrices, armX, arm);
        }
    }
}
