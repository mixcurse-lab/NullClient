package platform.client.features.modules.render;

import static platform.api.module.Interface.aM_;
import platform.api.module.Category;
import platform.client.Delta;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.other.ContainerEvent;
import platform.api.event.events.render.DrawEvent;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.ProjectUtil;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.joml.Vector2f;
import platform.inject.accessors.HandledScreenAccessor;

@ModuleRegister(a = "Shulker Preview", b = "Показывает содержимое шалкеров в инвентаре зажав ALT (и на земле, без зажатия, если античит слабый)", c = Category.Render)
public class ShulkerPreview extends Module {
    private final Vector2f b = new Vector2f(175.0f, 70.0f);

    @EventTarget
    public void a(ContainerEvent event) {
        Slot hovered;
        if (event.h() == ContainerEvent.Phase.POST && (hovered = ((HandledScreenAccessor) event.b()).getHoveredSlot()) != null && hovered.hasItem()) {
            ItemStack hoveredStack = hovered.getItem();
            if (hoveredStack.get(DataComponents.CONTAINER) != null && hoveredStack.getItem() instanceof BlockItem) {
                BlockItem blockItem = (BlockItem) hoveredStack.getItem();
                if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
                    a(event.d(), hoveredStack, ((ItemContainerContents) hoveredStack.get(DataComponents.CONTAINER)).nonEmptyItemCopyStream().toList(), event.f() + 8, (event.g() - this.b.y()) - 16.0f, 1.0f, true);
                }
            }
        }
    }

    @EventTarget
    public void a(DrawEvent event) {
        ItemStack stack;
        if (event.b()) {
            float tickDelta = event.g();
            for (ItemEntity itemEntity : aM_.level.getEntitiesOfClass(ItemEntity.class, aM_.player.getBoundingBox().inflate(64), entity -> true)) {
                if (itemEntity.getItem().getItem() instanceof BlockItem) {
                    BlockItem blockItem = (BlockItem) itemEntity.getItem().getItem();
                    if ((blockItem.getBlock() instanceof ShulkerBoxBlock) && (stack = itemEntity.getItem()) != null && !stack.isEmpty()) {
                        Vector2f projected = ProjectUtil.a(itemEntity.xo + ((itemEntity.getX() - itemEntity.xo) * (double) tickDelta), itemEntity.yo + ((itemEntity.getY() - itemEntity.yo) * (double) tickDelta) + 0.5d, itemEntity.zo + ((itemEntity.getZ() - itemEntity.zo) * (double) tickDelta));
                        ItemContainerContents container = (ItemContainerContents) stack.get(DataComponents.CONTAINER);
                        if (container != null && !container.nonEmptyItemCopyStream().toList().isEmpty()) {
                            a(event.i(), stack, container.nonEmptyItemCopyStream().toList(), projected.x() - ((this.b.x() * 0.5f) / 2.0f), projected.y() - (this.b.y() * 0.5f), 0.5f, false);
                        }
                    }
                }
            }
        }
    }

    private void a(GuiGraphicsExtractor context, ItemStack itemStack, List<ItemStack> stacks, float x, float y, float scale, boolean overlay) {
        context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath("delta", "pictures/minecraft/3x9.png"), (int) x, (int) y, 0.0f, 0.0f, (int) (this.b.x() * scale), (int) (this.b.y() * scale), 175, 70, 256, 256, ColorUtil.a(((BlockItem) itemStack.getItem()).getBlock().defaultMapColor().col, 255));
        for (int i = 0; i < Math.min(stacks.size(), 27); i++) {
            ItemStack stack = stacks.get(i);
            if (stack != null && !stack.isEmpty()) {
                float slotX = x + ((9 + ((i % 9) * 18) + 1) * scale);
                float slotY = y + ((9 + ((i / 9) * 18) + 1) * scale);
                Delta.h().d().j().a(context, stack, slotX, slotY, 0, 1.0f, 0.75f * scale, overlay);
            }
        }
    }
}


