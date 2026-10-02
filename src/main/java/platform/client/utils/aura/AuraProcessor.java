package platform.client.utils.aura;

import static platform.api.module.Interface.aM_;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.player.WillLandEvent;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.api.module.setting.SliderSetting;
import platform.client.Delta;
import platform.client.features.modules.combat.AuraUtil;
import platform.client.features.modules.combat.MaceUtil;
import platform.client.ui.screen.GUIScreen;
import platform.client.utils.math.MathUtil;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.player.MoveUtil;
import platform.client.utils.player.ServerUtil;
import platform.client.utils.rotation.Look;
import platform.client.utils.rotation.Rotation;
import platform.inject.invokers.MinecraftInvoker;
import platform.client.utils.aura.rotation.AuraRotation;
import platform.client.utils.aura.rotation.AtlantisRotation;
import platform.client.utils.aura.rotation.FantimeFovRotation;
import platform.client.utils.aura.rotation.FantimeRotation;
import platform.client.utils.aura.rotation.LegitCxRotation;
import platform.client.utils.aura.rotation.LegitRotation;
import platform.client.utils.aura.rotation.MXRotation;
import platform.client.utils.aura.rotation.NeuroRotation;
import platform.client.utils.aura.rotation.SpookyTimeRotation;


public class AuraProcessor {

    final AuraContext ctx = new AuraContext();
    final int[] slots = {-1, -1};
    boolean landedCrit;
    boolean altHit;
    float lpYaw, lpPitch;
    LivingEntity target;

    final ModeSetting type;
    final MultiModeSetting targetsSetting;
    final SliderSetting reachSetting;
    final SliderSetting extraReach;
    final BooleanSetting critOnly;
    final BooleanSetting adaptive;
    final MultiModeSetting skipWhen;
    final BooleanSetting shieldBreak;
    final BooleanSetting smartSprint;
    @SuppressWarnings("unused")
    private final ModeSetting priorityMode;
    final ModeSetting moveCorrection;

    private final AuraTargetSelector selector;
    private final AuraAttacker attacker;
    private final Map<String, AuraRotationHolder> rotations = new LinkedHashMap<>();


    private static final class AuraRotationHolder {
        final String name;
        final Runnable step;

        AuraRotationHolder(String name, Runnable step) {
            this.name = name;
            this.step = step;
        }
    }

    public AuraProcessor(ModeSetting type, MultiModeSetting targets, SliderSetting reach, SliderSetting extraReach,
                         BooleanSetting critOnly, BooleanSetting adaptive, MultiModeSetting skipWhen,
                         BooleanSetting shieldBreak, BooleanSetting smartSprint, ModeSetting priority,
                         ModeSetting moveCorrection) {
        this.type = type;
        this.targetsSetting = targets;
        this.reachSetting = reach;
        this.extraReach = extraReach;
        this.critOnly = critOnly;
        this.adaptive = adaptive;
        this.skipWhen = skipWhen;
        this.shieldBreak = shieldBreak;
        this.smartSprint = smartSprint;
        this.priorityMode = priority;
        this.moveCorrection = moveCorrection;
        this.selector = new AuraTargetSelector(reach, extraReach, priority, targets);
        this.attacker = new AuraAttacker(this);
        registerRotation(new FantimeRotation(this.ctx));
        registerRotation(new FantimeFovRotation(this.ctx));
        registerRotation(new LegitRotation(this.ctx));
        registerRotation(new LegitCxRotation(this.ctx));
        registerRotation(new NeuroRotation(this.ctx));
        registerRotation(new AtlantisRotation(this.ctx));
        registerRotation(new SpookyTimeRotation(this.ctx));
        registerRotation(new MXRotation(this.ctx));
    }

    private void registerRotation(AuraRotation rotation) {
        this.rotations.put(rotation.name(), new AuraRotationHolder(rotation.name(), rotation::rotate));
    }


    public void begin() {
        if (this.ctx.timers[9] == -1.0f) {
            this.ctx.timers[9] = (int) MathUtil.a(9.0f, 13.0f);
        }
        this.ctx.timers[8] = 2.0f;
        this.ctx.timers[10] = 0.0f;
        this.ctx.timers[11] = 0.0f;
        Arrays.fill(this.ctx.pitchHistory, aM_.player != null ? aM_.player.getXRot() : 0.0f);
        this.target = null;
    }


    public void suspend() {
        this.ctx.timers[10] = 0.0f;
        this.target = null;
    }

    public void handleInput(InputEvent event) {
        if (this.target != null) {
            float moveYaw;
            if (this.moveCorrection.l("Фокус")) {
                moveYaw = this.ctx.timers[1];
            } else if (this.moveCorrection.l("Таргет")) {
                // движение строго в центр хитбокса цели: забегаем внутрь тела
                // и держимся там, пока аура бьёт
                Vec3 center = this.target.getBoundingBox().getCenter();
                Vec3 rel = new Vec3(center.x - aM_.player.getX(), 0.0d, center.z - aM_.player.getZ());
                double distSq = rel.x * rel.x + rel.z * rel.z;
                moveYaw = distSq < 1.0e-4d ? this.ctx.timers[1] : (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(rel.z, rel.x)) - 90.0d);
            } else {
                moveYaw = Look.b();
            }
            MoveUtil.a(event, moveYaw, 2);
        }
        if (this.ctx.timers[0] > 0.0f && this.target != null && AuraUtil.a(this.target, this.reachSetting.c().floatValue())) {
            event.a(0.0f);
            event.b(0.0f);
            this.ctx.timers[0] -= 1.0f;
        }
    }


    public void update() {
        if (this.target == null || !this.selector.valid(this.target) || (MaceUtil.a() && !this.landedCrit && !aM_.player.getCooldowns().isOnCooldown(Items.MACE.getDefaultInstance()))) {
            LivingEntity prev = this.target;
            boolean fresh = prev == null || !this.selector.valid(prev);
            this.target = (fresh && this.skipWhen.a("Враг за стеной").c().booleanValue()) ? this.selector.find(false).or(() -> this.selector.find(true)).orElse(null) : this.selector.find(true).orElse(null);
            if (this.target != prev && this.target != null) {
                this.ctx.timers[10] = 0.0f;
                this.ctx.timers[11] = 0.0f;
                Arrays.fill(this.ctx.pitchHistory, aM_.player != null ? aM_.player.getXRot() : 0.0f);
            }
        }
        this.attacker.restoreSlots();
        if (this.target != null) {


            rotateTick();
            this.attacker.attack();
            return;
        }
        this.ctx.timers[8] = 1.0f;
    }

    public void setWillLand(WillLandEvent event) {
        this.landedCrit = event.b() && !aM_.player.onGround();
    }

    public LivingEntity target() {
        return this.target;
    }

    public int ticks() {
        return this.ctx.ticks;
    }


    public void tick() {
        this.ctx.ticks++;
    }


    private void rotateTick() {
        Vec3 eyePos = aM_.player.getEyePosition();
        LivingEntity currentTarget = this.target;
        double dist = this.reachSetting.c().floatValue();
        boolean wallOk = this.type.c().contains("ФанТайм") || this.type.c().equals("Легит CX") || this.type.c().equals("Нейро") || this.type.c().equals("Атлантис") || this.type.c().equals("СпукиТайм") || this.type.c().equals("МХ") || !this.skipWhen.a("Враг за стеной").c().booleanValue();
        Vec3 targetPosition = AuraUtil.a(eyePos, currentTarget, dist, wallOk);
        float yawToTarget = targetPosition == Vec3.ZERO ? Look.b() : (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(targetPosition.z, targetPosition.x)) - 90.0d);
        float pitchToTarget = targetPosition == Vec3.ZERO ? Look.c() : (float) (-Math.toDegrees(Math.atan2(targetPosition.y, Math.hypot(targetPosition.x, targetPosition.z))));
        System.arraycopy(this.ctx.pitchHistory, 0, this.ctx.pitchHistory, 1, 29);
        this.ctx.pitchHistory[0] = pitchToTarget;
        this.lpYaw = yawToTarget;
        this.lpPitch = pitchToTarget;
        if (currentTarget != null && this.ctx.ticks >= 2 && ((ServerUtil.a.a(currentTarget) > 6.0f || this.ctx.timers[2] > 43.0f) && this.ctx.timers[2] >= 33.0f && ((this.ctx.ticks == 4 || Math.random() > 0.5d) && (!this.altHit || !AuraUtil.a(aM_.player.getYRot(), aM_.player.getXRot(), 3.0d, currentTarget, false))))) {
            ((MinecraftInvoker) aM_).invokeDoAttack();
            if (Math.random() > 0.5d) {
                this.altHit = !this.altHit;
            }
            this.ctx.timers[2] = (int) MathUtil.a(-10.0f, 10.0f);
        }
        boolean skip = (this.skipWhen.a("Используется предмет").c().booleanValue() && aM_.player.isUsingItem() && aM_.player.getUseItemRemainingTicks() > 0 && this.ctx.ticks >= 8) || !(this.skipWhen.a("Открыт контейнер") == null || !this.skipWhen.a("Открыт контейнер").c().booleanValue() || aM_.gui.screen() == null || (aM_.gui.screen() instanceof GUIScreen));
        if ((this.ctx.timers[3] <= 0.0f && this.attacker.canAttack()) || AuraUtil.a(this.ctx.ticks, currentTarget, skip)) {
            this.ctx.timers[3] = 1.0f;
            if (!aM_.player.isInWater() && this.smartSprint.c().booleanValue() && !aM_.player.onGround()) {
                this.ctx.timers[0] = 1.0f;
            }
        }
        if (Delta.h().d().t().F().m() && this.attacker.canAttack() && AuraUtil.a(currentTarget, 3.0d) && aM_.player.isFallFlying()) {
            Delta.h().d().k().a(new Rotation(yawToTarget, pitchToTarget), 180.0f, 0, 3);
        }
        if (!this.type.c().contains("ФанТайм") && !this.type.c().equals("Легит CX") && !this.type.c().equals("СпукиТайм") && !this.type.c().equals("МХ") && (InventoryUtil.b(Items.MACE) != -1 || (Delta.h().d().t().H().t() && aM_.player.fallDistance > 3.0f && ((Double) MaceUtil.a(aM_.player, (Level) aM_.level).map(pos -> {
            return Double.valueOf(pos.distanceTo(aM_.player.position()));
        }).orElse(Double.valueOf(0.0d))).doubleValue() > 2.0d && AuraUtil.a(currentTarget, 4.0d + (aM_.player.getDeltaMovement().length() * 3.0d))))) {
            float t = aM_.player.tickCount + aM_.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float smoothW = ((float) ((((Math.sin(t * 0.31f) * 0.5d) + (Math.sin((t * 0.73f) + 1.1f) * 0.3000000314327426d)) + (Math.sin((t * 1.7f) + 2.6f) * 0.2000000098386085d)) * 8.0d)) / 8.0f;
            float finalYaw = AuraUtil.a(aM_.player.getYRot(), yawToTarget, 0.8f);
            float finalPitch = AuraUtil.a(aM_.player.getXRot(), pitchToTarget, 0.8f);
            Delta.h().d().k().a(new Rotation(finalYaw + smoothW, finalPitch + smoothW), 180.0f, 1, 2);
        }
        syncContext(targetPosition, yawToTarget, pitchToTarget);
        AuraRotationHolder holder = this.rotations.get(this.type.c());
        if (holder != null) {
            holder.step.run();
        }
        this.ctx.timers[3] -= 1.0f;
        this.ctx.timers[5] -= 1.0f;
        this.ctx.timers[8] -= 1.0f;
        this.ctx.timers[1] = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(this.target.getZ() - aM_.player.getZ(), this.target.getX() - aM_.player.getX())) - 90.0d);
    }

    private void syncContext(Vec3 targetPosition, float yawToTarget, float pitchToTarget) {
        this.ctx.target = this.target;
        this.ctx.targetPos = targetPosition;
        this.ctx.yawToTarget = yawToTarget;
        this.ctx.pitchToTarget = pitchToTarget;
        this.ctx.reach = this.reachSetting.c().floatValue();
        this.ctx.mode = this.type.c();
    }
}
