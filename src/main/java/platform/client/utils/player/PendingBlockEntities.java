package platform.client.utils.player;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class PendingBlockEntities {
    private record Pending(BlockPos pos, CompoundTag tag) {}

    private static final Map<Long, List<Pending>> PENDING = new ConcurrentHashMap<>();
    private static final int MAX_CHUNKS = 512;
    private static final int MAX_PER_CHUNK = 64;

    private PendingBlockEntities() {}

    public static void queue(BlockPos pos, CompoundTag tag) {
        if (pos == null || tag == null || tag.isEmpty()) return;
        long key = ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4);
        List<Pending> list = PENDING.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()));
        synchronized (list) {
            list.removeIf(p -> p.pos().equals(pos));
            if (list.size() < MAX_PER_CHUNK) {
                list.add(new Pending(pos.immutable(), tag.copy()));
            }
        }
        if (PENDING.size() > MAX_CHUNKS) {
            PENDING.clear();
        }
    }

    public static void apply(ClientLevel level, ChunkPos chunkPos) {
        if (level == null || chunkPos == null) return;
        List<Pending> list = PENDING.remove(ChunkPos.pack(chunkPos.x(), chunkPos.z()));
        if (list == null || list.isEmpty()) return;
        List<Pending> copy;
        synchronized (list) {
            copy = new ArrayList<>(list);
        }
        for (Pending pending : copy) {
            try {
                var state = level.getBlockState(pending.pos());
                if (!state.hasBlockEntity()) continue;
                BlockEntity be = BlockEntity.loadStatic(pending.pos(), state, pending.tag(), level.registryAccess());
                if (be != null && level.getBlockState(pending.pos()).hasBlockEntity()) {
                    level.setBlockEntity(be);
                }
            } catch (Throwable ignored) {
            }
        }
    }

    public static void clear() {
        PENDING.clear();
    }
}
