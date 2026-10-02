package platform.api.event.events.client;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.world.phys.Vec3;

public class CameraPositionEvent extends Event implements IEvent {
    private Vec3 a;

    @Generated
    public void a(Vec3 position) {
        this.a = position;
    }

    @Generated
    public Vec3 b() {
        return this.a;
    }

    public CameraPositionEvent(Vec3 position) {
        this.a = position;
    }
}



