package platform.client.features.modules.render;

import platform.inject.accessors.LevelAccessor;
import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.module.Module;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.ProjectUtil;
import platform.client.utils.player.ServerUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.DrawEvent;
import platform.client.utils.render.pipeline.DeltaRenderUtil;

import platform.client.utils.timer.CounterUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Generated;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.joml.Vector2f;

@ModuleRegister(a = "Warden ESP", b = "Отображает сундуки в городе варденов с таймером возрождения", c = Category.Render)
public class WardenESP extends Module {
    private static final Pattern b = Pattern.compile("(\\d{2}):(\\d{2})");
    private static final Identifier c0 = Identifier.fromNamespaceAndPath("delta", "pictures/minecraft/chest.png");
    private final List<a> c = new ArrayList();
    private final CounterUtil d = new CounterUtil();
    private List<BlockPos> e = new ArrayList();
    private boolean f;

    @EventTarget
    public void a(DrawEvent event) {
        if (aM_.level.dimension().identifier().toString().equals("minecraft:overworld")) {
            if (!this.f || this.d.c() >= 500L) {
                this.e = q();
                this.d.b();
                this.f = true;
            }
            List<BlockPos> chests = this.e;
            if (event.b()) {
                for (Entity class_1531Var : aM_.level.getEntities(aM_.player, aM_.player.getBoundingBox().inflate(256.0), e -> e instanceof ArmorStand)) {
                    if (class_1531Var instanceof ArmorStand) {
                        ArmorStand stand = (ArmorStand) class_1531Var;
                        Matcher matcher = b.matcher(stand.getName().getString());
                        if (matcher.find()) {
                            int minutes = Integer.parseInt(matcher.group(1));
                            int seconds = Integer.parseInt(matcher.group(2));
                            long ms = ((((long) minutes) * 60) + ((long) seconds)) * 1000;
                            BlockPos nearest = a(chests, stand.blockPosition());
                            if (nearest != null) {
                                a existing = b(nearest);
                                if (existing != null) {
                                    existing.a(ms);
                                } else {
                                    this.c.add(new a(nearest, ms));
                                }
                            }
                        }
                    }
                }
                this.c.removeIf(info -> {
                    return info.a() <= 0;
                });
                a(event, chests);
            }
            if (event.c()) {
                b(event, chests);
            }
        }
    }

    private void a(DrawEvent event, List<BlockPos> chests) {
        int background = ColorUtil.a(11, 11, 13, InterfaceC0020Opcode.cR);
        for (BlockPos coord : chests) {
            a info = b(coord);
            if (info != null) {
                Vector2f screen = ProjectUtil.a(((double) coord.getX()) + 0.5d, coord.getY() + 1, ((double) coord.getZ()) + 0.5d);
                if (ProjectUtil.a(screen)) {
                    int totalSec = (int) (info.a() / 1000);
                    Component text = Component.literal(String.format(Locale.US, "%02d:%02d", Integer.valueOf(totalSec / 60), Integer.valueOf(totalSec % 60)));
                    float width = 16.5f + Fonts.e.a(text.getString(), 6.5f);
                    float x = screen.x() - (width / 2.0f);
                    float y = screen.y() - 6.0f;
                    event.d().a(event.i(), x, y, width - 0.5f, 12.0f, 3.5f, background, 1.0f, background, 6.0f);
                    DeltaRenderUtil.queueTexture(event.i(), c0, x + 3.0f, y + 0.5f + 2.0f, 7.0f, 7.0f, -1, null);
                    Fonts.e.a(event.i(), text, x + 3.0f + 8.0f + 2.0f, (y + ((12.0f - Fonts.e.a(6.5f)) / 2.0f)) - 0.5f, 6.5f);
                }
            }
        }
    }

    private void b(DrawEvent event, List<BlockPos> chests) {
        for (BlockPos coord : chests) {
            if (b(coord) == null && aM_.player.getEyePosition().distanceToSqr(coord.getX() + 0.5d, coord.getY() + 0.5d, coord.getZ() + 0.5d) <= 4096.0d) {
                event.e().a(event.h(), new AABB(coord.getX(), coord.getY(), coord.getZ(), coord.getX() + 1, coord.getY() + 1, coord.getZ() + 1), ColorUtil.a(255, 100, 100, 255), 1.0f);
            }
        }
    }

    private BlockPos a(List<BlockPos> chests, BlockPos standPos) {
        for (BlockPos coord : chests) {
            if (standPos.getX() == coord.getX() && standPos.getZ() == coord.getZ()) {
                return coord;
            }
        }
        return null;
    }

    private a b(BlockPos pos) {
        int currentAnarchy = ServerUtil.a.d();
        for (a info : this.c) {
            if (info.c().equals(pos) && info.e() == currentAnarchy) {
                return info;
            }
        }
        return null;
    }

    public long a(BlockPos pos) {
        for (Entity class_1531Var : aM_.level.getEntities(aM_.player, aM_.player.getBoundingBox().inflate(256.0), e -> e instanceof ArmorStand)) {
            if (class_1531Var instanceof ArmorStand) {
                ArmorStand stand = (ArmorStand) class_1531Var;
                if (stand.blockPosition().getX() == pos.getX() && stand.blockPosition().getZ() == pos.getZ()) {
                    Matcher matcher = b.matcher(stand.getName().getString());
                    if (matcher.find()) {
                        return ((((long) Integer.parseInt(matcher.group(1))) * 60) + ((long) Integer.parseInt(matcher.group(2)))) * 1000;
                    }
                }
            }
        }
        a info = b(pos);
        if (info == null) {
            return -1L;
        }
        return info.a();
    }

    public List<BlockPos> q() {
        BlockEntity blockEntity;
        BlockEntityType<?> type;
        List<BlockPos> result = new ArrayList<>();
        for (TickingBlockEntity ticker : ((platform.inject.accessors.LevelAccessor) aM_.level).getBlockEntityTickers()) {
            BlockPos pos = ticker.getPos();
            if (!ticker.isRemoved() && pos.getY() >= -60 && pos.getY() <= -35 && pos.getX() >= -2070 && pos.getX() <= -1921 && pos.getZ() >= -2076 && pos.getZ() <= -1929 && (blockEntity = aM_.level.getBlockEntity(pos)) != null && ((type = blockEntity.getType()) == BlockEntityTypes.CHEST || type == BlockEntityTypes.TRAPPED_CHEST)) {
                result.add(pos);
            }
        }
        return result;
    }

    public static class a {
        private final BlockPos b;
        private long c;
        private final CounterUtil a = new CounterUtil();
        private int d = ServerUtil.a.d();

        @Generated
        public CounterUtil b() {
            return this.a;
        }

        @Generated
        public BlockPos c() {
            return this.b;
        }

        @Generated
        public long d() {
            return this.c;
        }

        @Generated
        public int e() {
            return this.d;
        }

        public a(BlockPos chestPos, long current) {
            this.b = chestPos;
            this.c = current;
            this.a.b();
        }

        public void a(long current) {
            if (Math.abs((current / 1000) - (a() / 1000)) > 5) {
                this.c = current;
                this.a.b();
                this.d = ServerUtil.a.d();
            }
        }

        public long a() {
            return Math.max(0L, this.c - this.a.c());
        }
    }
}



