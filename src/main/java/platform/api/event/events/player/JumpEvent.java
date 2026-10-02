package platform.api.event.events.player;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.world.entity.LivingEntity;

public class JumpEvent extends Event implements IEvent {
    private final LivingEntity a;

    @Generated
    public JumpEvent(LivingEntity livingEntity) {
        this.a = livingEntity;
    }

    @Generated
    public LivingEntity b() {
        return this.a;
    }
}



