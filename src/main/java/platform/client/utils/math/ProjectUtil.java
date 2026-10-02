package platform.client.utils.math;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;

import platform.api.module.Interface;

import lombok.Generated;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ProjectUtil implements Interface {
    @Generated
    private ProjectUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Vector2f a(double x, double y, double z) {
        Quaternionf yawQuat = new Quaternionf().rotateY((float) Math.toRadians(-aM_.getEntityRenderDispatcher().camera.yRot()));
        Quaternionf pitchQuat = new Quaternionf().rotateX((float) Math.toRadians(aM_.getEntityRenderDispatcher().camera.xRot()));
        Quaternionf cameraRotation = yawQuat.mul(pitchQuat, new Quaternionf());
        Quaternionf cameraRotation2 = cameraRotation.conjugate(new Quaternionf());
        Vector3f result3f = new Vector3f((float) (aM_.getEntityRenderDispatcher().camera.position().x - x), (float) (aM_.getEntityRenderDispatcher().camera.position().y - y), (float) (aM_.getEntityRenderDispatcher().camera.position().z - z));
        result3f.rotate(cameraRotation2);
        return a(result3f, aM_.getEntityRenderDispatcher().camera.getFov());
    }

    private static Vector2f a(Vector3f result3f, double fov) {
        float realAspect = (float) aM_.getWindow().getScreenWidth() / (float) aM_.getWindow().getScreenHeight();
        float modifiedAspect = realAspect;
        double scaleFactorY = ((double) (aM_.getWindow().getGuiScaledHeight() / 2.0f)) / (((double) result3f.z) * Math.tan(Math.toRadians(fov / 2.0d)));
        double scaleFactorX = (scaleFactorY * ((double) realAspect)) / ((double) modifiedAspect);
        return result3f.z < 0.0f ? new Vector2f((float) ((((double) (-result3f.x())) * scaleFactorX) + ((double) (aM_.getWindow().getGuiScaledWidth() / 2.0f))), (float) (((double) (aM_.getWindow().getGuiScaledHeight() / 2.0f)) - (((double) result3f.y()) * scaleFactorY))) : new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public static float[] a( AABB  box) {
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -3.4028235E38f;
        float maxY = -3.4028235E38f;
        for (int corner = 0; corner < 8; corner++) {
            double x = (corner & 1) == 0 ? box.minX : box.maxX;
            double y = (corner & 2) == 0 ? box.minY : box.maxY;
            double z = (corner & 4) == 0 ? box.minZ : box.maxZ;
            Vector2f screen = a(x, y, z);
            if (screen.x() != Float.MAX_VALUE) {
                minX = Math.min(minX, screen.x());
                minY = Math.min(minY, screen.y());
                maxX = Math.max(maxX, screen.x());
                maxY = Math.max(maxY, screen.y());
            }
        }
        if (maxX <= minX || maxY <= minY) {
            return null;
        }
        return new float[]{minX, minY, maxX, maxY};
    }

    public static boolean a(Vector2f screen) {
        return screen.x() != Float.MAX_VALUE && screen.y() != Float.MAX_VALUE && screen.x() >= 0.0f && screen.y() >= 0.0f && screen.x() <= ((float) aM_.getWindow().getGuiScaledWidth()) && screen.y() <= ((float) aM_.getWindow().getGuiScaledHeight());
    }
}



