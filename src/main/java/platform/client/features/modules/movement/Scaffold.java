package platform.client.features.modules.movement;

import platform.inject.invokers.MinecraftInvoker;
import platform.api.handlers.UseableHandler;
import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.rotation.Look;
import platform.client.utils.player.MoveUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.event.GlobalEvent;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.HotbarEvent;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.BooleanSetting;
import platform.client.utils.rotation.Rotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;

@ModuleRegister(a = "Scaffold", b = "Автоматически ставит блоки под вами", c = Category.Movement)
public class Scaffold extends Module {
    private a c;
    private final BooleanSetting b = new BooleanSetting("Избегать падения", false);
    private Vec3 d = Vec3.ZERO;
    private final int[] e = {-1, -1, -1};

    public Scaffold() {
        a(this.b);
    }

    @EventTarget
    public void a(InputEvent e) {
        if (r()) {
            return;
        }
        if (s() != null) {
            MoveUtil.a(e, Look.b(), 1);
            if (this.b.c().booleanValue()) {
                Delta.h().d().t().ah().b(e);
            }
        }
        if (a(aM_.player.getMainHandItem()) || a(aM_.player.getOffhandItem())) {
            return;
        }
        int hotbarSlot = d(true);
        if (hotbarSlot != -1) {
            if (aM_.player.getInventory().getSelectedSlot() != hotbarSlot && this.e[2] < 7 && Delta.h().d().v().a().a().isEmpty()) {
                aM_.player.getInventory().setSelectedSlot(hotbarSlot);
                this.e[2] = 9;
                return;
            }
            return;
        }
        int invSlot = d(false);
        if (this.e[2] < 5 && invSlot != -1 && Delta.h().d().v().a().a().isEmpty()) {
            if (this.e[1] == -1) {
                this.e[1] = invSlot;
            }
            if (aM_.player.getInventory().getSelectedSlot() != 5) {
                aM_.player.getInventory().setSelectedSlot(5);
            }
            Delta.h().d().v().a().a(invSlot, 5, 1);
            this.e[2] = 9;
        }
    }

    @EventTarget
    public void a(HotbarEvent event) {
        if (this.e[2] <= 5 || d(false) == -1) {
            return;
        }
        event.a(true);
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (!event.b() || !(event.d() instanceof ServerboundSetCarriedItemPacket)) {
            return;
        }
        this.e[2] = 9;
    }

    @EventTarget
    public void a(TickEvent e) {
        int[] iArr = this.e;
        iArr[2] = iArr[2] - 1;
    }

    @EventTarget
    public void a(GlobalEvent e) {
        if (s() == null || r()) {
            return;
        }
        if (this.c == null || !a(this.c)) {
            this.c = null;
            for (BlockPos target : q()) {
                this.c = b(target);
                if (this.c != null) {
                    break;
                }
            }
        }
        if (this.c == null) {
            return;
        }
        Rotation rotation = b(this.c);
        Delta.h().d().k().a(rotation, 100.0f, 7, 1);
        if (u() && !v() && !Delta.h().d().v().c().a()) {
            ((platform.inject.invokers.MinecraftInvoker) aM_).invokeDoItemUse();
            this.e[2] = 9;
            this.c = null;
        }
    }

    private boolean a(a data) {
        BlockPos placePos = data.a.relative(data.b);
        return a(aM_.level.getBlockState(placePos)) && a(data.a) && !a(data.a, data.b).isEmpty();
    }

    private List<Vec3> a(BlockPos pos, Direction face) {
        List<Vec3> points = new ArrayList<>();
        Vec3 eye = t();
        double reach = aM_.player.blockInteractionRange();
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        double[] offsets = {0.0d, -0.20000000236855192d, 0.200000000060146d, -0.3500000030268554d, 0.3500000598673184d, -0.4500000079401218d, 0.4499998886196472d};
        for (double u : offsets) {
            for (double v : offsets) {
                Vec3 point = a(pos, face, u, v);
                if (eye.distanceTo(point) <= reach && eye.subtract(point).normalize().dot(normal) > 0.1000000076546522d) {
                    BlockHitResult class_3965VarMethod_17742 = aM_.level.clip(new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, aM_.player));
                    if (class_3965VarMethod_17742.getType() == HitResult.Type.MISS) {
                        points.add(point);
                    } else if (class_3965VarMethod_17742 instanceof BlockHitResult) {
                        BlockHitResult block = class_3965VarMethod_17742;
                        if (block.getBlockPos().equals(pos)) {
                            points.add(point);
                        }
                    }
                }
            }
        }
        return points;
    }

    private List<BlockPos> q() {
        List<BlockPos> list = new ArrayList<>();
        this.d = this.d.scale(0.6000003608875443d).add(new Vec3(aM_.player.getX() - aM_.player.xo, 0.0d, aM_.player.getZ() - aM_.player.zo).scale(0.200000000060146d));
        for (int i = 0; i <= 2; i++) {
            Vec3 at = aM_.player.position().add(this.d.scale(i));
            BlockPos pos = BlockPos.containing(at.x, aM_.player.getY() - 1.0d, at.z);
            if (!list.contains(pos) && a(aM_.level.getBlockState(pos))) {
                list.add(pos);
            }
        }
        list.sort(Comparator.comparingDouble(pos2 -> {
            return pos2.distToCenterSqr(aM_.player.position());
        }));
        return list;
    }

    private boolean r() {
        List<UseableHandler.a> tasks = Delta.h().d().v().b().a();
        return !tasks.isEmpty() && ((UseableHandler.a) tasks.getFirst()).d() <= 1;
    }

    @Override
    public void b() {
        super.b();
        this.c = null;
        this.d = Vec3.ZERO;
        if (aM_.player != null) {
            this.e[0] = aM_.player.getInventory().getSelectedSlot();
        }
    }

    @Override
    public void c() {
        super.c();
        if (this.e[0] != -1 && aM_.player != null) {
            aM_.player.getInventory().setSelectedSlot(this.e[0]);
            if (this.e[1] != -1) {
                Delta.h().d().v().a().a(this.e[1], 5, 1);
            }
        }
        this.e[0] = -1;
        this.e[1] = -1;
        this.e[2] = -1;
        this.c = null;
        this.d = Vec3.ZERO;
    }

    private int d(boolean hotbarOnly) {
        int end = hotbarOnly ? 9 : 36;
        for (int i = 0; i < end; i++) {
            if (a(aM_.player.getInventory().getItem(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean a(ItemStack stack) {
        if (!stack.isEmpty()) {
            BlockItem class_1747VarMethod_7909 = (BlockItem) stack.getItem();
            if (class_1747VarMethod_7909 instanceof BlockItem) {
                BlockItem item = class_1747VarMethod_7909;
                if (item.getBlock().defaultBlockState().isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }

    private InteractionHand s() {
        if (a(aM_.player.getMainHandItem())) {
            return InteractionHand.MAIN_HAND;
        }
        if (a(aM_.player.getOffhandItem())) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private boolean a(BlockState state) {
        if (state.canBeReplaced()) {
            return true;
        }
        return state.getBlock() == Blocks.SNOW && ((Integer) state.getValue(SnowLayerBlock.LAYERS)).intValue() < 8;
    }

    private boolean a(BlockPos pos) {
        BlockState state = aM_.level.getBlockState(pos);
        return (state.isAir() || state.getBlock() == Blocks.SNOW || state.canBeReplaced() || state.getCollisionShape(aM_.level, pos).isEmpty()) ? false : true;
    }

    private a b(BlockPos pos) {
        a data = c(pos);
        if (data != null) {
            return data;
        }
        int[][] offsets = {new int[]{-1, 0, 0}, new int[]{1, 0, 0}, new int[]{0, 0, -1}, new int[]{0, 0, 1}, new int[]{-1, 0, -1}, new int[]{1, 0, 1}, new int[]{-1, 0, 1}, new int[]{1, 0, -1}, new int[]{0, -1, 0}, new int[]{-1, -1, 0}, new int[]{1, -1, 0}, new int[]{0, -1, -1}, new int[]{0, -1, 1}};
        Vec3 feet = aM_.player.position();
        return Arrays.stream(offsets).map(o -> {
            return pos.offset(o[0], o[1], o[2]);
        }).sorted(Comparator.comparingDouble(p -> {
            return p.distToCenterSqr(feet);
        })).map(this::c).filter((v0) -> {
            return Objects.nonNull(v0);
        }).findFirst().orElse(null);
    }

    private a c(BlockPos pos) {
        if (!a(aM_.level.getBlockState(pos))) {
            return null;
        }
        a best = null;
        int bestCount = 0;
        for (Direction face : Direction.values()) {
            BlockPos neighbor = pos.relative(face);
            if (a(neighbor)) {
                Direction placeFace = face.getOpposite();
                List<Vec3> points = a(neighbor, placeFace);
                if (points.size() > bestCount) {
                    bestCount = points.size();
                    best = new a(neighbor, placeFace, new BlockHitResult(a(neighbor, placeFace, 0.0d, 0.0d), placeFace, neighbor, false));
                }
            }
        }
        return best;
    }

    private Rotation b(a data) {
        Vec3 eye = t();
        List<Vec3> points = a(data.a, data.b);
        if (points.isEmpty()) {
            return Rotation.a(eye, data.c.getLocation());
        }
        Vec3 center = points.stream().reduce(Vec3.ZERO, (v0, v1) -> {
            return v0.add(v1);
        }).scale(1.0d / ((double) points.size()));
        Vec3 best = points.stream().min(Comparator.comparingDouble(point -> {
            return point.distanceToSqr(center);
        })).orElse(center);
        return Rotation.a(eye, best);
    }

    private Vec3 t() {
        Vec3 eye = aM_.player.getEyePosition();
        double fall = aM_.player.getDeltaMovement().y;
        return fall < 0.0d ? eye.add(0.0d, fall * 0.5d, 0.0d) : eye;
    }

    private AABB d(BlockPos pos) {
        VoxelShape shape = aM_.level.getBlockState(pos).getCollisionShape(aM_.level, pos);
        return shape.isEmpty() ? new AABB(0.0d, 0.0d, 0.0d, 1.0d, 1.0d, 1.0d) : shape.bounds();
    }

    private Vec3 a(BlockPos pos, Direction face, double u, double v) {
        AABB shape = d(pos);
        double cx = ((double) pos.getX()) + ((shape.minX + shape.maxX) / 2.0d);
        double cy = ((double) pos.getY()) + ((shape.minY + shape.maxY) / 2.0d);
        double cz = ((double) pos.getZ()) + ((shape.minZ + shape.maxZ) / 2.0d);
        double sx = shape.getXsize();
        double sy = shape.getYsize();
        double sz = shape.getZsize();
        switch (AnonymousClass1.a[face.ordinal()]) {
            case 1:
                return new Vec3(cx + (u * sx), ((double) pos.getY()) + shape.maxY, cz + (v * sz));
            case 2:
                return new Vec3(cx + (u * sx), ((double) pos.getY()) + shape.minY, cz + (v * sz));
            case 3:
                return new Vec3(cx + (u * sx), cy + (v * sy), ((double) pos.getZ()) + shape.minZ);
            case 4:
                return new Vec3(cx + (u * sx), cy + (v * sy), ((double) pos.getZ()) + shape.maxZ);
            case 5:
                return new Vec3(((double) pos.getX()) + shape.minX, cy + (v * sy), cz + (u * sz));
            case 6:
                return new Vec3(((double) pos.getX()) + shape.maxX, cy + (v * sy), cz + (u * sz));
            default:
                return Vec3.ZERO;
        }
    }

    static class AnonymousClass1 {
        static final int[] a = new int[Direction.values().length];

        static {
            try {
                a[Direction.UP.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[Direction.DOWN.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                a[Direction.NORTH.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                a[Direction.SOUTH.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                a[Direction.WEST.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                a[Direction.EAST.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    private boolean u() {
        BlockHitResult class_3965Var = (aM_.hitResult) instanceof BlockHitResult ? (BlockHitResult) aM_.hitResult : null;
        if (!(class_3965Var instanceof BlockHitResult)) {
            return false;
        }
        BlockHitResult hit = class_3965Var;
        if (hit.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos placePos = this.c.a.relative(this.c.b);
        if (hit.getBlockPos().equals(this.c.a) && hit.getDirection() == this.c.b) {
            return true;
        }
        return hit.getBlockPos().equals(placePos) && a(aM_.level.getBlockState(placePos));
    }

    private boolean v() {
        BlockPos placePos = this.c.a.relative(this.c.b);
        return !aM_.level.getEntitiesOfClass(Entity.class, new AABB(placePos), entity -> {
            return !entity.isSpectator() && entity.isAlive();
        }).isEmpty();
    }

    static final class a {
        final BlockPos a;
        final Direction b;
        final BlockHitResult c;

        a(BlockPos pos, Direction face, BlockHitResult result) {
            this.a = pos;
            this.b = face;
            this.c = result;
        }
        public BlockPos a() {
            return this.a;
        }

        public Direction b() {
            return this.b;
        }

        public BlockHitResult c() {
            return this.c;
        }
    }
}


