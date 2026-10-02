package platform.api.event.events.render;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import lombok.Generated;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CrosshairEvent extends Event implements IEvent {
    private final GuiGraphicsExtractor a;
    private final float b;

    @Generated
    public GuiGraphicsExtractor b() {
        return this.a;
    }

    @Generated
    public float c() {
        return this.b;
    }

    public CrosshairEvent(GuiGraphicsExtractor context, float partialTicks) {
        this.a = context;
        this.b = partialTicks;
    }
}



