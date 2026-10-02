package platform.client.features.modules.combat;

import platform.client.features.modules.combat.AuraUtil;
import platform.client.ui.screen.GUIScreen;
import platform.inject.accessors.LocalPlayerAccessor;
import platform.inject.invokers.MinecraftInvoker;
import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.rotation.Look;
import platform.client.utils.math.MathUtil;
import platform.client.utils.player.MoveUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.event.events.player.WillLandEvent;

import platform.api.module.setting.BooleanSetting;
import platform.client.utils.timer.CounterUtil;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.MultiModeSetting;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Generated;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

@ModuleRegister(a = "Trigger Bot", b = "Автоматически наносит удар при наведении прицела на цель", c = Category.Combat)
public class TriggerBot extends Module {
    private int l;
    boolean c;
    public int d;
    private int m;
    private float n;
    private LivingEntity o;
    private final MultiModeSetting f = new MultiModeSetting("Цели для атаки", new BooleanSetting("Игроки", true), new BooleanSetting("Животные", false), new BooleanSetting("Мобы", false), new BooleanSetting("Друзья", true));
    private final MultiModeSetting g = new MultiModeSetting("Дополнительно", new BooleanSetting("Только критические удары", true), new BooleanSetting("Адаптивные удары", true), new BooleanSetting("Случайные промахи", true));
    private final MultiModeSetting h = new MultiModeSetting("Не бить когда", new BooleanSetting("Используется предмет", true), new BooleanSetting("Открыт контейнер", true), new BooleanSetting("Враг за стеной", false));
    private final ModeSetting i = new ModeSetting("Сброс спринта", "Легитный", "Легитный", "Рейдж");
    private final ModeSetting j = new ModeSetting("Выбор таргета", "Свободный", "Свободный", "Фиксирующий");
    BooleanSetting b = new BooleanSetting("Преследование цели", false);
    private final CounterUtil k = new CounterUtil();
    boolean e = false;

    @Generated
    public int r() {
        return this.d;
    }

    @Generated
    public LivingEntity s() {
        return this.o;
    }

    public TriggerBot() {
        a(this.f, this.g, this.h, this.i, this.j, this.b);
    }

    @Override
    public void c() {
        super.c();
        this.o = null;
        this.l = 0;
        this.d = 0;
        this.k.b();
    }

    @EventTarget
    public void a(InputEvent e) {
        if (this.b.c().booleanValue() && this.o != null) {
            MoveUtil.a(e, this.n, 3);
        }
        if (this.o != null) {
            Vec3 targetPosition = AuraUtil.a(aM_.player.getEyePosition(), this.o, 3.0d, true);
            this.n = targetPosition == Vec3.ZERO ? Look.b() : (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(targetPosition.z, targetPosition.x)) - 90.0d);
        }
        if (this.i.l("Легитный") && this.l > 0) {
            e.a(0.0f);
            e.b(0.0f);
            this.l--;
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        t();
    }

    @EventTarget
    public void a(WillLandEvent e) {
        this.c = e.b() && !aM_.player.onGround();
    }

    private void t() {
        this.d++;
        v();
        if (this.o != null && this.g.a("Случайные промахи").c().booleanValue() && this.d >= 2 && this.m >= 30 && (((Math.random() > 0.5d && this.d >= 1) || this.d == 4) && (!this.e || !AuraUtil.a(aM_.player.getYRot(), aM_.player.getXRot(), 3.0d, this.o, false)))) {
            ((MinecraftInvoker) aM_).invokeDoAttack();
            this.e = !this.e;
            this.m = (int) MathUtil.a(-10.0f, 10.0f);
        }
        if (this.o != null && AuraUtil.a(this.d, this.o, false)) {
            this.l = 1;
        }
        u();
    }

    private void u() {
        if (this.o == null || !q()) {
            return;
        }
        if (!AuraUtil.a(aM_.player.getYRot(), aM_.player.getXRot(), 3.0d, this.o, !this.h.a("Враг за стеной").c().booleanValue())) {
            return;
        }
        boolean skip = false;
        if ((Delta.h().d().t().H().t() || (aM_.player.fallDistance > 2.0f && Delta.h().d().t().H().r().c().booleanValue())) && InventoryUtil.b(Items.MACE) != -1) {
            if (aM_.player.fallDistance < 1.5f) {
                return;
            }
            double landDist = ((Double) MaceUtil.a(aM_.player, (Level) aM_.level).map(pos -> {
                return Double.valueOf(pos.distanceTo(this.o.position()));
            }).orElse(Double.valueOf(33.0d))).doubleValue();
            boolean hitNow = landDist > 2.0d;
            if ((!this.c && !MaceUtil.b() && Delta.h().d().t().H().q().c().booleanValue() && !hitNow) || !MaceUtil.a() || aM_.player.isFallFlying()) {
                return;
            } else {
                skip = true;
            }
        }
        if (skip || !w()) {
            aM_.gameMode.attack(aM_.player, this.o);
            aM_.player.swing(InteractionHand.MAIN_HAND);
            if (Math.random() <= 0.899999737739563d) {
                this.m++;
            }
            this.d = 0;
        }
    }

    private void v() {
        if (this.j.l("Фиксирующий")) {
            if (!a(this.o) || (MaceUtil.a() && !this.c && !aM_.player.getCooldowns().isOnCooldown(Items.MACE.getDefaultInstance()))) {
                this.o = d(false);
                return;
            }
            return;
        }
        LivingEntity aimed = d(true);
        if (aimed != null) {
            this.o = aimed;
            this.k.b();
        } else if (this.o != null && this.k.a(1000L)) {
            this.o = null;
        }
    }

    private boolean a(LivingEntity entity) {
        return entity != null && entity.isAlive() && !entity.isSpectator() && entity != aM_.player && b(entity) && d(entity);
    }

    private boolean b(LivingEntity entity) {
        if (Delta.h().d().t().G().m() && aM_.player.isFallFlying()) {
            return true;
        }
        return AuraUtil.a((Entity) entity, 4.0d + (aM_.player.getDeltaMovement().length() * 3.0d) + ((double) ((InventoryUtil.b(Items.MACE) == -1 || ((double) aM_.player.fallDistance) <= 1.5d) ? 0.0f : 1.5f)) + ((double) ((Delta.h().d().t().H().m() && InventoryUtil.b(Items.MACE) != -1 && ((Boolean) MaceUtil.a(aM_.player, (Level) aM_.level).map(p -> {
            return Boolean.valueOf(aM_.player.getY() - p.y > 2.0d);
        }).orElse(false)).booleanValue()) ? 10 : 0)));
    }

    private boolean c(LivingEntity entity) {
        return Stream.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET).anyMatch(s -> {
            return entity.getItemBySlot(s).get(DataComponents.EQUIPPABLE) != null;
        });
    }

    private boolean w() {
        if (!((LocalPlayerAccessor) (Object) aM_.player).getWasSprinting() || aM_.player.isInWater() || aM_.player.isInLava() || aM_.player.isSwimming() || aM_.player.onGround()) {
            return false;
        }
        if (this.i.l("Рейдж")) {
            ((LocalPlayerAccessor) (Object) aM_.player).setWasSprinting(false);
            aM_.player.setSprinting(false);
            aM_.player.connection.send(new ServerboundPlayerCommandPacket(aM_.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
            this.l = 1;
            return false;
        }
        this.l = 1;
        return ((LocalPlayerAccessor) (Object) aM_.player).getWasSprinting();
    }

    public boolean q() {
        if (this.h.a("Используется предмет") != null && this.h.a("Используется предмет").c().booleanValue() && aM_.player.isUsingItem() && aM_.player.getUseItemRemainingTicks() > 0 && this.d >= 8) {
            this.d = 8;
            return false;
        }
        if ((this.h.a("Открыт контейнер") != null && this.h.a("Открыт контейнер").c().booleanValue() && aM_.gui.screen() != null && !(aM_.gui.screen() instanceof GUIScreen)) || !AuraUtil.a(this.o, 3.0d)) {
            return false;
        }
        if (Delta.h().d().t().H().t()) {
            if (aM_.player.getCooldowns().isOnCooldown(aM_.player.getMainHandItem())) {
                return false;
            }
        } else if (aM_.player.fallDistance > 1.5f) {
            if (aM_.player.getCooldowns().isOnCooldown(aM_.player.getMainHandItem()) || this.d <= 3) {
                return false;
            }
        } else if (MaceUtil.a()) {
            if (aM_.player.getCooldowns().isOnCooldown(aM_.player.getMainHandItem()) || aM_.player.getAttackStrengthScale(0.5f) < 0.9f) {
                return false;
            }
        } else if (aM_.player.getAttackStrengthScale(0.5f) < 0.9f || this.d < 10) {
            return false;
        }
        return AuraUtil.c() || (this.g.a("Адаптивные удары").c().booleanValue() && aM_.player.onGround() && !aM_.player.input.keyPresses.jump()) || !AuraUtil.b();
    }

    private LivingEntity d(boolean aimed) {
        if (!aimed) {
            return (this.h.a("Враг за стеной").c().booleanValue() ? e(false).or(() -> {
                return e(true);
            }) : e(true)).orElse(null);
        }
        float yaw = aM_.player.getYRot();
        float pitch = aM_.player.getXRot();
        return x().filter(e -> {
            return AuraUtil.a(yaw, pitch, 3.0d, e, !this.h.a("Враг за стеной").c().booleanValue());
        }).min(Comparator.comparingDouble((v0) -> {
            return AuraUtil.a(v0);
        })).orElse(null);
    }

    private Optional<LivingEntity> e(boolean allowBehindWalls) {
        Comparator<? super LivingEntity> comparatorComparingDouble;
        Vec3 eye = aM_.player.getEyePosition();
        if (MaceUtil.a()) {
            Vec3 landing = MaceUtil.a(aM_.player, (Level) aM_.level).orElse(null);
            Vec3 landingEye = landing != null ? landing.add(0.0d, aM_.player.getEyeHeight(), 0.0d) : null;
            comparatorComparingDouble = Comparator.comparing((LivingEntity e) ->
                Boolean.valueOf(!AuraUtil.a(eye, e, 4.0d) && (landingEye == null || !AuraUtil.a(landingEye, e, 4.0d)))
            ).thenComparing((LivingEntity e2) ->
                Boolean.valueOf(aM_.player.fallDistance > 1.0f && !c(e2))
            ).thenComparingDouble((LivingEntity v0) -> AuraUtil.a(v0));
        } else {
            comparatorComparingDouble = Comparator.comparingDouble(e3 -> {
                return Math.acos(Mth.clamp(Vec3.directionFromRotation(aM_.player.getXRot(), aM_.player.getYRot()).dot(e3.getBoundingBox().getCenter().subtract(eye).normalize()), -1.0d, 1.0d));
            });
        }
        Stream<LivingEntity> stream = x();
        if (!allowBehindWalls) {
            stream = stream.filter(e4 -> {
                return AuraUtil.a(eye, e4, 4.0d);
            });
        }
        return stream.min(comparatorComparingDouble);
    }

    private Stream<LivingEntity> x() {
        return aM_.level.getEntitiesOfClass(LivingEntity.class, aM_.player.getBoundingBox().inflate(128.0d))
                .stream()
                .filter(this::a);
    }

    private boolean d(LivingEntity e) {
        if (e instanceof Player) {
            Player p = (Player) e;
            return this.f.a("Игроки").c().booleanValue() && (this.f.a("Друзья").c().booleanValue() || !Delta.h().d().e().d(p.getName().getString()));
        }
        if (e instanceof Mob) {
            return this.f.a("Мобы").c().booleanValue();
        }
        if (e instanceof Animal) {
            return this.f.a("Животные").c().booleanValue();
        }
        return false;
    }
}


