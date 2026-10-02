package platform.api.event.events.render;

import platform.client.Delta;
import platform.api.event.interfaces.IEvent;
import platform.api.event.Event;
import platform.api.module.Interface;
import platform.client.utils.render.Draw2DProcessor;
import platform.client.utils.render.Draw3DProcessor;
import lombok.Generated;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class DrawEvent extends Event implements Interface, IEvent {
    private final Draw2DProcessor b = Delta.h().d().i();
    private final Draw3DProcessor c = Delta.h().d().j();
    private final a d;
    private final float e;
    private final PoseStack f;
    private GuiGraphicsExtractor g;

    public enum a {
        D2D,
        D3D
    }

    @Generated
    public Draw2DProcessor d() { return this.b; }

    @Generated
    public Draw3DProcessor e() { return this.c; }

    @Generated
    public a f() { return this.d; }

    @Generated
    public float g() { return this.e; }

    @Generated
    public PoseStack h() { return this.f; }

    @Generated
    public GuiGraphicsExtractor i() { return this.g; }

    public DrawEvent(PoseStack stack, float tickDelta, a type) {
        this.f = stack;
        this.e = tickDelta;
        this.d = type;
    }

    public DrawEvent(GuiGraphicsExtractor context, float tickDelta, a type) {
        this.g = context;
        this.f = new PoseStack();
        this.e = tickDelta;
        this.d = type;
    }

    public boolean b() { return this.d == a.D2D; }
    public boolean c() { return this.d == a.D3D; }
}



