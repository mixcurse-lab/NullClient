package platform.client.utils.aura.rotation;

import static platform.api.module.Interface.aM_;

import net.minecraft.util.Mth;
import platform.client.Delta;
import platform.client.utils.aura.AuraContext;
import platform.client.utils.rotation.Rotation;


public class AtlantisRotation extends AuraRotation {

    public AtlantisRotation(AuraContext ctx) {
        super(ctx);
    }

    @Override
    public String name() {
        return "Атлантис";
    }

    @Override
    public void rotate() {
        if (aM_.player == null) {
            return;
        }
        float finalYaw = Mth.wrapDegrees(ctx.yawToTarget);
        float finalPitch = Mth.clamp(ctx.pitchToTarget, -90.0f, 90.0f);
        Delta.h().d().k().a(new Rotation(finalYaw, finalPitch), 220.0f, 1, 1);
    }
}
