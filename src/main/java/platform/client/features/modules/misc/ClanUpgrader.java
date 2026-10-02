package platform.client.features.modules.misc;

import platform.inject.invokers.MinecraftInvoker;
import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.rotation.Look;
import platform.client.utils.math.MathUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;
import platform.client.utils.rotation.Rotation;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@ModuleRegister(a = "Clan Upgrader", b = "Быстро прокачивает клан с помощью редстоуна и факела", c = Category.Misc)
public class ClanUpgrader extends Module {
    private int b;

    @Override
    public void c() {
        super.c();
        if (this.b != -1) {
            aM_.player.getInventory().setSelectedSlot(this.b);
            this.b = -1;
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        int redstone = InventoryUtil.a(Items.REDSTONE, true);
        int torch = InventoryUtil.a(Items.TORCH, true);
        int target = redstone != -1 ? redstone : torch;
        if (redstone == -1 && torch == -1) {
            ChatUtil.a((Object) "Вам необходимо иметь факел или редстоун в хотбаре");
            a();
            return;
        }
        float randomPitch = ((float) (Math.sin(System.currentTimeMillis() / 1220.0d) * ((double) (Math.abs(90.0f - aM_.player.getXRot()) / 8.0f)))) + MathUtil.a(-0.1f, 0.1f);
        Rotation rotation = new Rotation(Look.b() + MathUtil.a(-1.0f, 1.0f), MathUtil.b(88.0f + randomPitch, -90.0f, 90.0f));
        Delta.h().d().k().a(rotation, 90.0f, 1, 1);
        if (this.b == -1) {
            this.b = aM_.player.getInventory().getSelectedSlot();
        }
        if (aM_.player.getInventory().getSelectedSlot() != target) {
            aM_.player.getInventory().setSelectedSlot(target);
        }
        if (Rotation.b().a(rotation) <= 1.0d) {
            BlockPos position = aM_.player.blockPosition();
            if (aM_.level.getBlockState(position).is(Blocks.REDSTONE_WIRE) || aM_.level.getBlockState(position).is(Blocks.TORCH)) {
                aM_.gameMode.startDestroyBlock(position, Direction.UP);
                aM_.player.swing(InteractionHand.MAIN_HAND);
            } else {
                ((platform.inject.invokers.MinecraftInvoker) aM_).invokeDoItemUse();
            }
        }
    }
}








