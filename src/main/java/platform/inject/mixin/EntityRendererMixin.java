package platform.inject.mixin;

import platform.client.Delta;
import platform.client.features.modules.render.EntityESP;
import platform.client.features.modules.render.ShaderESP;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EntityRenderer.class})
public abstract class EntityRendererMixin {
    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Entity;D)Z", at = {@At("HEAD")}, cancellable = true)
    private void delta$entityEspHideVanillaName(Entity entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
        if (Delta.h().d().t().entityESP() instanceof EntityESP esp && esp.isVanillaNameHidden(entity)) {
            cir.setReturnValue(Boolean.FALSE);
        }
    }

    @ModifyExpressionValue(method = {"extractRenderState"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z")})
    private boolean delta$shaderEspGlow(boolean original, @Local(argsOnly = true, index = 0) Entity entity) {
        if (Delta.h().d().t().ad().m() && !entity.isInvisible() && (entity instanceof Player || entity instanceof ItemEntity)) {
            return true;
        }
        return original;
    }

    @ModifyExpressionValue(method = {"extractRenderState"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I")})
    private int delta$shaderEspColor(int original, @Local(argsOnly = true, index = 0) Entity entity) {
        ShaderESP shaderEsp = Delta.h().d().t().ad();
        if (shaderEsp.m()) {
            return shaderEsp.r().c().intValue() & 0xFFFFFF;
        }
        return original;
    }
}
