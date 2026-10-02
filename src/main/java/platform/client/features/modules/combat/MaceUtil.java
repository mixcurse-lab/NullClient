package platform.client.features.modules.combat;

import static platform.api.module.Interface.aM_;

import platform.api.module.Interface;

import java.util.ArrayList;
import java.util.Optional;
import lombok.Generated;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

public class MaceUtil implements Interface {
    @Generated
    private MaceUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean a() {
        return aM_.player != null && aM_.player.getMainHandItem().is(Items.MACE);
    }

    public static Optional<Vec3> a(LocalPlayer player, Level world) {
        if (world == null || player.onGround()) {
            return Optional.empty();
        }
        if (player.getAbilities().flying || player.isFallFlying() || player.isPassenger() || player.onClimbable()) {
            return Optional.empty();
        }
        if (player.hasEffect(MobEffects.LEVITATION)) {
            return Optional.empty();
        }
        if (player.isInWater() || player.isInLava()) {
            return Optional.empty();
        }
        double gravity = player.getGravity();
        AABB baseBox = player.getBoundingBox();
        double ox = 0.0d;
        double oy = 0.0d;
        double oz = 0.0d;
        Vec3 velocity = player.getDeltaMovement();
        ArrayList<VoxelShape> shapes = new ArrayList<>();
        for (int tick = 0; tick < 400; tick++) {
            double drag = 0.98d;
            double vx = velocity.x * drag;
            double vy = (velocity.y - gravity) * drag;
            double vz = velocity.z * drag;
            Vec3 step = new Vec3(vx, vy, vz);
            AABB simBox = baseBox.move(ox, oy, oz);
            shapes.clear();
            Vec3 allowed = Entity.collideBoundingBox(player, step, simBox, world, shapes);
            if (step.y < -1.0E-5d && allowed.y > step.y + 1.0E-5d) {
                return Optional.of(player.position().add(ox + allowed.x, oy + allowed.y, oz + allowed.z));
            }
            ox += allowed.x;
            oy += allowed.y;
            oz += allowed.z;
            if (a(world, baseBox.move(ox, oy, oz))) {
                return Optional.of(player.position().add(ox, oy, oz));
            }
            if (Math.abs(allowed.x - step.x) > 1.0E-5d) {
                vx = 0.0d;
            }
            if (Math.abs(allowed.y - step.y) > 1.0E-5d) {
                vy = 0.0d;
            }
            if (Math.abs(allowed.z - step.z) > 1.0E-5d) {
                vz = 0.0d;
            }
            velocity = new Vec3(vx, vy, vz);
            if (velocity.lengthSqr() < 1.0E-12d && step.y >= -1.0E-5d) {
                break;
            }
        }
        return Optional.empty();
    }

    public static boolean b() {
        if (aM_.level == null) {
            return false;
        }
        LocalPlayer player = aM_.player;
        if (player == null) {
            return false;
        }
        double offsetY = 0.0d;
        Vec3 velocity = player.getDeltaMovement();
        double gravity = player.getGravity();
        for (int tick = 0; tick < 2; tick++) {
            double nextVy = (velocity.y - gravity) * 0.98d;
            Vec3 step = new Vec3(0.0d, nextVy, 0.0d);
            AABB box = player.getBoundingBox().move(0.0d, offsetY, 0.0d);
            ArrayList<VoxelShape> shapes = new ArrayList<>();
            Vec3 allowed = Entity.collideBoundingBox(player, step, box, aM_.level, shapes);
            if (nextVy < 0.0d && b(aM_.level, box.move(allowed.x, allowed.y, allowed.z))) {
                return true;
            }
            offsetY += allowed.y;
            velocity = new Vec3(velocity.x, allowed.y, velocity.z);
        }
        return false;
    }

    private static boolean a(Level world, AABB box) {
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            FluidState fluid = world.getBlockState(pos).getFluidState();
            if (!fluid.isEmpty() && (fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA))) {
                return true;
            }
        }
        return false;
    }

    private static boolean b(Level world, AABB box) {
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = world.getBlockState(pos);
            if (state.is(Blocks.COBWEB) || state.is(Blocks.SWEET_BERRY_BUSH)) {
                return true;
            }
            FluidState fluid = state.getFluidState();
            if (!fluid.isEmpty() && (fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA))) {
                return true;
            }
        }
        return false;
    }

    public static double getFallDistance() {
        return aM_.player != null ? aM_.player.fallDistance : 0.0d;
    }

    public static boolean canMaceCrit() {
        return aM_.player != null && aM_.player.fallDistance > 1.5f && !aM_.player.onGround() && !aM_.player.isFallFlying();
    }
}