package platform.client.features.modules.movement;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.SliderSetting;

@ModuleRegister(a = "Fly", b = "Позволяет свободно летать по миру", c = Category.Movement)
public class Fly extends Module {
    private final SliderSetting b = new SliderSetting("Скорость X и Z", 1.0f, 0.1f, 5.0f, 0.1f);
    private final SliderSetting c = new SliderSetting("Скорость Y", 1.0f, 0.1f, 5.0f, 0.1f);

    public Fly() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(TickEvent event) {
        q();
    }

    private void q() {
        double dFloatValue;
        float fSignum;
        float yaw = aM_.player.getYRot();
        float forward = aM_.player.input.getMoveVector().y;
        float sideways = aM_.player.input.getMoveVector().x;
        if (aM_.options.keyShift.isDown()) {
            dFloatValue = -this.c.c().floatValue();
        } else {
            dFloatValue = aM_.options.keyJump.isDown() ? this.c.c().floatValue() : 0.0d;
        }
        double motionY = dFloatValue;
        if (forward == 0.0f && sideways == 0.0f) {
            aM_.player.setDeltaMovement(0.0d, motionY, 0.0d);
            return;
        }
        if (forward != 0.0f) {
            if (sideways != 0.0f) {
                fSignum = (forward > 0.0f ? -45.0f : 45.0f) * Math.signum(sideways);
            } else {
                fSignum = 0.0f;
            }
            yaw += fSignum;
            sideways = 0.0f;
            forward = Math.signum(forward);
        }
        double radians = Math.toRadians(yaw + 90.0f);
        double speedXZ = this.b.c().floatValue();
        double motionX = (((double) forward) * speedXZ * Math.cos(radians)) + (((double) sideways) * speedXZ * Math.sin(radians));
        double motionZ = ((((double) forward) * speedXZ) * Math.sin(radians)) - ((((double) sideways) * speedXZ) * Math.cos(radians));
        aM_.player.setDeltaMovement(motionX, motionY, motionZ);
    }
}


