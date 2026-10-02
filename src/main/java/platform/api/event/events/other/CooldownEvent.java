package platform.api.event.events.other;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.world.item.Item;

public class CooldownEvent extends Event implements IEvent {
    private final Item a;
    private final int b;

    @Generated
    public Item b() {
        return this.a;
    }

    @Generated
    public int c() {
        return this.b;
    }

    public CooldownEvent(Item item, int cooldown) {
        this.a = item;
        this.b = cooldown;
    }
}



