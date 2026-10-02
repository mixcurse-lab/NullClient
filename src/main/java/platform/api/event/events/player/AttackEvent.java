package platform.api.event.events.player;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.world.entity.Entity;

public class AttackEvent extends Event implements IEvent {
    private final Entity a;

    @Generated
    public Entity b() {
        return this.a;
    }

    public AttackEvent(Entity entity) {
        this.a = entity;
    }
}



