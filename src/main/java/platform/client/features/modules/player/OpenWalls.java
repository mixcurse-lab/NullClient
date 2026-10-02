package platform.client.features.modules.player;

import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.module.ModuleRegister;

import java.util.Set;
import lombok.Generated;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BeaconBlock;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LoomBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BlastFurnaceBlock;
import net.minecraft.world.level.block.CartographyTableBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.SmokerBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.client.player.LocalPlayer;

@ModuleRegister(a = "Open Walls", b = "Позволяет открывать хранилища сквозь стены", c = Category.Player)
public class OpenWalls extends Module {
    private final Set<Class<?>> b = Set.of(new Class[]{AbstractChestBlock.class, FurnaceBlock.class, CraftingTableBlock.class, SpawnerBlock.class, ShulkerBoxBlock.class, AnvilBlock.class, BeaconBlock.class, BlastFurnaceBlock.class, BrewingStandBlock.class, CampfireBlock.class, CartographyTableBlock.class, GrindstoneBlock.class, LecternBlock.class, LoomBlock.class, SmokerBlock.class, StonecutterBlock.class, BarrelBlock.class});

    @Generated
    public Set<Class<?>> q() {
        return this.b;
    }

    public BlockHitResult a(LocalPlayer player) {
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(player.getViewVector(1.0f).scale(player.blockInteractionRange()));
        return player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player) {
            @Override
            public VoxelShape getBlockShape(BlockState state, BlockGetter world, BlockPos pos) {
                for (Class<?> clazz : OpenWalls.this.b) {
                    if (clazz.isInstance(state.getBlock())) {
                        return super.getBlockShape(state, world, pos);
                    }
                }
                return Shapes.empty();
            }
        });
    }
}



