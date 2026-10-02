package platform.client.features.modules.movement;

import static platform.api.module.Interface.aM_;

import java.util.Locale;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import platform.api.event.events.client.TickEvent;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;
import platform.client.utils.player.MoveUtil;

@ModuleRegister(a = "Speed", b = "Увеличивает скорость передвижения", c = Category.Movement)
public class Speed extends Module {
    private final ModeSetting b = new ModeSetting("Режим", "Ваниль", "Ваниль", "Грим", "ФанТайм", "ХолиВорлд");
    private final SliderSetting c = ((SliderSetting) new SliderSetting("Скорость", 1.5f, 1.0f, 5.0f, 0.1f).a(() -> {
        return this.b.l("Ваниль");
    }));
    private final SliderSetting d = ((SliderSetting) new SliderSetting("Множитель Грим", 1.0f, 0.1f, 4.0f, 0.1f).a(() -> {
        return this.b.l("Грим");
    }));
    private final SliderSetting e = ((SliderSetting) new SliderSetting("Множитель ХолиВорлд", 1.0f, 0.1f, 4.0f, 0.1f).a(() -> {
        return this.b.l("ХолиВорлд");
    }));

    public Speed() {
        a(this.b, this.c, this.d, this.e);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (aM_.player == null || aM_.level == null || !MoveUtil.a()) {
            return;
        }
        if (this.b.l("Ваниль")) {
            this.a(this.c.c().floatValue() / 3.0f);
            return;
        }
        if (aM_.player.isSwimming() || aM_.player.isFallFlying() || aM_.player.isShiftKeyDown()) {
            return;
        }
        if (this.b.l("ФанТайм")) {
            if (aM_.player.getBoundingBox().getYsize() < 1.5f) {
                this.a(aM_.player.hasEffect(MobEffects.SPEED) ? 0.32f : 0.28f);
            }
        } else if (this.b.l("Грим")) {
            this.a(0.5f, 0.07d * this.d.c().floatValue());
        } else if (this.b.l("ХолиВорлд")) {
            this.a(0.35f, 0.0205d * this.e.c().floatValue());
        }
    }


    private void a(float speed) {
        float forward = aM_.player.input.getMoveVector().y;
        float sideways = aM_.player.input.getMoveVector().x;
        double len = Math.hypot(forward, sideways);
        if (len == 0.0d) {
            aM_.player.setDeltaMovement(0.0d, aM_.player.getDeltaMovement().y, 0.0d);
            return;
        }
        forward = (float) (forward / len);
        sideways = (float) (sideways / len);
        double rad = Math.toRadians(aM_.player.getYRot());
        double x = (((double) (-forward)) * Math.sin(rad)) + (((double) sideways) * Math.cos(rad));
        double z = (((double) forward) * Math.cos(rad)) + (((double) sideways) * Math.sin(rad));
        aM_.player.setDeltaMovement(x * speed, aM_.player.getDeltaMovement().y, z * speed);
    }


    private void a(float inflate, double power) {
        AABB box = aM_.player.getBoundingBox().inflate(inflate);
        int collisions = 0;
        for (Entity entity : aM_.level.entitiesForRendering()) {
            if (entity == null || entity == aM_.player || (entity instanceof ArmorStand)) {
                continue;
            }
            boolean valid = (entity instanceof LivingEntity) || entity.getType().toString().toLowerCase(Locale.ROOT).contains("boat");
            if (valid && box.intersects(entity.getBoundingBox())) {
                collisions++;
            }
        }
        if (collisions <= 0) {
            return;
        }
        double rad = Math.toRadians(aM_.player.getYRot());
        aM_.player.push((-Math.sin(rad)) * power * collisions, 0.0d, Math.cos(rad) * power * collisions);
    }
}
