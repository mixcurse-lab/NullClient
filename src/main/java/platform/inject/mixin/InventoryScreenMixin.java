package platform.inject.mixin;

import static platform.api.module.Interface.aM_;

import platform.client.features.modules.render.Animations;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InventoryScreen.class})
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {

    @Unique
    private Button button;

    public InventoryScreenMixin(InventoryMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    public void init() {
        super.init();
        this.button = addRenderableWidget(Button.builder(Component.literal("\u0412\u044b\u043a\u0438\u043d\u0443\u0442\u044c \u0432\u0441\u0451"), this::onDropAllClick).bounds((this.width - 100) / 2, ((this.height - this.imageHeight) / 2) - 24, 100, 20).build());
    }

    @Inject(method = {"extractRenderState"}, at = {@At("HEAD")})
    private void headRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {

        if (this.button != null) {
            this.button.active = this.menu.slots.stream().anyMatch(slot -> !slot.getItem().isEmpty());
        }
    }

    @Inject(method = {"extractRenderState"}, at = {@At("RETURN")})
    private void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    }

    @Unique
    private void onDropAllClick(Button button) {
        for (Slot slot : this.menu.slots) {
            if (!slot.getItem().isEmpty() && this.button.active) {
                this.slotClicked(slot, slot.index, 1, ContainerInput.THROW);
            }
        }
    }
}

