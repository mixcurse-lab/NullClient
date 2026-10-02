package platform.client.utils.inject;


import platform.client.utils.render.AnimationUtil;

public interface IItemCooldownManager {
    default AnimationUtil getAnimation() {
        return null;
    }

    default void setHealCooldown(int duration) {
    }
}



