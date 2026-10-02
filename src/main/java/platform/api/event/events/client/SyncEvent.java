package platform.api.event.events.client;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.world.item.ItemStack;

public class SyncEvent extends Event implements IEvent {
    private final int a;
    private ItemStack b;

    @Generated
    public void a(ItemStack stack) {
        this.b = stack;
    }

    @Generated
    public int b() {
        return this.a;
    }

    @Generated
    public ItemStack c() {
        return this.b;
    }

    public SyncEvent(int slot, ItemStack stack) {
        this.a = slot;
        this.b = stack;
    }
}



