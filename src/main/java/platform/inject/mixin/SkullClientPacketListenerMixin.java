package platform.inject.mixin;

import platform.client.Delta;
import platform.client.utils.player.PendingBlockEntities;
import platform.client.utils.player.ServerUtil;
import platform.client.utils.player.SkullProfileNbtFixer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class SkullClientPacketListenerMixin {
    @Inject(method = "handleBlockEntityData", at = @At("HEAD"))
    private void delta$normalizeSkullOnPacket(ClientboundBlockEntityDataPacket packet, CallbackInfo ci) {


        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.level != null) {
                var pos = packet.getPos();
                if (!mc.level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                    PendingBlockEntities.queue(pos, packet.getTag());
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            if (Delta.h() != null && Delta.h().d().t().skullFix() != null && Delta.h().d().t().skullFix().m() && ServerUtil.c.a()) {
                SkullProfileNbtFixer.normalize(packet.getTag());
            } else if (Delta.h() == null) {
                if (ServerUtil.c.a()) SkullProfileNbtFixer.normalize(packet.getTag());
            }
        } catch (Exception ignored) {
            try { SkullProfileNbtFixer.normalize(packet.getTag()); } catch (Exception ignored2) {}
        }
    }
}
