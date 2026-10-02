package platform.api.event.events.render;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import java.util.List;
import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public class TooltipEvent extends Event implements IEvent {
    private final ItemStack a;
    private final List<Component> b;

    @Generated
    public ItemStack b() {
        return this.a;
    }

    @Generated
    public List<Component> c() {
        return this.b;
    }

    public TooltipEvent(ItemStack stack, List<Component> lines) {
        this.a = stack;
        this.b = lines;
    }
}



