package platform.client.utils.aura.rotation;

import static platform.api.module.Interface.aM_;

import net.minecraft.util.Mth;
import platform.client.Delta;
import platform.client.features.modules.combat.AuraUtil;
import platform.client.utils.aura.AuraContext;
import platform.client.utils.math.MathUtil;
import platform.client.utils.rotation.Look;
import platform.client.utils.rotation.Rotation;


public abstract class FantimeStyleRotation extends AuraRotation {

    protected FantimeStyleRotation(AuraContext ctx) {
        super(ctx);
    }


    protected abstract boolean useMousePitch();

    @Override
    public final void rotate() {
        float yawToTarget = ctx.yawToTarget;
        float pitchToTarget = ctx.pitchToTarget;
        float t = aM_.player.tickCount + aM_.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float smoothW = (float) ((Math.sin(((double) t) * 0.4000000008323731d) * 3.0d) + (Math.sin((((double) t) * 0.9500002390239708d) + 1.4000004888461306d) * 2.0d));
        float smoothH = (float) ((Math.cos((((double) t) * 0.5d) + 0.7000001555309916d) * 0.5d) + (Math.cos((((double) t) * 0.7800000620494261d) + 3.10000031689524d) * 1.5d));
        float finalPitch = AuraUtil.a(aM_.player.getXRot(), ctx.pitchHistory[Mth.clamp(10 - ctx.ticks, 0, 29)] + (smoothH * 1.5f), MathUtil.a(0.1f, 0.5f));
        float finalYaw = AuraUtil.a(aM_.player.getYRot(), yawToTarget + smoothW, MathUtil.a(0.1f, 0.4f));
        if (ctx.timers[3] >= 0.0f) {
            if (!AuraUtil.a(aM_.player.getYRot(), aM_.player.getXRot(), ctx.reach, ctx.target, true) && ctx.timers[8] <= 0.0f) {
                finalYaw = yawToTarget;
            }
            if (!AuraUtil.a(yawToTarget, finalPitch, ctx.reach, ctx.target, true) && ctx.timers[8] <= 0.0f) {
                finalPitch = pitchToTarget;
            }
            if (!AuraUtil.a(aM_.player.getYRot() + smoothW, aM_.player.getYRot() + smoothH, ctx.reach, ctx.target, true) && AuraUtil.a(aM_.player.getYRot(), aM_.player.getXRot(), ctx.reach, ctx.target, true)) {
                smoothW = Mth.clamp(smoothW, -0.05f, 0.05f);
                smoothH = Mth.clamp(smoothH, -0.05f, 0.05f);
            }
        }
        if (ctx.ticks <= 4 && ctx.timers[2] % 2.0f == 0.0f) {
            finalYaw = aM_.player.getYRot();
        }
        Delta.h().d().k().a(new Rotation(finalYaw + smoothW, (useMousePitch() ? Look.c() : finalPitch) + smoothH), 220.0f, 1, 1);
    }
}
