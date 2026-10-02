package platform.inject.mixin;

import platform.client.Delta;
import platform.client.features.modules.render.EntityESP;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({AvatarRenderer.class})
public abstract class AvatarRendererMixin {
    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Avatar;D)Z", at = {@At("HEAD")}, cancellable = true)
    private void delta$entityEspHideVanillaName(Avatar entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
        if (Delta.h().d().t().entityESP() instanceof EntityESP esp && esp.isVanillaNameHidden(entity)) {
            cir.setReturnValue(Boolean.FALSE);
        }
    }
}
