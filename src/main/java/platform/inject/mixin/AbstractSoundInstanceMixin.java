package platform.inject.mixin;

import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.event.events.client.SoundEvent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({AbstractSoundInstance.class})
public abstract class AbstractSoundInstanceMixin {
    @ModifyReturnValue(method = {"getVolume"}, at = {@At("RETURN")})
    private float getVolume(float original) {
        SoundEvent event = new SoundEvent((SoundInstance) this, original);
        EventManager.a((IEvent) event);
        return event.c();
    }
}

