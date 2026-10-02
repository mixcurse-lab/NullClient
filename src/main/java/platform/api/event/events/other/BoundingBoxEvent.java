package platform.api.event.events.other;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class BoundingBoxEvent extends Event implements IEvent {
    public AABB a;
    public Entity b;

    @Generated
    public void a(AABB box) {
        this.a = box;
    }

    @Generated
    public void a(Entity entity) {
        this.b = entity;
    }

    @Generated
    public BoundingBoxEvent(AABB box, Entity entity) {
        this.a = box;
        this.b = entity;
    }

    @Generated
    public AABB b() {
        return this.a;
    }

    @Generated
    public Entity c() {
        return this.b;
    }
}



