package platform.client.utils.inject;


import platform.client.utils.render.AnimationUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.Holder;

public interface IStatusEffectInstance {
    AnimationUtil getAnimation();

    int getInitialDuration();

    void setInitialDuration(int i);

    int getDuration();

    int getAmplifier();

    Holder<MobEffect> getEffectType();
}



