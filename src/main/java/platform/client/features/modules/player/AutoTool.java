package platform.client.features.modules.player;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;
import platform.api.handlers.InventoryHandler;

import java.util.Comparator;
import java.util.stream.IntStream;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.phys.BlockHitResult;

@ModuleRegister(a = "Auto Tool", b = "Автоматически выбирает подходящий инструмент для блока", c = Category.Player)
public class AutoTool extends Module implements Interface {
    private final int[] b = {-1, -1};

    @EventTarget
    public void a(TickEvent event) {
        int bestSlot;
        InventoryHandler handler = Delta.h().d().v().a();
        if (handler.a().isEmpty()) {
            if (aM_.hitResult instanceof BlockHitResult class_3965Var) {
                BlockHitResult hit = class_3965Var;
                if (aM_.options.keyAttack.isDown()) {
                    if (this.b[0] != -1 || (bestSlot = a(aM_.level.getBlockState(hit.getBlockPos()))) == -1) {
                        return;
                    }
                    this.b[0] = aM_.player.getInventory().getSelectedSlot();
                    if (bestSlot > 8) {
                        this.b[1] = bestSlot;
                        handler.a(bestSlot, this.b[0], 1);
                        return;
                    } else {
                        aM_.player.getInventory().setSelectedSlot(bestSlot);
                        return;
                    }
                }
            }
            if (this.b[0] == -1) {
                return;
            }
            if (this.b[1] == -1) {
                aM_.player.getInventory().setSelectedSlot(this.b[0]);
            } else {
                handler.a(this.b[1], this.b[0], 1);
            }
            this.b[0] = -1;
            this.b[1] = -1;
        }
    }

    private int a(BlockState state) {
        int shears;
        Inventory inventory = aM_.player.getInventory();
        return (!state.is(Blocks.COBWEB) || (shears = IntStream.range(0, inventory.getContainerSize()).filter(i -> {
            return inventory.getItem(i).is(Items.SHEARS);
        }).findFirst().orElse(-1)) == -1) ? IntStream.range(0, inventory.getContainerSize()).filter(i2 -> {
            return inventory.getItem(i2).getDestroySpeed(state) > 1.0f && a(inventory.getItem(i2), state);
        }).boxed().max(Comparator.comparingDouble(i3 -> {
            return inventory.getItem(i3.intValue()).getDestroySpeed(state);
        })).orElse(-1).intValue() : shears;
    }

    private boolean a(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return stack.getItem() instanceof AxeItem;
        }
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return stack.get(DataComponents.TOOL) != null;
        }
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return stack.getItem() instanceof ShovelItem;
        }
        if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            return stack.getItem() instanceof HoeItem;
        }
        return true;
    }
}





