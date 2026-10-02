package platform.client.utils.inject;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({Entity.class})
public interface EntityCollisionPredictionInvoker {
    @Invoker("collideBoundingBox")
    static Vec3 findCollisionsForMovement(Entity entity, Vec3 movement, AABB box, Level world) {
        return Vec3.ZERO;
    }

    @Invoker("collideBoundingBox")
    static Vec3 adjustMovementForCollisions(Entity entity, Vec3 movement, AABB boundingBox, Level world, List<VoxelShape> collisions) {
        return Vec3.ZERO;
    }
}

