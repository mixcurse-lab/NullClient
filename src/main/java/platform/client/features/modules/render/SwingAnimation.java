package platform.client.features.modules.render;

import platform.api.module.Category;
import platform.client.Delta;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.HandAnimationEvent;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lombok.Generated;
import net.minecraft.world.InteractionHand;

@ModuleRegister(a = "Swing Animation", b = "Настраивает анимацию взмаха руки", c = Category.Render)
public class SwingAnimation extends Module {
    private final BooleanSetting b = new BooleanSetting("Учитывать включённую Aura", true);
    private final ModeSetting c = new ModeSetting("Режим анимации", "Мод 1", "Мод 1", "Мод 2", "Мод 3", "Мод 4", "Мод 5");
    private final SliderSetting d = (SliderSetting) new SliderSetting("Угол поворота", 75.0f, 0.0f, 360.0f, 1.0f).a(() -> {
        return Boolean.valueOf(this.c.l("Мод 1"));
    });
    private final SliderSetting e = (SliderSetting) new SliderSetting("Наклон кончика", -20.0f, -90.0f, 90.0f, 1.0f).a(() -> {
        return Boolean.valueOf(!this.c.l("Мод 5"));
    });
    private final SliderSetting f = new SliderSetting("Интенсивность взмаха", 5.0f, 1.0f, 10.0f, 1.0f);

    @Generated
    public BooleanSetting q() {
        return this.b;
    }

    @Generated
    public ModeSetting r() {
        return this.c;
    }

    @Generated
    public SliderSetting s() {
        return this.d;
    }

    @Generated
    public SliderSetting t() {
        return this.e;
    }

    @Generated
    public SliderSetting u() {
        return this.f;
    }

    public SwingAnimation() {
        a(this.b, this.c, this.d, this.e, this.f);
    }

    @EventTarget
    public void a(HandAnimationEvent event) {
        if ((!this.b.c().booleanValue() || Delta.h().d().t().B().s() != null) && event.c() == InteractionHand.MAIN_HAND) {
            PoseStack matrices = event.b();
            float anim = (float) Math.sin(((double) event.d()) * 3.1415936112270124d);
            float power = this.f.c().floatValue() * 10.0f;
            int arm = event.e();
            matrices.translate(arm * (this.c.l("Мод 5") ? 0.5f : 0.72f), -0.5f, this.c.l("Мод 5") ? -0.72f : -1.0f);
            if (!this.c.l("Мод 5")) {
                matrices.mulPose(Axis.XP.rotationDegrees(-this.e.c().floatValue()));
            }
            switch (this.c.c()) {
                case "Мод 1":
                    matrices.mulPose(Axis.YP.rotationDegrees(arm * 90));
                    matrices.mulPose(Axis.ZP.rotationDegrees(arm * (-70)));
                    matrices.mulPose(Axis.XP.rotationDegrees((-this.d.c().floatValue()) - (power * anim)));
                    break;
                case "Мод 2":
                    matrices.mulPose(Axis.YP.rotationDegrees(arm * 90));
                    matrices.mulPose(Axis.ZP.rotationDegrees(arm * (-65)));
                    matrices.mulPose(Axis.XP.rotationDegrees((-65.0f) + (power * anim)));
                    break;
                case "Мод 3":
                    matrices.mulPose(Axis.YP.rotationDegrees(arm * (-90)));
                    matrices.mulPose(Axis.ZP.rotationDegrees(arm * 60));
                    matrices.mulPose(Axis.XP.rotationDegrees(30.0f));
                    matrices.mulPose(Axis.ZP.rotationDegrees(arm * power * anim));
                    break;
                case "Мод 4":
                    matrices.mulPose(Axis.YP.rotationDegrees(arm * 90));
                    matrices.mulPose(Axis.ZP.rotationDegrees(arm * (-75)));
                    matrices.mulPose(Axis.XP.rotationDegrees((-45.0f) - (power * anim)));
                    matrices.mulPose(Axis.YP.rotationDegrees(arm * power * anim * 0.5f));
                    break;
                case "Мод 5":
                    float strength = power / 80.0f;
                    float swing = anim * anim;
                    float twist = (float) Math.sin(((double) (event.d() * event.d())) * 3.1415936112270124d);
                    matrices.mulPose(Axis.YP.rotationDegrees(arm * (45.0f + (twist * (-20.0f) * strength))));
                    matrices.mulPose(Axis.ZP.rotationDegrees(arm * swing * (-22.0f) * strength));
                    matrices.mulPose(Axis.XP.rotationDegrees(swing * (-85.0f) * strength));
                    matrices.mulPose(Axis.YP.rotationDegrees(arm * (-45.0f)));
                    break;
            }
            event.a(true);
        }
    }
}


