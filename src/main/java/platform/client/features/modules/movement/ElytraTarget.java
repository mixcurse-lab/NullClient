package platform.client.features.modules.movement;

import platform.client.features.modules.combat.AuraUtil;
import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;
import platform.client.features.modules.combat.Aura;
import platform.client.utils.rotation.Rotation;

import platform.client.utils.timer.CounterUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.Mth;

@ModuleRegister(a = "Elytra Target", b = "Наводит на врага в полёте на элитре и ускоряется фейерверком из второй руки", c = Category.Movement)
public class ElytraTarget extends Module {
    private final CounterUtil b = new CounterUtil();

    @EventTarget
    public void a(TickEvent event) {
        Aura aura = Delta.h().d().t().B();
        if (aura.m() && aM_.player.isFallFlying()) {
            if (aM_.player.getOffhandItem().getItem() != Items.FIREWORK_ROCKET && !Delta.h().d().t().V().b) {
                if (Delta.h().d().v().a().a().isEmpty()) {
                    Delta.h().d().v().a().a(Items.FIREWORK_ROCKET, 45, 1);
                }
            } else if ((this.b.a(150L) && aM_.player.getDeltaMovement().length() < 1.5d) || aura.attackTicks() == 1) {
                aM_.gameMode.useItem(aM_.player, InteractionHand.OFF_HAND);
                this.b.b();
            }
            LivingEntity target = aura.s();
            if (target == null) {
                return;
            }
            Vec3 eye = aM_.player.getEyePosition();
            Vec3 enemy = target.getBoundingBox().getCenter();
            double dx = enemy.x - eye.x;
            double dz = enemy.z - eye.z;
            double horizontal = Math.sqrt((dx * dx) + (dz * dz));
            double nx = horizontal == 0.0d ? 0.0d : dx / horizontal;
            double nz = horizontal == 0.0d ? 0.0d : dz / horizontal;
            double lift = Math.max(0.0d, 3.0d - q());
            Vec3 aim = new Vec3(enemy.x + (nx * 4.0d), enemy.y + lift, enemy.z + (nz * 4.0d));
            Rotation aimRotation = Rotation.a(eye, aim);
            float Yaw = AuraUtil.a(aM_.player.getYRot(), aimRotation.c(), 1.0f);
            float Pitch = AuraUtil.a(aM_.player.getXRot(), aura.attackTicks() <= 3 ? 0.0f : aimRotation.d(), aura.attackTicks() <= 3 ? 1.0f : Mth.clamp(aura.attackTicks() / 10.0f, 0.0f, 1.0f));
            Delta.h().d().k().a(new Rotation(Yaw, Pitch), 180.0f, 1, 1);
        }
    }

    private double q() {
        Vec3 start = aM_.player.position();
        Vec3 end = start.subtract(0.0d, 2.0d, 0.0d);
        BlockHitResult result = aM_.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, aM_.player));
        if (result.getType() == HitResult.Type.MISS) {
            return 2.0d;
        }
        return start.y - result.getLocation().y;
    }
}


