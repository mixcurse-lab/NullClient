package platform.api.event.events.render;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.world.InteractionHand;
import com.mojang.blaze3d.vertex.PoseStack;

public class HandAnimationEvent extends Event implements IEvent {
    private final PoseStack a;
    private final InteractionHand b;
    private final float c;
    private final int d;

    @Generated
    public HandAnimationEvent(PoseStack matrices, InteractionHand hand, float swingProgress, int armX) {
        this.a = matrices;
        this.b = hand;
        this.c = swingProgress;
        this.d = armX;
    }

    @Generated
    public PoseStack b() {
        return this.a;
    }

    @Generated
    public InteractionHand c() {
        return this.b;
    }

    @Generated
    public float d() {
        return this.c;
    }

    @Generated
    public int e() {
        return this.d;
    }
}



