package platform.client.utils.aura;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;


public class AuraContext {

    public LivingEntity target;


    public float yawToTarget;
    public float pitchToTarget;


    public Vec3 targetPos = Vec3.ZERO;


    public float reach;


    public String mode = "";


    public final float[] timers = {-1.0f, -1.0f, -1.0f, -1.0f, 0.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f};


    public final float[] pitchHistory = new float[30];


    public int ticks;
}
