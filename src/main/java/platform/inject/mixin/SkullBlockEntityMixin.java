package platform.inject.mixin;

import platform.client.Delta;
import platform.client.utils.player.ServerUtil;
import platform.client.utils.player.SkullProfileNbtFixer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public abstract class SkullBlockEntityMixin {
    @Inject(method = "loadStatic", at = @At("HEAD"))
    private static void delta$normalizeSkullOnLoad(BlockPos pos, BlockState state, CompoundTag tag, HolderLookup.Provider registries, CallbackInfoReturnable<BlockEntity> cir) {
        try {
            if (Delta.h() != null && Delta.h().d().t().skullFix() != null && Delta.h().d().t().skullFix().m() && ServerUtil.c.a()) {
                SkullProfileNbtFixer.normalize(tag);
            } else if (Delta.h() == null) {

                if (ServerUtil.c.a()) SkullProfileNbtFixer.normalize(tag);
            }
        } catch (Exception ignored) {
            try { SkullProfileNbtFixer.normalize(tag); } catch (Exception ignored2) {}
        }
    }
}
