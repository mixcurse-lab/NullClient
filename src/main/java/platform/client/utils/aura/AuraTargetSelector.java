package platform.client.utils.aura;

import static platform.api.module.Interface.aM_;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.api.module.setting.SliderSetting;
import platform.client.Delta;
import platform.client.features.modules.combat.AuraUtil;
import platform.client.features.modules.combat.MaceUtil;
import platform.client.utils.player.InventoryUtil;


public class AuraTargetSelector {

    private final SliderSetting dist;
    private final SliderSetting extraDist;
    private final ModeSetting priority;
    private final MultiModeSetting targets;

    public AuraTargetSelector(SliderSetting dist, SliderSetting extraDist,
                              ModeSetting priority, MultiModeSetting targets) {
        this.dist = dist;
        this.extraDist = extraDist;
        this.priority = priority;
        this.targets = targets;
    }


    public Optional<LivingEntity> find(boolean allowBehindWalls) {
        Comparator<LivingEntity> comparatorComparingDouble;
        Comparator<LivingEntity> order;
        if (aM_.level == null || aM_.player == null) {
            return Optional.empty();
        }
        Vec3 eye = aM_.player.getEyePosition();
        double reach = this.dist.c().floatValue() + this.extraDist.c().floatValue();
        if (MaceUtil.a()) {
            Vec3 landing = MaceUtil.a(aM_.player, (Level) aM_.level).orElse(null);
            Vec3 landingEye = landing != null ? landing.add(0.0d, aM_.player.getEyeHeight(), 0.0d) : null;
            order = Comparator.comparing((LivingEntity e) ->
                Boolean.valueOf(!AuraUtil.a(eye, e, reach) && (landingEye == null || !AuraUtil.a(landingEye, e, reach)))
            ).thenComparing((LivingEntity e2) ->
                Boolean.valueOf(aM_.player.fallDistance > 1.0f && !hasArmor(e2))
            ).thenComparingDouble((LivingEntity v0) -> AuraUtil.a(v0));
        } else {
            switch (this.priority.c()) {
                case "Дистанция":
                    comparatorComparingDouble = Comparator.comparingDouble((v0) -> AuraUtil.a(v0));
                    break;
                case "ХП":
                    comparatorComparingDouble = Comparator.comparingDouble(LivingEntity::getHealth);
                    break;
                default:
                    comparatorComparingDouble = Comparator.comparingDouble(e3 -> Math.acos(Mth.clamp(Vec3.directionFromRotation(aM_.player.getXRot(), aM_.player.getYRot()).dot(e3.getBoundingBox().getCenter().subtract(eye).normalize()), -1.0d, 1.0d)));
                    break;
            }
            order = comparatorComparingDouble;
        }
        Stream<LivingEntity> stream2 = aM_.level.getEntitiesOfClass(LivingEntity.class, aM_.player.getBoundingBox().inflate(128.0d))
                .stream()
                .filter(e4 -> e4 != aM_.player && e4.isAlive())
                .filter(this::inRange)
                .filter(this::valid);
        if (!allowBehindWalls) {
            stream2 = stream2.filter(e5 -> AuraUtil.a(eye, e5, reach));
        }
        return stream2.min(order);
    }


    public boolean valid(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || !inRange(entity)) {
            return false;
        }
        if (entity instanceof Player) {
            boolean isFriend = Delta.h().d().e().d(entity.getName().getString());
            boolean naked = Stream.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET).noneMatch(slot -> entity.getItemBySlot(slot).get(DataComponents.EQUIPPABLE) != null);
            if (!flag("Игроки")) {
                return false;
            }
            if (isFriend) {
                return flag("Друзья");
            }
            return !naked || flag("Без брони");
        }
        if ((entity instanceof Monster) || (entity instanceof Slime) || (entity instanceof Enemy) || (entity instanceof EnderDragon)) {
            return flag("Враждебные мобы");
        }
        if ((entity instanceof AgeableMob) || (entity instanceof AbstractGolem) || (entity instanceof Allay) || (entity instanceof AmbientCreature)) {
            return flag("Животные");
        }
        return false;
    }


    private boolean inRange(LivingEntity entity) {
        if (Delta.h().d().t().G().m() && aM_.player.isFallFlying()) {
            return true;
        }
        return AuraUtil.a((Entity) entity, ((double) (this.dist.c().floatValue() + this.extraDist.c().floatValue())) + (aM_.player.getDeltaMovement().length() * 3.0d) + ((double) ((InventoryUtil.b(Items.MACE) == -1 || ((double) aM_.player.fallDistance) <= 1.5d) ? 0.0f : 1.5f)) + ((double) ((Delta.h().d().t().H().m() && InventoryUtil.b(Items.MACE) != -1 && ((Boolean) MaceUtil.a(aM_.player, (Level) aM_.level).map(p -> Boolean.valueOf(aM_.player.getY() - p.y > 2.0d)).orElse(false)).booleanValue()) ? 10 : 0)));
    }

    private boolean hasArmor(LivingEntity entity) {
        return Stream.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET).anyMatch(s -> entity.getItemBySlot(s).get(DataComponents.EQUIPPABLE) != null);
    }

    private boolean flag(String name) {
        var setting = this.targets.a(name);
        return setting != null && setting.c().booleanValue();
    }
}
