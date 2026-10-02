package platform.api.event.events.client;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.client.resources.sounds.SoundInstance;

public class SoundEvent extends Event implements IEvent {
    private final SoundInstance a;
    private float b;

    @Generated
    public SoundInstance b() {
        return this.a;
    }

    @Generated
    public void a(float volume) {
        this.b = volume;
    }

    @Generated
    public float c() {
        return this.b;
    }

    public SoundEvent(SoundInstance sound, float volume) {
        this.a = sound;
        this.b = volume;
    }
}



