package platform.inject.mixin;

import platform.client.utils.inject.ISlot;
import platform.client.utils.render.AnimationUtil;
import lombok.Generated;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Slot.class})
public abstract class SlotMixin implements ISlot {

    @Unique
    private final AnimationUtil animation = new AnimationUtil();

    @Override
    @Generated
    public AnimationUtil getAnimation() {
        return this.animation;
    }

    @Inject(method = {"<init>*"}, at = {@At("TAIL")})
    private void init(CallbackInfo ci) {
        this.animation.c(1.0f);
    }
}
