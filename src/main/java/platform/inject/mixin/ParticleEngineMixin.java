package platform.inject.mixin;

import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.event.events.render.RemovalsEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void delta$removalsParticles(ParticleOptions options, double x, double y, double z, double xa, double ya, double za, CallbackInfoReturnable<Particle> cir) {
        if (options instanceof BlockParticleOption) {
            RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.BREAK_PARTICLES);
            EventManager.a((IEvent) event);
            if (event.a()) {
                cir.setReturnValue(null);
                return;
            }
        }
        if (options.getType() == ParticleTypes.RAIN) {
            RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.WEATHER);
            EventManager.a((IEvent) event);
            if (event.a()) {
                cir.setReturnValue(null);
            }
        }
    }
}
