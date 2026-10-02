package platform.inject.mixin;


import platform.client.utils.render.AnimationUtil;
import platform.client.utils.inject.IStatusEffectInstance;
import lombok.Generated;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.Holder;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({MobEffectInstance.class})
public abstract class MobEffectInstanceMixin implements IStatusEffectInstance {

    @Shadow
    public abstract int getDuration();

    @Shadow
    public abstract int getAmplifier();

    @Shadow
    public abstract Holder<MobEffect> getEffect();

    @Unique
    private final AnimationUtil animation = new AnimationUtil();

    @Unique
    private int initialDuration;

    @Override
    @Generated
    public AnimationUtil getAnimation() {
        return this.animation;
    }

    @Override
    @Generated
    public int getInitialDuration() {
        return this.initialDuration;
    }

    @Override
    @Generated
    public void setInitialDuration(int initialDuration) {
        this.initialDuration = initialDuration;
    }

    @Override
    @Generated
    public Holder<MobEffect> getEffectType() {
        return getEffect();
    }

    @Inject(method = {"<init>*"}, at = {@At("TAIL")})
    private void onInit(CallbackInfo ci) {
        this.initialDuration = ((MobEffectInstance)(Object) this).getDuration();
    }
}

