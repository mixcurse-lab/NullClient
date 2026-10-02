package platform.inject.mixin;

import platform.client.Delta;
import platform.client.features.modules.render.ItemPhysic;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemEntityRenderer.class})
public class ItemEntityRendererMixin {
    @Unique
    private final Map<ItemEntityRenderState, Boolean> delta$onGround = new WeakHashMap<>();

    @Inject(method = {"extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V"}, at = {@At("TAIL")})
    private void captureGround(ItemEntity entity, ItemEntityRenderState state, float partialTicks, CallbackInfo ci) {
        this.delta$onGround.put(state, entity.onGround());
    }

    @ModifyArg(method = {"submit"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"), index = 1)
    private float cancelBob(float original) {
        ItemPhysic itemPhysic = Delta.h().d().t().ag();
        if (itemPhysic != null && itemPhysic.m()) {
            return 0.0f;
        }
        return original;
    }

    @ModifyExpressionValue(method = {"submit"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;getSpin(FF)F"))
    private float cancelSpin(float original) {
        ItemPhysic itemPhysic = Delta.h().d().t().ag();
        if (itemPhysic != null && itemPhysic.m()) {
            return 0.0f;
        }
        return original;
    }

    @Inject(method = {"submit"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;submitMultipleFromCount(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ItemClusterRenderState;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/phys/AABB;)V", shift = At.Shift.BEFORE)})
    private void applyPhysics(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        ItemPhysic itemPhysic = Delta.h().d().t().ag();
        if (itemPhysic == null || !itemPhysic.m()) {
            return;
        }
        if (itemPhysic.q().c().booleanValue()) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
        Boolean onGround = this.delta$onGround.get(state);
        if (Boolean.TRUE.equals(onGround)) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(state.ageInTicks * 30.0f % 360.0f));
        }
    }
}
