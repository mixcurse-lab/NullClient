package platform.client.features.modules.misc;

import static platform.api.module.Interface.aM_;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.render.ColorUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;

import platform.client.utils.timer.CounterUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;

@ModuleRegister(a = "X Ray", b = "Подсвечивает найденные древние обломки при взрыве динамита", c = Category.Misc)
public class XRay extends Module implements Interface {
    private final List<BlockPos> b = new ArrayList();
    private final CounterUtil c = new CounterUtil();
    private boolean d;

    @Generated
    public List<BlockPos> s() {
        return this.b;
    }

    @Generated
    public CounterUtil q() {
        return this.c;
    }

    @Generated
    public boolean r() {
        return this.d;
    }

    @Override
    public void c() {
        super.c();
        this.b.clear();
        this.d = false;
        this.c.b();
    }

    @Override
    public void b() {
        super.b();
        this.b.clear();
        this.d = false;
        this.c.b();
    }

    @EventTarget
    public void a(DrawEvent draw) {
        if (draw.c()) {
            this.b.removeIf(blockPos -> {
                return aM_.level.getBlockState(blockPos).is(Blocks.AIR) || blockPos.distSqr(aM_.player.blockPosition()) >= 6400.0d || !aM_.level.getChunkSource().hasChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4);
            });
            this.b.forEach(pos -> {
                draw.e().a(draw.h(), new AABB(pos), ColorUtil.a(255, InterfaceC0020Opcode.bo, 0, InterfaceC0020Opcode.ap), 1.0f);
            });
        }
    }

    @EventTarget
    public void a(PacketEvent packet) {
        if (packet.d() instanceof ClientboundSectionBlocksUpdatePacket chunkDeltaPacket) {
            chunkDeltaPacket.runUpdates((blockPos, blockState) -> {
                if (blockState.is(Blocks.ANCIENT_DEBRIS)) {
                    BlockPos add = blockPos.immutable();
                    if (!this.b.contains(add)) {
                        this.b.add(add);
                        this.d = true;
                        this.c.b();
                    }
                }
            });
        }
    }

    @EventTarget
    public void a(TickEvent e) {
        this.c.a();
        if (this.d && this.c.b(5L)) {
            ChatUtil.a((Object) ("Обнаружено &c" + this.b.size() + "&7 древних обломков "));
            this.d = false;
        }
    }
}

