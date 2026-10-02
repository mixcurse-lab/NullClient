package platform.inject.mixin;

import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.event.events.render.GammaEvent;
import platform.api.event.events.render.RemovalsEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({LightmapRenderStateExtractor.class})
public class LightmapRenderStateExtractorMixin {
    @WrapOperation(method = {"extract"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 1)})
    private Object delta$onGetGamma(OptionInstance<?> option, Operation<Object> original) {
        GammaEvent event = new GammaEvent(((Number) original.call(option)).doubleValue());
        EventManager.a((IEvent) event);
        return Double.valueOf(event.b());
    }

    @ModifyReturnValue(method = {"calculateDarknessScale"}, at = {@At("RETURN")})
    private float delta$onDarknessScale(float original) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.DARKNESS);
        EventManager.a((IEvent) event);
        if (event.a()) {
            return 0.0f;
        }
        return original;
    }
}
