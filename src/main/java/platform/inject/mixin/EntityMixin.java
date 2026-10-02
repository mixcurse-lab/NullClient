package platform.inject.mixin;

import platform.api.event.events.other.BoundingBoxEvent;
import platform.client.Delta;
import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.module.Interface;
import platform.api.module.Module;
import platform.api.event.events.render.RemovalsEvent;
import platform.client.features.modules.movement.NoPush;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public abstract class EntityMixin {
    @Shadow
    private AABB bb;

    @Inject(method = {"getBoundingBox"}, at = {@At("HEAD")}, cancellable = true)
    public final void getBoundingBox(CallbackInfoReturnable<AABB> cir) {
        if (!((Object) this instanceof net.minecraft.world.entity.LivingEntity)) {
            return;
        }
        BoundingBoxEvent event = new BoundingBoxEvent(this.bb, (Entity)(Object) this);
        EventManager.a((IEvent) event);
        cir.setReturnValue(event.b());
    }

    @Inject(method = {"onAboveBubbleColumn"}, at = {@At("HEAD")}, cancellable = true)
    private void onAboveBubbleColumn(boolean drag, BlockPos pos, CallbackInfo ci) {
        Entity self = (Entity)(Object) this;
        if ((self instanceof LocalPlayer) && Delta.h().d().t().aq().m()) {
            self.setDeltaMovement(self.getDeltaMovement().x, Math.min(1.8d, self.getDeltaMovement().y + 9.99999999E8d), self.getDeltaMovement().z);
            ci.cancel();
        }
    }

    @ModifyReturnValue(method = {"hasGlowingTag"}, at = {@At("RETURN")})
    private boolean delta$removalsGlow(boolean original) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.GLOW);
        EventManager.a((IEvent) event);
        if (event.a()) {
            return false;
        }
        return original;
    }

    @WrapOperation(method = {"collideBoundingBox(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collectCollidersIgnoringWorldBorder(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;")})
    private static List<VoxelShape> delta$filterInsideBlockColliders(Entity source, Level level, List<VoxelShape> entityColliders, AABB boundingBox, Operation<List<VoxelShape>> original) {
        List<VoxelShape> colliders = original.call(source, level, entityColliders, boundingBox);
        if (source instanceof LocalPlayer) {
            NoPush noPush = (NoPush) Delta.h().d().t().O();
            if (noPush != null && noPush.m() && noPush.q().a("Блоков").c().booleanValue()) {
                AABB box = source.getBoundingBox();
                List<Vec3> points = new ArrayList<>();
                points.add(new Vec3(box.minX, box.minY, box.minZ));
                points.add(new Vec3(box.minX, box.minY, box.maxZ));
                points.add(new Vec3(box.maxX, box.minY, box.minZ));
                points.add(new Vec3(box.maxX, box.minY, box.maxZ));
                points.add(new Vec3(box.minX, box.maxY, box.minZ));
                points.add(new Vec3(box.minX, box.maxY, box.maxZ));
                points.add(new Vec3(box.maxX, box.maxY, box.minZ));
                points.add(new Vec3(box.maxX, box.maxY, box.maxZ));
                Vec3 feet = source.position();
                points.add(feet);
                points.add(feet.add(0.0D, source.getBbHeight() / 2.0D, 0.0D));
                points.add(source.getEyePosition());
                List<VoxelShape> filtered = new ArrayList<>(colliders);
                filtered.removeIf(shape -> {
                    AABB bounds = shape.bounds();
                    for (Vec3 point : points) {
                        if (point.x > bounds.minX && point.x < bounds.maxX
                            && point.y > bounds.minY && point.y < bounds.maxY
                            && point.z > bounds.minZ && point.z < bounds.maxZ) {
                            return true;
                        }
                    }
                    return false;
                });
                return filtered;
            }
        }
        return colliders;
    }
}

