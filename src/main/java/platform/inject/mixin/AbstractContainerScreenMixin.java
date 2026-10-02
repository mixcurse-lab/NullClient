package platform.inject.mixin;

import platform.client.Delta;
import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.module.Interface;
import platform.api.event.events.other.ContainerEvent;
import platform.client.utils.lib.javassist.TokenId;
import platform.client.utils.inject.ISlot;
import platform.client.features.modules.player.ItemScroller;
import platform.client.features.modules.render.Animations;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Shadow
    @Final
    protected Slot hoveredSlot;

    @Unique private boolean delta$didPush;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void delta$containerPre(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EventManager.a((IEvent) new ContainerEvent((AbstractContainerScreen<?>) (Object) this, graphics, mouseX, mouseY, ContainerEvent.Phase.PRE));
        ItemScroller itemScroller = Delta.h().d().t().itemScroller();
        if (itemScroller != null && itemScroller.m() && this.hoveredSlot != null && this.hoveredSlot.hasItem() && GLFW.glfwGetMouseButton(Interface.aM_.getWindow().handle(), 0) == 1 && GLFW.glfwGetKey(Interface.aM_.getWindow().handle(), TokenId.O_) == 1 && itemScroller.r().a(itemScroller.q().c().intValue())) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
            Interface.aM_.gameMode.handleContainerInput(screen.getMenu().containerId, this.hoveredSlot.index, 0, ContainerInput.QUICK_MOVE, Interface.aM_.player);
            itemScroller.r().b();
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void delta$containerPost(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EventManager.a((IEvent) new ContainerEvent((AbstractContainerScreen<?>) (Object) this, graphics, mouseX, mouseY, ContainerEvent.Phase.POST));
    }

    @Unique
    private boolean delta$isSkull(Slot slot) {
        if (slot == null || !slot.hasItem()) return false;
        var stack = slot.getItem();
        return stack.is(Items.PLAYER_HEAD)
                || stack.is(Items.SKELETON_SKULL)
                || stack.is(Items.WITHER_SKELETON_SKULL)
                || stack.is(Items.ZOMBIE_HEAD)
                || stack.is(Items.CREEPER_HEAD)
                || stack.is(Items.DRAGON_HEAD)
                || stack.is(Items.PIGLIN_HEAD);
    }

    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void delta$slotScaleHead(GuiGraphicsExtractor context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        this.delta$didPush = false;


    }

    @Inject(method = "extractSlot", at = @At("RETURN"))
    private void delta$slotScaleTail(GuiGraphicsExtractor context, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.delta$didPush) {
            context.pose().popMatrix();
            this.delta$didPush = false;
        }
    }
}
