package platform.api.event.events.other;

import platform.api.event.Event;
import platform.api.event.interfaces.IEvent;

import java.util.List;
import lombok.Generated;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class ContainerEvent extends Event implements IEvent {
    public enum Phase {
        PRE,
        POST,
        TITLE
    }

    private final AbstractContainerScreen<?> screen;
    private final AbstractContainerMenu handler;
    private final GuiGraphicsExtractor context;
    private final List<Slot> slots;
    private final int mouseX;
    private final int mouseY;
    private final Phase phase;
    private Component title;

    @Generated
    public AbstractContainerScreen<?> b() {
        return this.screen;
    }

    @Generated
    public AbstractContainerMenu c() {
        return this.handler;
    }

    @Generated
    public GuiGraphicsExtractor d() {
        return this.context;
    }

    @Generated
    public List<Slot> e() {
        return this.slots;
    }

    @Generated
    public int f() {
        return this.mouseX;
    }

    @Generated
    public int g() {
        return this.mouseY;
    }

    @Generated
    public Phase h() {
        return this.phase;
    }

    @Generated
    public void a(Component title) {
        this.title = title;
    }

    @Generated
    public Component i() {
        return this.title;
    }

    public ContainerEvent(AbstractContainerScreen<?> screen, GuiGraphicsExtractor context, int mouseX, int mouseY, Phase type) {
        this.screen = screen;
        this.handler = screen.getMenu();
        this.slots = screen.getMenu().slots;
        this.context = context;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.phase = type;
        this.title = screen.getTitle();
    }

    public ContainerEvent(AbstractContainerScreen<?> screen, Component title) {
        this.screen = screen;
        this.handler = screen.getMenu();
        this.slots = screen.getMenu().slots;
        this.context = null;
        this.mouseX = 0;
        this.mouseY = 0;
        this.phase = Phase.TITLE;
        this.title = title;
    }
}



