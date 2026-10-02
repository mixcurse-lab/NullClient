package platform.client.features.modules.combat;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.rotation.Look;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.ClickEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.event.events.player.WillLandEvent;

import platform.client.utils.rotation.Rotation;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.BlockPos;

@ModuleRegister(a = "Projectile Helper", b = "Помогает целиться по противнику при стрельбе из лука или трезубца", c = Category.Combat)
public class ProjectileHelper extends Module {
    private int d;
    private int e;
    private boolean g;
    private boolean h;
    private final LivingEntity[] b = new LivingEntity[2];
    private final Vec3[] c = new Vec3[5];
    private boolean f = true;

    public LivingEntity q() {
        return this.b[0];
    }

    public boolean r() {
        if (this.b[0] == null || aM_.player == null || !aM_.player.isUsingItem()) {
            return false;
        }
        ItemStack active = aM_.player.getActiveItem();
        return active.getItem().getUseDuration(active, aM_.player) - aM_.player.getUseItemRemainingTicks() > 2;
    }

    @Override
    public void c() {
        super.c();
        s();
    }

    @EventTarget
    public void a(WillLandEvent event) {
        this.g = event.b();
    }

    @EventTarget
    public void a(TickEvent event) {
        Rotation aim;
        if (aM_.player == null || aM_.level == null) {
            return;
        }
        ItemStack stack = aM_.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(stack.getItem() instanceof BowItem) && !(stack.getItem() instanceof TridentItem)) {
            s();
            return;
        }
        if (!aM_.player.isUsingItem()) {
            this.f = true;
        }
        if (!this.f) {
            s();
            return;
        }
        this.h = aM_.player.input.keyPresses.jump() && (aM_.player.onGround() || this.g);
        a(t());
        if (this.b[0] != null) {
            Vec3[] class_243VarArr = this.c;
            int i = this.e;
            this.e = i + 1;
            class_243VarArr[i % this.c.length] = new Vec3(this.b[0].getX() - this.b[0].xOld, 0.0d, this.b[0].getZ() - this.b[0].zOld);
        }
        if (r() && (aim = a(stack)) != null) {
            Delta.h().d().k().a(aim, 180.0f, 1, 1);
        }
    }

    @EventTarget
    public void a(ClickEvent event) {
        if (event.b() && event.h() == 0 && aM_.player != null && aM_.player.isUsingItem()) {
            this.f = !this.f;
        }
    }

    private void s() {
        LivingEntity[] class_1309VarArr = this.b;
        this.b[1] = null;
        class_1309VarArr[0] = null;
        this.d = 0;
        Arrays.fill(this.c, (Object) null);
    }

    private LivingEntity t() {
        Vec3 eye = aM_.player.getEyePosition();
        Vec3 look = Vec3.directionFromRotation(Look.c(), Look.b());
        List<Player> players = aM_.level.getEntitiesOfClass(Player.class, new AABB(aM_.player.blockPosition()).inflate(120.0d), e -> e != aM_.player && e.isAlive() && !Delta.h().d().e().d(e.getName().getString()));
        return players.stream()
                .filter(e -> eye.distanceToSqr(e.getBoundingBox().getCenter()) <= 14400.0d)
                .min(Comparator.comparingDouble(e2 -> -look.dot(e2.getBoundingBox().getCenter().subtract(eye).normalize())))
                .map(e -> (LivingEntity) e)
                .orElse(null);
    }

    private void a(LivingEntity best) {
        if (best != this.b[1]) {
            this.b[1] = best;
            this.d = 0;
        } else {
            this.d++;
        }
        if (this.b[0] != this.b[1]) {
            if (this.b[0] == null || this.d >= 4) {
                this.b[0] = this.b[1];
                Arrays.fill(this.c, (Object) null);
            }
        }
    }

    private Vec3 u() {
        Vec3 sum = Vec3.ZERO;
        int count = 0;
        for (Vec3 entry : this.c) {
            if (entry != null && entry.horizontalDistanceSqr() > 1.000000229429758E-6d) {
                sum = sum.add(entry);
                count++;
            }
        }
        return count == 0 ? Vec3.ZERO : sum.scale(1.0d / ((double) count));
    }

    private Rotation a(ItemStack stack) {
        Vec3 shooter = v();
        Vec3 origin = aM_.player.getEyePosition().add(0.0d, -0.1000000074661073d, 0.0d);
        double speed = stack.getItem() instanceof BowItem ? b(stack) : 2.5d;
        AABB box = this.b[0].getBoundingBox();
        Vec3 motion = u();
        Vec3 aim = box.getCenter();
        float yaw = 0.0f;
        float pitch = 0.0f;
        for (int i = 0; i < 6; i++) {
            yaw = a(origin, aim);
            pitch = a(origin, aim, shooter, speed);
            double[] shot = a(origin, Vec3.directionFromRotation(pitch, yaw).scale(speed).add(shooter), Math.hypot(aim.x - origin.x, aim.z - origin.z), true);
            if (shot == null) {
                return null;
            }
            Vec3 moved = box.getCenter().add(motion.scale(Math.min(shot[1] + 6.0d, 13.0d)));
            if (moved.distanceToSqr(aim) < 9.999996044721066E-5d) {
                break;
            }
            aim = moved;
        }
        Rotation rotation = new Rotation(Mth.wrapDegrees(yaw), pitch);
        return rotation;
    }

    private float a(Vec3 origin, Vec3 aim, Vec3 shooter, double speed) {
        float low = -90.0f;
        float high = 90.0f;
        float yaw = a(origin, aim);
        double target = Math.hypot(aim.x - origin.x, aim.z - origin.z);
        double height = aim.y - origin.y;
        for (int i = 0; i < 24; i++) {
            float middle = (low + high) / 2.0f;
            double[] shot = a(origin, Vec3.directionFromRotation(middle, yaw).scale(speed).add(shooter), target, false);
            if (shot == null || shot[0] >= height) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return (low + high) / 2.0f;
    }

    private double[] a(Vec3 origin, Vec3 velocity, double target, boolean blocked) {
        Vec3 position = origin;
        Vec3 current = velocity;
        double travelled = 0.0d;
        for (int tick = 1; tick <= 100; tick++) {
            Vec3 next = position.add(current);
            if (blocked && aM_.level.clip(new ClipContext(position, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, aM_.player)).getType() != HitResult.Type.MISS) {
                return null;
            }
            double reached = Math.hypot(next.x - origin.x, next.z - origin.z);
            if (reached >= target) {
                double alpha = reached == travelled ? 1.0d : (target - travelled) / (reached - travelled);
                return new double[]{Mth.lerp((float) alpha, (float) position.y, (float) next.y) - origin.y, ((double) (tick - 1)) + alpha};
            }
            position = next;
            travelled = reached;
            current = current.scale(a(position) ? 0.6000002908794272d : 0.9900000228356232d).add(0.0d, -0.050000001868616015d, 0.0d);
        }
        return null;
    }

    private boolean a(Vec3 position) {
        return aM_.level.getBlockState(BlockPos.containing(position)).getFluidState().is(FluidTags.WATER);
    }

    private Vec3 v() {
        Vec3 velocity = new Vec3(aM_.player.getX() - aM_.player.xOld, aM_.player.getY() - aM_.player.yOld, aM_.player.getZ() - aM_.player.zOld);
        if (!this.h) {
            return new Vec3(velocity.x, aM_.player.onGround() ? 0.0d : velocity.y, velocity.z);
        }
        float yaw = aM_.player.getYRot() * 0.017453292f;
        double sprint = aM_.player.isSprinting() ? 0.19999997617511883d : 0.0d;
        return new Vec3(velocity.x - (((double) Mth.sin(yaw)) * sprint), Math.max(0.42f + aM_.player.getJumpBoostPower(), velocity.y), velocity.z + (((double) Mth.cos(yaw)) * sprint));
    }

    private double b(ItemStack stack) {
        float pull = 1.0f;
        ItemStack active = aM_.player.getActiveItem();
        if (aM_.player.isUsingItem() && (active.getItem() instanceof BowItem)) {
            float f = ((active.getItem().getUseDuration(active, aM_.player) - aM_.player.getUseItemRemainingTicks()) + 1.5f) / 20.0f;
            pull = Math.min(((f * f) + (f * 2.0f)) / 3.0f, 1.0f);
        }
        return ((double) pull) * 3.0d;
    }

    private float a(Vec3 from, Vec3 to) {
        return (float) Math.toDegrees(Math.atan2(-(to.x - from.x), to.z - from.z));
    }
}


