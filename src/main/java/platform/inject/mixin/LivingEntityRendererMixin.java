package platform.inject.mixin;

import platform.client.Delta;
import platform.client.features.modules.render.EntityESP;
import platform.client.features.modules.render.SeeInvisibles;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import platform.interfaces.SeeInvisiblesMarker;

import static platform.api.module.Interface.aM_;

@Mixin({LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin {
    @Shadow
    public abstract Identifier getTextureLocation(LivingEntityRenderState state);

    @Inject(method = {"extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V"}, at = {@At("TAIL")})
    private void delta$seeInvisibles(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
        SeeInvisibles module = Delta.h().d().t().T();
        if (module == null) {
            return;
        }
        if (!(entity instanceof Player) || entity == aM_.player) {
            return;
        }
        boolean reveal = module.m() && entity.isInvisible();
        ((SeeInvisiblesMarker) state).delta$setSeeInvisRevealed(reveal);
        if (reveal) {
            state.isInvisible = false;
        }
    }

    @Inject(method = "getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;", at = {@At("TAIL")}, cancellable = true)
    private void delta$seeInvisiblesTranslucent(LivingEntityRenderState state, boolean bodyVisible, boolean invisibleToPlayer, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (glowing || cir.getReturnValue() == null || !((SeeInvisiblesMarker) state).delta$isSeeInvisRevealed()) {
            return;
        }
        cir.setReturnValue(RenderTypes.entityTranslucent(this.getTextureLocation(state)));
    }

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = {@At("HEAD")}, cancellable = true)
    private void delta$entityEspHideVanillaName(LivingEntity entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
        if (Delta.h().d().t().entityESP() instanceof EntityESP esp && esp.isVanillaNameHidden(entity)) {
            cir.setReturnValue(Boolean.FALSE);
        }
    }
}
