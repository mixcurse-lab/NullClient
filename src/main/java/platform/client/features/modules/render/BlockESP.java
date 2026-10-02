package platform.client.features.modules.render;

import platform.inject.accessors.LevelAccessor;
import platform.client.features.commands.BlockESPCommand;
import platform.api.system.configs.ThemeInfo;
import platform.api.module.Category;
import platform.client.Delta;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.TickEvent;
import platform.client.utils.render.ColorUtil;

import static platform.api.module.Interface.aM_;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.phys.AABB;

@ModuleRegister(a = "Block ESP", b = "Подсвечивает добавленные вами блоки через .blockesp", c = Category.Render)
public class BlockESP extends Module {
    private final List<BlockPos> b = new CopyOnWriteArrayList();
    private ExecutorService c;
    private int d;

    @Override
    public void b() {
        super.b();
        this.b.clear();
        this.c = Executors.newSingleThreadExecutor();
        this.d = 0;
    }

    @Override
    public void c() {
        super.c();
        this.b.clear();
        if (this.c != null) {
            this.c.shutdownNow();
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        int i = this.d + 1;
        this.d = i;
        if (i % 12 == 0) {
            List<BlockESPCommand.a> list = Delta.h().d().u().g().c();
            if (list.isEmpty()) {
                this.b.clear();
            } else {
                this.c.submit(() -> {
                    a((Set<Block>) list.stream().map((v0) -> {
                        return v0.a();
                    }).collect(Collectors.toSet()));
                });
            }
        }
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (event.c()) {
            List<BlockESPCommand.a> entries = Delta.h().d().u().g().c();
            if (!entries.isEmpty()) {
                Map<Block, Integer> colors = (Map) entries.stream().collect(Collectors.toMap((v0) -> {
                    return v0.a();
                }, (v0) -> {
                    return v0.b();
                }, (first, second) -> {
                    return second;
                }));
                for (TickingBlockEntity ticker : ((LevelAccessor) aM_.level).getBlockEntityTickers()) {
                    if (!ticker.isRemoved()) {
                        a(event, ticker.getPos(), colors);
                    }
                }
                for (BlockPos pos : this.b) {
                    a(event, pos, colors);
                }
            }
        }
    }

    private void a(DrawEvent event, BlockPos pos, Map<Block, Integer> colors) {
        Integer color = colors.get(aM_.level.getBlockState(pos).getBlock());
        if (color != null) {
            event.e().a(event.h(), new AABB(pos), color.intValue() != -1 ? color.intValue() : ColorUtil.a(Delta.h().d().o().a(ThemeInfo.PRIMARY).a(), InterfaceC0020Opcode.al), 1.5f);
        }
    }

    private void a(Set<Block> targets) {
        List<BlockPos> found = new ArrayList<>();
        BlockPos center = aM_.player.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int maxY = aM_.level.getMinY() + aM_.level.getHeight() - 1;
        for (int x = center.getX() - 70; x <= center.getX() + 70; x++) {
            for (int z = center.getZ() - 70; z <= center.getZ() + 70; z++) {
                if (aM_.level.getChunkSource().hasChunk(x >> 4, z >> 4)) {
                    for (int y = aM_.level.getMinY(); y < maxY; y++) {
                        if (targets.contains(aM_.level.getBlockState(mutable.set(x, y, z)).getBlock())) {
                            found.add(mutable.immutable());
                        }
                    }
                }
            }
        }
        this.b.clear();
        this.b.addAll(found);
    }
}


