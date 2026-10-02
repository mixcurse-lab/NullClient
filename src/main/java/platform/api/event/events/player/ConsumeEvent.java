package platform.api.event.events.player;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.world.item.ItemStack;

public class ConsumeEvent extends Event implements IEvent {
    private final ItemStack a;

    @Generated
    public ConsumeEvent(ItemStack stack) {
        this.a = stack;
    }

    @Generated
    public ItemStack b() {
        return this.a;
    }
}



