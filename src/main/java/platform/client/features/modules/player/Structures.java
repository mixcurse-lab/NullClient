package platform.client.features.modules.player;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.BlockChangeEvent;
import platform.api.event.events.client.TickEvent;

import platform.client.utils.timer.CounterUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

@ModuleRegister(a = "Structures", b = "Отображает время до исчезновения структур трапки и пласта", c = Category.Player)
public class Structures extends Module {
    private final List<Block> allowedBlocks = java.util.Arrays.asList(new Block[]{Blocks.QUARTZ_BLOCK, Blocks.DEAD_TUBE_CORAL_BLOCK, Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.PURPUR_PILLAR, Blocks.END_STONE_BRICKS, Blocks.NETHER_BRICKS, Blocks.GILDED_BLACKSTONE, Blocks.PRISMARINE_BRICKS, Blocks.ICE, Blocks.NETHER_WART_BLOCK, Blocks.RESPAWN_ANCHOR, Blocks.NETHERITE_BLOCK});
    private final List<a> c = new ObjectArrayList();
    private final Set<BlockPos> d = new ObjectOpenHashSet();
    private int e;

    @EventTarget
    public void a(BlockChangeEvent event) {
        if (this.allowedBlocks.contains(event.d().getBlock()) && !this.allowedBlocks.contains(event.c().getBlock())) {
            this.d.add(event.b());
            this.e = 2;
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        this.c.removeIf(structure -> {
            if (structure.c().a(structure.d().d())) {
                structure.a();
                return true;
            }
            return false;
        });
        if (this.e > 0) {
            int i = this.e - 1;
            this.e = i;
            if (i == 0) {
                if (!this.d.isEmpty()) {
                    int minX = this.d.stream().mapToInt(BlockPos::getX).min().orElse(0);
                    int minY = this.d.stream().mapToInt(BlockPos::getY).min().orElse(0);
                    int minZ = this.d.stream().mapToInt(BlockPos::getZ).min().orElse(0);
                    int maxX = this.d.stream().mapToInt(BlockPos::getX).max().orElse(0);
                    int maxY = this.d.stream().mapToInt(BlockPos::getY).max().orElse(0);
                    int maxZ = this.d.stream().mapToInt(BlockPos::getZ).max().orElse(0);
                    BoundingBox box = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
                    for (b type : b.values()) {
                        if (type.a(box, this.d)) {
                            this.c.add(new a(type, box));
                            break;
                        }
                    }
                }
                this.d.clear();
            }
        }
    }

    @Override
    public void c() {
        this.c.forEach((v0) -> {
            v0.a();
        });
        this.d.clear();
        this.c.clear();
        this.e = 0;
        super.c();
    }

    static class a implements Interface {
        private final CounterUtil c = new CounterUtil();
        private final b d;
        private final BoundingBox e;

        @Generated
        public CounterUtil c() {
            return this.c;
        }

        @Generated
        public b d() {
            return this.d;
        }

        @Generated
        public BoundingBox e() {
            return this.e;
        }

        public a(b type, BoundingBox box) {
            this.d = type;
            this.e = box;
            this.c.b();
        }

        public void a() {
        }
    }

    public enum b {
        DRAGON_TRAPKA("Драконья трапка", new int[][]{new int[]{7, 7, 7}, new int[]{7, 7, 6}}, true, 30000, Items.NETHERITE_SCRAP),
        TRAPKA("Трапка", new int[][]{new int[]{5, 5, 5}, new int[]{5, 5, 4}, new int[]{5, 6, 5}, new int[]{5, 6, 4}}, true, 15000, Items.NETHERITE_SCRAP),
        DRAGON_PLAST("Драконий пласт", new int[][]{new int[]{7, 7, 2}, new int[]{7, 7, 1}}, true, 20000, Items.DRIED_KELP),
        PLAST("Пласт", new int[][]{new int[]{5, 5, 2}, new int[]{5, 5, 1}}, true, 20000, Items.DRIED_KELP),
        GARMOSHKA("Пласт", new int[][]{new int[]{5, 5, 5}, new int[]{5, 6, 5}}, false, 20000, Items.DRIED_KELP);

        private final String f;
        private final int[][] g;
        private final boolean h;
        private final long i;
        private final Item j;

        @Generated
        b(final String displayName, final int[][] dimensions, final boolean hollow, final long cooldown, final Item item) {
            this.f = displayName;
            this.g = dimensions;
            this.h = hollow;
            this.i = cooldown;
            this.j = item;
        }

        @Generated
        public String a() {
            return this.f;
        }

        @Generated
        public int[][] b() {
            return this.g;
        }

        @Generated
        public boolean c() {
            return this.h;
        }

        @Generated
        public long d() {
            return this.i;
        }

        @Generated
        public Item e() {
            return this.j;
        }

        public boolean a(BoundingBox box, Set<BlockPos> positions) {
            int[] size = {box.getXSpan(), box.getYSpan(), box.getZSpan()};
            Arrays.sort(size);
            for (int[] dimension : this.g) {
                int[] sorted = (int[]) dimension.clone();
                Arrays.sort(sorted);
                if (Arrays.equals(size, sorted)) {
                    return this.h == positions.stream().noneMatch(pos -> {
                        return pos.getX() > box.minX() && pos.getX() < box.maxX() && pos.getY() > box.minY() && pos.getY() < box.maxY() && pos.getZ() > box.minZ() && pos.getZ() < box.maxZ();
                    });
                }
            }
            return false;
        }
    }
}



