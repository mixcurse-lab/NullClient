package platform.inject.mixin;


import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.event.events.player.PushEvent;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({WorldBorder.class})
public class WorldBorderMixin {
    @Inject(method = {"getCollisionShape"}, at = {@At("HEAD")}, cancellable = true)
    private void getCollisionShape(CallbackInfoReturnable<VoxelShape> cir) {
        PushEvent event = new PushEvent(PushEvent.a.WORLD_BORDER);
        EventManager.a((IEvent) event);
        if (event.a()) {
            cir.setReturnValue(Shapes.empty());
        }
    }
}
