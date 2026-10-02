package platform.client.features.modules.misc;

import platform.api.module.Interface;
import platform.inject.accessors.SlotAccessor;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.math.MathUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;
import platform.client.utils.rotation.Rotation;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;

@ModuleRegister(a = "Apple Farmer", b = "Автоматически фармит яблоки", c = Category.Misc)
public class AppleFarmer extends Module {
    @EventTarget
    public void a(TickEvent event) {
        if (aM_.player == null || aM_.level == null) return;

        Delta.h().d().t().aV().b(19);
        if (Delta.h().d().v().k().a() || !Delta.h().d().v().i().a(aM_.player.getMainHandItem(), 10.0d, 98.0d)) {
            return;
        }

        if (aM_.gui.screen() instanceof ContainerScreen) {
            q();
            return;
        }

        if (u()) {
            q();
            return;
        }

        if (InventoryUtil.a(Items.BONE_MEAL) < 8 && a(Items.BONE) >= 0) {
            r();
            return;
        }

        BlockPos log = a(5.0d, st -> st.is(BlockTags.LOGS), this::c);
        if (log != null) {
            a(log, stack -> stack.getItem() instanceof AxeItem);
            return;
        }

        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        BlockPos origin = aM_.player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-4, -2, -4), origin.offset(4, 2, 4))) {
            BlockState st = aM_.level.getBlockState(pos);
            if (!(st.is(Blocks.DIRT) || st.is(Blocks.GRASS_BLOCK) || st.is(Blocks.PODZOL))) {
                continue;
            }
            BlockState up = aM_.level.getBlockState(pos.above());
            if (!(up.isAir() || up.getBlock() instanceof SaplingBlock)) {
                continue;
            }
            double score = c(pos.immutable());
            if (score < bestScore) {
                bestScore = score;
                best = pos.immutable();
            }
        }
        if (best != null) {
            a(best);
            return;
        }

        s();
    }

    private void a(BlockPos pos, Predicate<ItemStack> tool) {
        if (aM_.player == null) return;
        if (!tool.test(aM_.player.getMainHandItem())) {
            a(tool);
            return;
        }
        if ((aM_.player.getMainHandItem().getItem() instanceof AxeItem) && aM_.player.getAttackStrengthScale(0.0f) <= 0.15f) {
            return;
        }
        Vec3 center = Vec3.atCenterOf(pos);
        if (a(center)) {
            Vec3 hit = new AABB(pos).clip(aM_.player.getEyePosition(), center).orElse(center);
            Direction dir = Direction.getApproximateNearest(hit.subtract(center));
            aM_.gameMode.continueDestroyBlock(pos, dir);
            aM_.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private void a(BlockPos dirt) {
        if (aM_.level == null) return;
        boolean grown = aM_.level.getBlockState(dirt.above()).getBlock() instanceof SaplingBlock;
        Predicate<ItemStack> want = grown ? s -> s.is(Items.BONE_MEAL) : s2 -> {
            if (s2.getItem() instanceof BlockItem) {
                BlockItem b = (BlockItem) s2.getItem();
                return b.getBlock() instanceof SaplingBlock;
            }
            return false;
        };
        if (!want.test(aM_.player.getMainHandItem())) {
            a(want);
            return;
        }
        Vec3 top = new Vec3(((double) dirt.getX()) + 0.5d, dirt.getY() + 1, ((double) dirt.getZ()) + 0.5d);
        if (a(top) && aM_.player.tickCount % 4 == 0) {
            aM_.gameMode.useItemOn(aM_.player, InteractionHand.MAIN_HAND, new BlockHitResult(top, Direction.UP, grown ? dirt.above() : dirt, false));
            aM_.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private void q() {
        if (aM_.gui.screen() instanceof ContainerScreen) {
            ContainerScreen screen = (ContainerScreen) aM_.gui.screen();
            int moved = a((AbstractContainerMenu) screen.getMenu(), false, 9, stack -> stack.is(Items.APPLE) || stack.is(Items.OAK_LOG));
            if (moved == 0 || aM_.player.tickCount % 50 == 0) {
                aM_.player.clientSideCloseContainer();
            }
            return;
        }
        BlockPos chestPos = a(5.0d, s -> s.is(u() ? Blocks.CHEST : Blocks.BARREL), this::c);
        b(chestPos);
    }

    private void r() {
        if (aM_.player.tickCount % 2 != 0) return;
        if (aM_.player.inventoryMenu.getSlot(0).getItem().is(Items.BONE_MEAL)) {
            a((AbstractContainerMenu) aM_.player.inventoryMenu, 0, 0, ContainerInput.QUICK_MOVE);
            aM_.player.clientSideCloseContainer();
            return;
        }
        int bone = a(Items.BONE);
        if (bone < 0) return;
        a((AbstractContainerMenu) aM_.player.inventoryMenu, bone, 0, ContainerInput.PICKUP);
        a((AbstractContainerMenu) aM_.player.inventoryMenu, 1, 0, ContainerInput.PICKUP);
    }

    private void s() {
        if (aM_.player == null) return;
        float sw = t();
        a(new Rotation(sw * 10.0f, MathUtil.b(sw / 4.0f, -30.0f, 30.0f)));
        if (aM_.player.tickCount % 5 != 0) return;
        int keep = -1;
        int max = -1;
        for (Slot slot : aM_.player.inventoryMenu.slots) {
            if (a(slot) && slot.getItem().is(Items.OAK_SAPLING) && slot.getItem().getCount() > max) {
                max = slot.getItem().getCount();
                keep = slot.index;
            }
        }
        for (Slot slot2 : aM_.player.inventoryMenu.slots) {
            if (a(slot2) && (slot2.getItem().is(Items.STICK) || (slot2.getItem().is(Items.OAK_SAPLING) && slot2.index != keep))) {
                a((AbstractContainerMenu) aM_.player.inventoryMenu, slot2.index, 1, ContainerInput.THROW);
            }
        }
    }

    private void b(BlockPos block) {
        if (block != null && aM_.player.getMainHandItem().getItem() instanceof BlockItem) {
            a(stack -> !(stack.getItem() instanceof BlockItem));
        }
        if (block != null && !(aM_.player.getMainHandItem().getItem() instanceof BlockItem) && a(Vec3.atCenterOf(block)) && aM_.player.tickCount % 4 == 0) {
            Vec3 center = Vec3.atCenterOf(block);
            Vec3 hit = new AABB(block).clip(aM_.player.getEyePosition(), center).orElse(center);
            Direction dir = Direction.getApproximateNearest(hit.subtract(center));
            aM_.gameMode.useItemOn(aM_.player, InteractionHand.MAIN_HAND, new BlockHitResult(hit, dir, block, false));
        }
    }

    private boolean a(Predicate<ItemStack> match) {
        if (aM_.player == null) return false;
        int slot = IntStream.range(0, 36).filter(i -> match.test(aM_.player.getInventory().getItem(i))).findFirst().orElse(-1);
        if (slot < 0) return false;
        if (slot < 9) {
            aM_.player.getInventory().setSelectedSlot(slot);
            return true;
        }
        if (aM_.player.tickCount % 4 != 0) return true;
        int target = IntStream.range(0, 9).filter(i2 -> aM_.player.getInventory().getItem(i2).isEmpty()).findFirst().orElse(aM_.player.getInventory().getSelectedSlot());
        Delta.h().d().v().a().a(slot, target, 1);
        aM_.player.getInventory().setSelectedSlot(target);
        return true;
    }

    private boolean a(Vec3 point) {
        Rotation rotation = Rotation.a(aM_.player.getEyePosition(), point);
        float sw = t();
        a(new Rotation(rotation.c() + (sw / 2.0f), MathUtil.b(rotation.d() + (sw / 4.0f), -90.0f, 90.0f)));
        return Rotation.b().a(rotation) < 20.0d;
    }

    private void a(Rotation rotation) {
        Delta.h().d().k().a(rotation, 180.0f, 1, 1);
    }

    private float t() {
        float t = aM_.player.tickCount + aM_.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        return (float) (((Math.sin(t * 0.31f) * 0.5d) + (Math.sin((t * 0.73f) + 1.1f) * 0.30000003042305273d) + (Math.sin((t * 1.7f) + 2.6f) * 0.2000000149681302d)) * 8.0d);
    }

    private int a(AbstractContainerMenu handler, boolean fromContainer, int limit, Predicate<ItemStack> match) {
        int moved = 0;
        for (Slot slot : handler.slots) {
            if (moved >= limit) break;
            if (a(slot) != fromContainer && match.test(slot.getItem())) {
                a(handler, slot.index, 0, ContainerInput.QUICK_MOVE);
                moved++;
            }
        }
        return moved;
    }

    private void a(AbstractContainerMenu handler, int slot, int button, ContainerInput action) {
        aM_.gameMode.handleContainerInput(handler.containerId, slot, button, action, aM_.player);
    }

    private boolean a(Slot slot) {
        return ((SlotAccessor) slot).getInventory() == aM_.player.getInventory();
    }

    private int a(Item item) {
        for (Slot slot : aM_.player.inventoryMenu.slots) {
            if (a(slot) && slot.getItem().is(item)) {
                return slot.index;
            }
        }
        return -1;
    }

    private boolean a(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem) {
            BlockItem b = (BlockItem) stack.getItem();
            if (!(b.getBlock() instanceof SaplingBlock)) {
                if (stack.is(Items.BONE_MEAL) && (!stack.is(Items.BONE) || InventoryUtil.a(Items.BONE_MEAL) != 0)) {
                    return false;
                }
            }
        } else if (stack.is(Items.BONE_MEAL)) {
        }
        return true;
    }

    private boolean u() {
        return InventoryUtil.a(Items.APPLE) > 128 || InventoryUtil.a(Items.OAK_LOG) > 192;
    }

    private boolean v() {
        return InventoryUtil.a(Items.OAK_SAPLING) == 0 || InventoryUtil.a(Items.BONE_MEAL) == 0;
    }

    private double c(BlockPos pos) {
        return aM_.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos));
    }

    private double d(BlockPos pos) {
        if (aM_.player == null) return Double.MAX_VALUE;
        Vec3 eye = aM_.player.getEyePosition();
        Vec3 look = aM_.player.getViewVector(1.0f);
        Vec3 diff = Vec3.atCenterOf(pos).subtract(eye);
        double along = diff.dot(look);
        if (along <= 0.0d) return Double.MAX_VALUE;
        return diff.subtract(look.scale(along)).lengthSqr();
    }

    private BlockPos a(double radius, Predicate<BlockState> match, ToDoubleFunction<BlockPos> score) {
        if (aM_.level == null) return null;
        BlockPos origin = aM_.player.blockPosition();
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        double reachSq = radius * radius;
        int r = (int) Math.ceil(radius);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (match.test(aM_.level.getBlockState(pos)) && c(pos) <= reachSq) {
                        double s = score.applyAsDouble(pos);
                        if (s < bestScore) {
                            bestScore = s;
                            best = pos.immutable();
                        }
                    }
                }
            }
        }
        return best;
    }
}


