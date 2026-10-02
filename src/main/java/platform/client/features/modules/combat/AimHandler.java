package platform.client.features.modules.combat;

import platform.api.handlers.Handler_2;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.ProjectUtil;

import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.handlers.BaseHandler;
import platform.client.features.modules.combat.ProjectileHelper;

import platform.client.utils.render.AnimationUtil;
import lombok.Generated;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

@Handler_2
public class AimHandler extends BaseHandler implements Interface {
    private final AnimationUtil b = new AnimationUtil();
    private LivingEntity c;

    @Generated
    public AnimationUtil a() {
        return this.b;
    }

    @EventTarget
    public void a(DrawEvent event) {
        this.b.a(0.0f, 1.0f, 0.25f, EasingList.g, event.g());
        float alpha = this.b.c();
        if (event.b() && this.c != null && alpha > 0.0f) {
            Vec3 real = a(this.c, event.g());
            Vector2f screen = ProjectUtil.a(real.x, real.y, real.z);
            if (!ProjectUtil.a(screen)) {
                return;
            }
            float distance = (float) aM_.player.getEyePosition().distanceTo(real);
            float size = ((float) Math.max(28.0d, 40.0d - (((double) distance) * 0.7000002488091963d))) * (1.2f - (0.2f * alpha));
            event.i().pose().pushMatrix();
            event.i().pose().translate(screen.x(), screen.y());
            event.i().pose().rotate((float) Math.toRadians(((float) Math.sin(System.currentTimeMillis() / 820.0d)) * 350.0f));
            event.d().a(event.i(), Identifier.fromNamespaceAndPath("delta", "pictures/marker.png"), (-size) / 2.0f, (-size) / 2.0f, size, size, 0.0f, ColorUtil.a(-1, alpha * 0.8f));
            event.i().pose().popMatrix();
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        ProjectileHelper projectile = Delta.h().d().t().D();
        LivingEntity current = null;
        if (projectile.m() && projectile.r()) {
            current = projectile.q();
        }
        boolean visible = current != null;
        if (visible) {
            this.c = current;
        }
        this.b.a(visible);
        if (!visible && this.b.a() <= 0.0f) {
            this.c = null;
        }
    }

    private Vec3 a(LivingEntity entity, float delta) {
        return new Vec3(Mth.lerp(delta, entity.xOld, entity.getX()), Mth.lerp(delta, entity.yOld, entity.getY()) + (((double) entity.getBbHeight()) / 2.0d), Mth.lerp(delta, entity.zOld, entity.getZ()));
    }
}


