package platform.client.utils.aura.rotation;

import static platform.api.module.Interface.aM_;

import net.minecraft.util.Mth;
import platform.client.Delta;
import platform.client.features.modules.combat.AuraUtil;
import platform.client.utils.aura.AuraContext;
import platform.client.utils.math.MathUtil;
import platform.client.utils.rotation.Rotation;


public class LegitRotation extends AuraRotation {

    public LegitRotation(AuraContext ctx) {
        super(ctx);
    }

    @Override
    public String name() {
        return "Легит";
    }

    @Override
    public void rotate() {
        float yawToTarget = ctx.yawToTarget;
        float pitchToTarget = ctx.pitchToTarget;
        float t = aM_.player.tickCount + aM_.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float fSin = ((float) (((Math.sin(t * 0.31f) * 0.5d) + (Math.sin((t * 1.7f) + 2.6f) * 0.2000000098386085d)) * 8.0d)) / 4.0f;
        float smoothW = fSin;
        float smoothH = fSin;
        float finalYaw = AuraUtil.a(aM_.player.getYRot(), yawToTarget, MathUtil.a(0.2f, 0.35f));
        float finalPitch = AuraUtil.a(aM_.player.getXRot(), pitchToTarget, MathUtil.a(0.15f, 0.25f));
        if (ctx.timers[3] >= 0.0f) {
            finalPitch = AuraUtil.a(aM_.player.getXRot(), pitchToTarget, 0.35f);
            smoothH /= 3.0f;
            smoothW /= 3.0f;
            if (!AuraUtil.a(aM_.player.getYRot(), aM_.player.getXRot(), ctx.reach, ctx.target, true)) {
                finalYaw = AuraUtil.a(aM_.player.getYRot(), yawToTarget, MathUtil.a(0.7f, 1.0f));
            }
        }
        if (!AuraUtil.a(finalYaw + smoothW, finalPitch + smoothH, ctx.reach, ctx.target, true) && AuraUtil.a(yawToTarget, pitchToTarget, ctx.reach, ctx.target, true)) {
            smoothW = Mth.clamp(smoothW, -0.15f, 0.15f);
            smoothH = Mth.clamp(smoothH, -0.15f, 0.15f);
        }
        if (ctx.timers[5] >= 0.0f) {
            smoothW *= 8.0f;
            if (ctx.ticks >= 1 && ctx.timers[2] % 5.0f == 0.0f) {
                finalPitch = AuraUtil.a(aM_.player.getXRot(), -pitchToTarget, 0.05f);
            }
        }
        Delta.h().d().k().a(new Rotation(finalYaw + smoothW, finalPitch + smoothH), 180.0f, 1, 1);
    }
}
