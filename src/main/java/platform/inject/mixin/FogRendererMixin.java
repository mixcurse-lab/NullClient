package platform.inject.mixin;

import platform.client.features.modules.render.AtmoDawnFog;
import platform.client.features.modules.render.ShaderSky;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.world.level.material.FogType;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class FogRendererMixin {


    @Inject(method = "setupFog", at = @At("RETURN"), cancellable = true)
    private void delta$atmoFog(
            Camera camera,
            int viewDistance,
            DeltaTracker deltaTracker,
            float darkenWorldAmount,
            ClientLevel level,
            CallbackInfoReturnable<FogData> cir) {

        boolean skyActive = ShaderSky.check() || AtmoDawnFog.check();
        if (!skyActive) return;
        if (camera.getFluidInCamera() != FogType.NONE) return;
        FogData data = cir.getReturnValue();
        if (data == null) return;


        float far = 1_000_000.0f;
        data.environmentalStart = far;
        data.environmentalEnd = far;
        data.renderDistanceStart = far;
        data.renderDistanceEnd = far;
        data.skyEnd = far;
        data.cloudEnd = far;

        cir.setReturnValue(data);
    }
}
