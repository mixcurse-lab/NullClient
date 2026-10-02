package platform.inject.accessors;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({Camera.class})
public interface CameraAccessor {
    @Invoker("setRotation")
    void invokeSetRotation(float f, float f2);

    @Invoker("setPosition")
    void invokeSetPos(double d, double d2, double d3);
}

