package platform.inject.accessors;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({LocalPlayer.class})
public interface LocalPlayerAccessor {
    @Accessor("yRotLast")
    float getLastYaw();

    @Accessor("xRotLast")
    float getLastPitch();

    @Accessor("wasSprinting")
    boolean getWasSprinting();

    @Accessor("wasSprinting")
    void setWasSprinting(boolean wasSprinting);
}

