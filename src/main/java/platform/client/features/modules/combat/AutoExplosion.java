package platform.client.features.modules.combat;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.math.MathUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;
import platform.client.utils.rotation.Rotation;

import platform.api.module.setting.BooleanSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;

@ModuleRegister(a = "Auto Explosion", b = "Размещает кристалл на обсидиане и мгновенно его подрывает", c = Category.Combat)
public class AutoExplosion extends Module {
    private BlockPos c;
    private int e;
    private final BooleanSetting b = new BooleanSetting("Установить двойной кристалл", false);
    private int d = -1;

    public AutoExplosion() {
        a(this.b);
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.b()) {
            if (event.d() instanceof ServerboundUseItemOnPacket packet && aM_.player.getMainHandItem().getItem() == Items.OBSIDIAN) {
                    BlockHitResult hit = packet.getHitResult();
                    this.c = hit.getBlockPos().relative(hit.getDirection());
                    this.d = -1;
                    this.e = 0;
            }
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.c == null) {
            return;
        }
        int slot = InventoryUtil.a(Items.END_CRYSTAL, true);
        if (slot == -1 || !aM_.level.getBlockState(this.c).is(Blocks.OBSIDIAN) || !aM_.player.isWithinBlockInteractionRange(this.c, 0.0d)) {
            q();
            return;
        }
        if (this.e == 1 || (this.b.c().booleanValue() && this.e == 5)) {
            b(slot);
        }
        for (EndCrystal crystal : aM_.level.getEntitiesOfClass(EndCrystal.class, new AABB(this.c.above()).inflate(0.5d), c -> {
            return true;
        })) {
            a(a(crystal.getBoundingBox()));
            if (this.e >= 3 && aM_.player.isWithinEntityInteractionRange(crystal, 0.0d)) {
                aM_.gameMode.attack(aM_.player, crystal);
                aM_.player.swing(InteractionHand.MAIN_HAND);
                if (this.b.c().booleanValue() && this.e < 7) {
                    break;
                }
                q();
                return;
            }
        }
        int i = this.e + 1;
        this.e = i;
        if (i > (this.b.c().booleanValue() ? 8 : 4)) {
            q();
        }
    }

    private void b(int slot) {
        if (aM_.player.getInventory().getSelectedSlot() != slot) {
            this.d = aM_.player.getInventory().getSelectedSlot();
            aM_.player.getInventory().setSelectedSlot(slot);
        }
        Vec3 center = new Vec3(this.c.getX() + 0.5d, this.c.getY() + 0.5d, this.c.getZ() + 0.5d);
        Vec3 hit = (Vec3) new AABB(this.c).clip(aM_.player.getEyePosition(), center).orElse(center);
        a(center);
        aM_.gameMode.useItemOn(aM_.player, InteractionHand.MAIN_HAND, new BlockHitResult(hit, Direction.getApproximateNearest(hit.subtract(center)), this.c, false));
        aM_.player.swing(InteractionHand.MAIN_HAND);
    }

    private Vec3 a(AABB b) {
        Vec3 e = aM_.player.getEyePosition();
        return new Vec3(Mth.clamp(e.x, b.minX, b.maxX) + ((double) MathUtil.a(-0.1f, 0.1f)), Mth.clamp(e.y, b.minY, b.maxY) + ((double) MathUtil.a(-0.1f, 0.1f)), Mth.clamp(e.z, b.minZ, b.maxZ) + ((double) MathUtil.a(-0.1f, 0.1f)));
    }

    private void a(Vec3 point) {
        Rotation r = Rotation.a(aM_.player.getEyePosition(), point);
        Delta.h().d().k().a(r, 120.0f, 1, 2);
    }

    private void q() {
        if (this.d != -1) {
            aM_.player.getInventory().setSelectedSlot(this.d);
        }
        this.d = -1;
        this.c = null;
        this.e = 0;
    }

    @Override
    public void c() {
        super.c();
        q();
    }
}


