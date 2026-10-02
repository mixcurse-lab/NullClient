package platform.api.event.events.render;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;

public class HandViewEvent extends Event implements IEvent {
    private final PoseStack a;
    private final ItemStack b;
    private final InteractionHand c;

    @Generated
    public HandViewEvent(PoseStack matrices, ItemStack stack, InteractionHand hand) {
        this.a = matrices;
        this.b = stack;
        this.c = hand;
    }

    @Generated
    public PoseStack b() {
        return this.a;
    }

    @Generated
    public ItemStack c() {
        return this.b;
    }

    @Generated
    public InteractionHand d() {
        return this.c;
    }
}



