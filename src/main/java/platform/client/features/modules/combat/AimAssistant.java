package platform.client.features.modules.combat;

import platform.client.features.modules.combat.AuraUtil;
import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.LookEvent;
import platform.api.event.events.client.TickEvent;
import platform.client.features.modules.combat.TriggerBot;

import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.api.module.setting.SliderSetting;
import java.util.Comparator;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

@ModuleRegister(a = "Aim Assistant", b = "Доводит прицел до цели", c = Category.Combat)
public class AimAssistant extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Цели для наведения", new BooleanSetting("Игроки", true), new BooleanSetting("Животные", false), new BooleanSetting("Мобы", false), new BooleanSetting("Друзья", true));
    private final BooleanSetting c = new BooleanSetting("Наводить за стеной", false);
    private final SliderSetting d = new SliderSetting("Порог", 5.0f, 1.0f, 5.0f, 0.25f);
    private final BooleanSetting e = new BooleanSetting("Только с оружием", true);
    private LivingEntity f;
    private Vec3 g;

    @Generated
    public LivingEntity q() {
        return this.f;
    }

    public AimAssistant() {
        a(this.b, this.c, this.d, this.e);
    }

    @Override
    public void c() {
        super.c();
        this.f = null;
    }

    @EventTarget
    public void a(TickEvent event) {
        TriggerBot trigger = Delta.h().d().t().X();
        LivingEntity found = trigger.m() ? trigger.s() : r();
        if (found != this.f) {
            this.g = null;
        }
        this.f = found;
    }

    @EventTarget
    public void a(LookEvent event) {
        if (!a(this.f) || aM_.player.isUsingItem()) {
            return;
        }
        if (!this.e.c().booleanValue() || s()) {
            Vec3 position = AuraUtil.a(aM_.player.getEyePosition(), this.f, 3.0d, this.c.c().booleanValue());
            if (position == Vec3.ZERO) {
                return;
            }
            this.g = this.g == null ? position : this.g.lerp(position, 0.2000000448441151d);
            float yaw = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(this.g.z, this.g.x)) - 90.0d);
            float pitch = (float) (-Math.toDegrees(Math.atan2(this.g.y, Math.hypot(this.g.x, this.g.z))));
            float deltaYaw = Mth.wrapDegrees(yaw - aM_.player.getYRot());
            float deltaPitch = pitch - aM_.player.getXRot();
            if (Math.abs(deltaPitch) <= 13.0f && Math.abs(deltaYaw) < 8.0f && AuraUtil.a(aM_.player.getYRot(), aM_.player.getXRot(), 3.0d, this.f, this.c.c().booleanValue())) {
                deltaPitch = 0.0f;
            }
            float frame = aM_.getDeltaTracker().getRealtimeDeltaTicks();
            float ease = Mth.clamp(((float) Math.hypot(deltaYaw, deltaPitch)) / 4.0f, 0.0f, 1.0f);
            float speed = this.d.c().floatValue() * frame * ease;
            if (speed <= 0.0f) {
                return;
            }
            float step = Math.min(1.0f, speed / Math.max(Math.abs(deltaYaw), Math.abs(deltaPitch) * 2.0f));
            aM_.player.setYRot(aM_.player.getYRot() + (deltaYaw * step));
            if (deltaPitch != 0.0f) {
                aM_.player.setXRot(Mth.clamp(aM_.player.getXRot() + (deltaPitch * step), -90.0f, 90.0f));
            }
        }
    }

    private LivingEntity r() {
        Vec3 eye = aM_.player.getEyePosition();
        Vec3 look = Vec3.directionFromRotation(aM_.player.getXRot(), aM_.player.getYRot());
        return aM_.level.getEntitiesOfClass(LivingEntity.class, aM_.player.getBoundingBox().inflate(128.0d))
                .stream()
                .filter(entity -> a(entity) && (this.c.c().booleanValue() || AuraUtil.a(eye, entity, 4.0d)))
                .min(Comparator.comparingDouble(entity2 ->
                        Math.acos(Mth.clamp(look.dot(entity2.getBoundingBox().getCenter().subtract(eye).normalize()), -1.0d, 1.0d))))
                .orElse(null);
    }

    private boolean s() {
        Item item = aM_.player.getMainHandItem().getItem();
        return (aM_.player.getMainHandItem().get(DataComponents.WEAPON) != null) || (item instanceof AxeItem) || (item instanceof MaceItem);
    }

    private boolean a(LivingEntity entity) {
        return entity != null && entity.isAlive() && !entity.isRemoved() && entity != aM_.player && AuraUtil.a((Entity) entity, 4.0d + (aM_.player.getDeltaMovement().length() * 3.0d)) && b(entity);
    }

    private boolean b(LivingEntity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            return this.b.a("Игроки").c().booleanValue() && (this.b.a("Друзья").c().booleanValue() || !Delta.h().d().e().d(player.getName().getString()));
        }
        if (entity instanceof Mob) {
            return this.b.a("Мобы").c().booleanValue();
        }
        return (entity instanceof Animal) && this.b.a("Животные").c().booleanValue();
    }
}


