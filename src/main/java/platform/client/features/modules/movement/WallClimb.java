package platform.client.features.modules.movement;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.MotionEvent;

import platform.client.utils.timer.CounterUtil;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;

@ModuleRegister(a = "Wall Climb", b = "Позволяет взбираться по стенам", c = Category.Movement)
public class WallClimb extends Module {
    private final ModeSetting b = new ModeSetting("Выберите тип обхода", "Матрикс", "Матрикс");
    private final SliderSetting c = new SliderSetting("Скорость режима", 20.0f, 1.0f, 100.0f, 1.0f);
    private final CounterUtil d = new CounterUtil();

    public WallClimb() {
        a(this.b, this.c);
    }

    @Override
    public void b() {
        super.b();
        this.d.b();
    }

    @EventTarget
    public void a(MotionEvent event) {
        if (this.b.l("Матрикс")) {
            a(event, this.c.c().longValue());
        }
    }

    private void a(MotionEvent event, long value) {
        if (this.d.a(value * 5) && aM_.player.horizontalCollision) {
            event.b(true);
            aM_.player.setOnGround(true);
            aM_.player.verticalCollision = true;
            aM_.player.horizontalCollision = true;
            aM_.player.jumpFromGround();
            this.d.b();
        }
    }
}


