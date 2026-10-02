package platform.client.features.modules.render;

import static platform.api.module.Interface.aM_;

import platform.api.system.configs.ThemeInfo;
import platform.api.module.Category;
import platform.client.Delta;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.DrawEvent;
import platform.client.utils.render.AnimationUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.client.utils.math.ProjectUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

@ModuleRegister(a = "Predictions", b = "Прогнозирует и отображает траекторию полёта трезубца, стрел и зелий", c = Category.Render)
public class Predictions extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Отслеживаемые предметы", new BooleanSetting("Стрелы", true), new BooleanSetting("Трезубцы", true), new BooleanSetting("Эндер жемчуг", true), new BooleanSetting("Зелья", true));
    private final BooleanSetting c = new BooleanSetting("Радужный цвет", false);
    private final Map<Integer, b> d = new HashMap();

    static final class b {
        private final Vec3 a;
        private final int b;
        private final ItemStack c;
        private final AnimationUtil d;

        b(Vec3 impact, int ticks, ItemStack item, AnimationUtil anim) {
            this.a = impact;
            this.b = ticks;
            this.c = item;
            this.d = anim;
        }

        public Vec3 a() {
            return this.a;
        }

        public int b() {
            return this.b;
        }

        public ItemStack c() {
            return this.c;
        }

        public AnimationUtil d() {
            return this.d;
        }
    }

    public Predictions() {
        a(this.b, this.c);
    }

    private boolean a(Entity entity) {
        return (entity.getX() == entity.xOld && entity.getY() == entity.yOld && entity.getZ() == entity.zOld) ? false : true;
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (aM_.player == null || aM_.level == null) {
            return;
        }
        if (event.c()) {
            Set<Integer> activeIds = new HashSet<>();
            AABB range = aM_.player.getBoundingBox().inflate(aM_.options.getEffectiveRenderDistance() * 16);
            if (this.b.a("Стрелы").c().booleanValue()) {
                aM_.level.getEntitiesOfClass(Arrow.class, range, (v1) -> {
                    return a(v1);
                }).forEach(e -> {
                    a(event, e, Items.ARROW.getDefaultInstance(), activeIds);
                });
            }
            if (this.b.a("Трезубцы").c().booleanValue()) {
                aM_.level.getEntitiesOfClass(ThrownTrident.class, range, e2 -> {
                    return e2.clientSideReturnTridentTickCount <= 0 && a((Entity) e2);
                }).forEach(e3 -> {
                    a(event, e3, Items.TRIDENT.getDefaultInstance(), activeIds);
                });
            }
            if (this.b.a("Эндер жемчуг").c().booleanValue()) {
                aM_.level.getEntitiesOfClass(ThrownEnderpearl.class, range, (v1) -> {
                    return a(v1);
                }).forEach(e4 -> {
                    a(event, e4, Items.ENDER_PEARL.getDefaultInstance(), activeIds);
                });
            }
            if (this.b.a("Зелья").c().booleanValue()) {
                aM_.level.getEntitiesOfClass(ThrownSplashPotion.class, range, (v1) -> {
                    return a(v1);
                }).forEach(e5 -> {
                    a(event, e5, e5.getItem(), activeIds);
                });
            }
            b(event);
            this.d.entrySet().removeIf(entry -> {
                if (activeIds.contains(entry.getKey())) {
                    return false;
                }
                AnimationUtil anim = ((b) entry.getValue()).d();
                anim.a(false);
                anim.a(0.0f, 1.0f, 0.25f, EasingList.s, event.g());
                return anim.c() <= 0.0f;
            });
        }
        if (event.b()) {
            this.d.values().forEach(info -> {
                a(event, info);
            });
        }
    }

    private void a(DrawEvent event, Entity entity, ItemStack item, Set<Integer> activeIds) {
        List<Vec3> path = b(entity);
        if (path.size() < 2) {
            return;
        }
        activeIds.add(Integer.valueOf(entity.getId()));
        b existing = this.d.get(Integer.valueOf(entity.getId()));
        AnimationUtil anim = existing != null ? existing.d() : new AnimationUtil();
        anim.a(true);
        anim.a(0.0f, 1.0f, 0.25f, EasingList.s, event.g());
        float alpha = anim.c();
        int primaryColor = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        float hueBase = (entity.getUUID().getLeastSignificantBits() & 65535) / 65535.0f;
        int segCount = path.size() - 1;
        for (int i = 0; i < segCount; i++) {
            float max = Math.max(0.0f, Math.min(1.0f, (alpha * segCount) - i));
            if (max <= 0.0f) {
                break;
            }
            float hue = (((hueBase + ((float) ((path.get(i).x * 0.05000000070627959d) + (path.get(i).z * 0.05000000070627959d)))) % 1.0f) + 1.0f) % 1.0f;
            int base = this.c.c().booleanValue() ? (-16777216) | (Color.HSBtoRGB(hue, 1.0f, 1.0f) & 16777215) : primaryColor;
            int lineAlpha = (int) (255.0f * (0.3f + (0.7f * (1.0f - (i / segCount)))) * max * alpha);
            event.e().a(event.h(), path.get(i), path.get(i + 1), null, (base & 16777215) | (lineAlpha << 24), 1.5f);
        }
        this.d.put(Integer.valueOf(entity.getId()), new b((Vec3) path.get(path.size() - 1), segCount, item, anim));
    }

    private void a(DrawEvent event, b info) {
        float alpha = info.d().c();
        if (alpha <= 0.0f) {
            return;
        }
        Vector2f screen = ProjectUtil.a(info.a().x, info.a().y, info.a().z);
        if (ProjectUtil.a(screen)) {
            float iconSize = Fonts.e.a(7.25f) * 7.25f;
            String format = String.format(Locale.US, "%.1fs", Float.valueOf(info.b() / 20.0f));
            float width = (2.0f * 3.0f) + iconSize + Fonts.e.a(format, 7.25f);
            float height = iconSize + (2.0f * 2.0f);
            float x = screen.x() - (width / 2.0f);
            float y = screen.y() - (height / 2.0f);
            event.i().pose().pushMatrix();
            event.i().pose().translate(screen.x(), screen.y());
            event.i().pose().scale(0.8f + (alpha * 0.2f), 0.8f + (alpha * 0.2f));
            event.i().pose().translate(-screen.x(), -screen.y());
            event.d().a(event.i(), x, y, width + 1.0f, height, 2.0f, ColorUtil.a(0, 0, 0, (int) (130.0f * alpha)));
            event.e().a(event.i(), info.c(), x + 2.0f, (y + 2.0f) - 0.25f, 0, alpha, iconSize / 16.0f, false);
            Fonts.e.a(event.i(), Component.literal(format), x + (2.0f * 2.0f) + iconSize, y + 2.0f, 7.25f, alpha);
            event.i().pose().popMatrix();
        }
    }

    private List<Vec3> b(Entity entity) {
        Vec3 vel = entity.getDeltaMovement();
        Vec3 pos = entity.position();
        boolean isThrowable = entity instanceof ThrowableItemProjectile;
        double gravity = entity instanceof ThrownSplashPotion ? 0.05000000070627959d : 0.030000000582077163d;
        List<Vec3> path = new ArrayList<>();
        path.add(pos);
        for (int i = 0; i < 140 && vel.lengthSqr() >= 1.0000000000139336E-6d && pos.y >= aM_.level.getMinY() && pos.y <= aM_.level.getMinY() + aM_.level.getHeight(); i++) {
            double drag = aM_.level.getFluidState(BlockPos.containing(pos)).is(FluidTags.WATER) ? isThrowable ? 0.7999999144424994d : 0.6000000001891753d : 0.990000120151185d;
            if (isThrowable) {
                vel = new Vec3(vel.x * drag, (vel.y - gravity) * drag, vel.z * drag);
            }
            Vec3 next = pos.add(vel);
            BlockHitResult impact = aM_.level.clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
            if (impact.getType() != HitResult.Type.MISS) {
                path.add(impact.getLocation());
                return path;
            }
            pos = next;
            if (!isThrowable) {
                vel = new Vec3(vel.x * drag, (vel.y * drag) - 0.05000000070627959d, vel.z * drag);
            }
            path.add(pos);
        }
        return path;
    }

    private void b(DrawEvent event) {
        ItemStack mainStack = aM_.player.getItemInHand(InteractionHand.MAIN_HAND);
        Item main = mainStack.getItem();
        Item off = aM_.player.getItemInHand(InteractionHand.OFF_HAND).getItem();
        float speed = 0.0f;
        boolean isThrowable = false;
        boolean potion = main == Items.SPLASH_POTION || main == Items.LINGERING_POTION || off == Items.SPLASH_POTION || off == Items.LINGERING_POTION;
        if ((main instanceof BowItem) && this.b.a("Стрелы").c().booleanValue()) {
            float pull = q();
            if (pull < 0.1f) {
                return;
            } else {
                speed = pull * 3.0f;
            }
        } else if ((main instanceof CrossbowItem) && this.b.a("Стрелы").c().booleanValue()) {
            speed = 3.0f;
        } else if ((main instanceof TridentItem) && this.b.a("Трезубцы").c().booleanValue()) {
            speed = 2.5f;
        } else if ((main == Items.ENDER_PEARL || off == Items.ENDER_PEARL) && this.b.a("Эндер жемчуг").c().booleanValue()) {
            speed = 1.5f;
            isThrowable = true;
        } else if (potion && this.b.a("Зелья").c().booleanValue()) {
            speed = 0.5f;
            isThrowable = true;
        }
        if (speed == 0.0f) {
            return;
        }
        float[] viewSpread = ((main instanceof CrossbowItem) && a(mainStack)) ? new float[]{-10.0f, 0.0f, 10.0f} : new float[]{0.0f};
        for (float viewSpreadDegrees : viewSpread) {
            a result = a(speed, isThrowable, potion ? -20.0f : 0.0f, potion ? 0.05000000070627959d : 0.030000000582077163d, viewSpreadDegrees, event.g());
            if (result.a().size() >= 2) {
                if (result.c() != null) {
                    event.e().a(event.h(), result.c().getBoundingBox(), ColorUtil.a(255, 100, 100, InterfaceC0020Opcode.aN), 1.0f);
                } else if (result.b() != null && result.d() != null) {
                    a(event, (Vec3) result.a().get(result.a().size() - 1), 0.33f, ColorUtil.a(255, 255, 255, InterfaceC0020Opcode.aN), result.d());
                }
            }
        }
    }

    private void a(DrawEvent event, Vec3 center, double radius, int color, Direction face) {
        Direction.Axis axis = face.getAxis();
        Vec3 u = axis == Direction.Axis.Y ? new Vec3(1.0d, 0.0d, 0.0d) : new Vec3(0.0d, 1.0d, 0.0d);
        Vec3 class_243Var = (axis != Direction.Axis.X && axis == Direction.Axis.Z) ? new Vec3(1.0d, 0.0d, 0.0d) : new Vec3(0.0d, 0.0d, 1.0d);
        Vec3 v = class_243Var;
        double step = 6.283186671116134d / ((double) 8);
        double controlRadius = radius / Math.cos(step / 2.0d);
        for (int i = 0; i < 8; i++) {
            double a1 = step * ((double) i);
            double a2 = step * ((double) (i + 1));
            double am = a1 + (step / 2.0d);
            event.e().a(event.h(), a(center, u, v, radius, a1), a(center, u, v, radius, a2), a(center, u, v, controlRadius, am), color, 1.5f);
        }
    }

    private Vec3 a(Vec3 center, Vec3 u, Vec3 v, double radius, double angle) {
        return center.add(u.scale(Math.cos(angle) * radius)).add(v.scale(Math.sin(angle) * radius));
    }

    private boolean a(ItemStack stack) {
        return ((ItemEnchantments) stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)).getLevel(aM_.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MULTISHOT)) > 0;
    }

    private float q() {
        ItemStack active = aM_.player.getUseItem();
        if (!aM_.player.isUsingItem() || !(active.getItem() instanceof BowItem)) {
            return 0.0f;
        }
        int useTicks = active.getItem().getUseDuration(active, aM_.player) - aM_.player.getUseItemRemainingTicks();
        float f = useTicks / 20.0f;
        return Math.min(((f * f) + (f * 2.0f)) / 3.0f, 1.0f);
    }

    static final class a {
        private final List<Vec3> a;
        private final BlockPos b;
        private final Entity c;
        private final Direction d;

        a(List<Vec3> path, BlockPos hitBlock, Entity hitEntity, Direction hitSide) {
            this.a = path;
            this.b = hitBlock;
            this.c = hitEntity;
            this.d = hitSide;
        }

        public List<Vec3> a() {
            return this.a;
        }

        public BlockPos b() {
            return this.b;
        }

        public Entity c() {
            return this.c;
        }

        public Direction d() {
            return this.d;
        }
    }

    private a a(float speed, boolean isThrowable, float pitchOffset, double gravity, float viewSpreadDegrees, float tickDelta) {
        double pitchRad = Math.toRadians(aM_.player.getXRot(tickDelta));
        double yawRad = Math.toRadians(aM_.player.getYRot(tickDelta));
        Vec3 look = new Vec3((-Math.sin(yawRad)) * Math.cos(pitchRad), -Math.sin(Math.toRadians(aM_.player.getXRot(tickDelta) + pitchOffset)), Math.cos(yawRad) * Math.cos(pitchRad)).normalize();
        if (viewSpreadDegrees != 0.0f) {
            Vec3 right = new Vec3(0.0d, 1.0d, 0.0d).cross(look);
            Vec3 axis = look.cross(right.lengthSqr() < 9.999996190428959E-11d ? new Vec3(Math.cos(yawRad), 0.0d, Math.sin(yawRad)) : right.normalize()).normalize();
            double rad = Math.toRadians(viewSpreadDegrees);
            double c = Math.cos(rad);
            double s = Math.sin(rad);
            look = look.scale(c).add(axis.cross(look).scale(s)).add(axis.scale(axis.dot(look) * (1.0d - c)));
        }
        Vec3 vel = look.scale(speed).add(aM_.player.getDeltaMovement().x, aM_.player.onGround() ? 0.0d : aM_.player.getDeltaMovement().y, aM_.player.getDeltaMovement().z);
        Vec3 pos = aM_.player.getEyePosition(tickDelta);
        List<Vec3> path = new ArrayList<>();
        path.add(pos);
        for (int i = 0; i < 130 && vel.lengthSqr() >= 1.0000000000139336E-6d && pos.y >= aM_.level.getMinY() && pos.y <= aM_.level.getMinY() + aM_.level.getHeight(); i++) {
            double drag = aM_.level.getFluidState(BlockPos.containing(pos)).is(FluidTags.WATER) ? isThrowable ? 0.7999999144424994d : 0.6000000001891753d : 0.990000120151185d;
            if (isThrowable) {
                vel = new Vec3(vel.x * drag, (vel.y - gravity) * drag, vel.z * drag);
            }
            Vec3 next = pos.add(vel);
            BlockHitResult hitBlock = aM_.level.clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, aM_.player));
            Vec3 end = hitBlock.getType() != HitResult.Type.MISS ? hitBlock.getLocation() : next;
            Entity hitEntity = a(pos, end);
            if (hitEntity != null) {
                path.add((Vec3) hitEntity.getBoundingBox().inflate(0.30000001176381136d).clip(pos, end).orElse(end));
                return new a(path, null, hitEntity, null);
            }
            if (hitBlock.getType() != HitResult.Type.MISS) {
                path.add(hitBlock.getLocation());
                return new a(path, hitBlock.getBlockPos(), null, hitBlock.getDirection());
            }
            pos = next;
            if (!isThrowable) {
                vel = new Vec3(vel.x * drag, (vel.y * drag) - 0.05000000070627959d, vel.z * drag);
            }
            path.add(pos);
        }
        return new a(path, null, null, null);
    }

    private Entity a(Vec3 start, Vec3 end) {
        Entity closest = null;
        double closestDist = 1.7976922776554332E308d;
        for (Entity candidate : aM_.level.getEntities(aM_.player, new AABB(start, end).inflate(1.0d), e -> e.isAlive() && !e.isSpectator() && (e instanceof LivingEntity))) {
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.30000001176381136d).clip(start, end);
            if (hit.isPresent()) {
                double dist = start.distanceToSqr(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = candidate;
                }
            }
        }
        return closest;
    }
}


