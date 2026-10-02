package platform.client.features.modules.movement;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;
import platform.client.utils.rotation.Look;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.CameraPositionEvent;
import platform.api.event.events.other.CrosshairTargetEvent;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.SliderSetting;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.level.ClipContext;
import net.minecraft.client.CameraType;

@ModuleRegister(a = "Free Camera", b = "Позволяет свободно перемещать камеру, пока игрок остаётся на месте", c = Category.Movement)
public class FreeCamera extends Module implements Interface {
    private final SliderSetting b = new SliderSetting("Скорость движения XZ", 1.0f, 0.1f, 5.0f, 0.1f);
    private final SliderSetting c = new SliderSetting("Скорость движения Y", 1.0f, 0.1f, 5.0f, 0.1f);
    private final BooleanSetting d = new BooleanSetting("Замораживать пакеты в полёте", true);
    private Vec3 e;
    private Vec3 f;
    private Vec3 g;
    private float h;
    private float i;
    private boolean j;
    private boolean k;

    public FreeCamera() {
        a(this.d, this.b, this.c);
    }

    @Override
    public void b() {
        super.b();
        if (aM_.player == null) {
            d(true);
            a();
            return;
        }
        this.f = aM_.player.getEyePosition();
        this.e = aM_.player.getEyePosition();
        if (this.d.c().booleanValue()) {
            this.g = aM_.player.position();
        }
        d(false);
    }

    @Override
    public void c() {
        super.c();
        d(true);
    }

    @EventTarget
    public void a(CameraPositionEvent event) {
        if (this.e != null && aM_.player.isAlive()) {
            if (aM_.options.getCameraType() != CameraType.FIRST_PERSON) {
                aM_.options.setCameraType(CameraType.FIRST_PERSON);
            }
            Vec3 basePrev = this.f != null ? this.f : this.e;
            double tickDelta = (double) aM_.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            Vec3 interpolated = new Vec3(basePrev.x + ((this.e.x - basePrev.x) * tickDelta), basePrev.y + ((this.e.y - basePrev.y) * tickDelta), basePrev.z + ((this.e.z - basePrev.z) * tickDelta));
            event.a(interpolated);
            event.a(true);
        }
    }

    @EventTarget
    public void a(CrosshairTargetEvent event) {
        if (this.e != null && aM_.player.isAlive()) {
            event.a((HitResult) aM_.level.clip(new ClipContext(this.e, this.e.add(aM_.player.getViewVector(event.b()).scale(aM_.player.blockInteractionRange())), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, aM_.player)));
            event.a(true);
        }
    }

    @EventTarget
    public void a(TickEvent eventTick) {
        float f;
        float f2;
        if (this.e != null && aM_.player.isAlive()) {
            if (aM_.gui.screen() == null) {
                if (aM_.options.keyUp.isDown()) {
                    f = 1.0f;
                } else {
                    f = aM_.options.keyDown.isDown() ? -1.0f : 0.0f;
                }
                this.h = f;
                if (InputConstants.isKeyDown(aM_.getWindow(), 65)) {
                    f2 = 1.0f;
                } else {
                    f2 = InputConstants.isKeyDown(aM_.getWindow(), 68) ? -1.0f : 0.0f;
                }
                this.i = f2;
                this.j = aM_.options.keyJump.isDown();
                this.k = aM_.options.keyShift.isDown();
            } else {
                d(false);
            }
            if (this.d.c().booleanValue() && !aM_.player.onGround()) {
                aM_.player.setDeltaMovement(0.0d, 0.0d, 0.0d);
                if (this.g != null) {
                    aM_.player.setPos(this.g.x, this.g.y, this.g.z);
                }
            }
            this.f = this.e;
            this.e = this.e.add(((((double) this.h) * (-Math.sin(Math.toRadians(Look.b())))) + (((double) this.i) * Math.cos(Math.toRadians(Look.b())))) * ((double) this.b.c().floatValue()), (this.j ? this.c.c().floatValue() : 0.0d) - (this.k ? this.c.c().floatValue() : 0.0d), ((((double) this.h) * Math.cos(Math.toRadians(Look.b()))) + (((double) this.i) * Math.sin(Math.toRadians(Look.b())))) * ((double) this.b.c().floatValue()));
        }
    }

    @EventTarget
    public void a(InputEvent event) {
        float f;
        float f2;
        if (this.e != null && aM_.player.isAlive()) {
            if (aM_.gui.screen() != null) {
                event.a(0.0f);
                event.b(0.0f);
                event.b(false);
                event.c(false);
                return;
            }
            if (InputConstants.isKeyDown(aM_.getWindow(), 265)) {
                f = 1.0f;
            } else {
                f = InputConstants.isKeyDown(aM_.getWindow(), 264) ? -1.0f : 0.0f;
            }
            event.a(f);
            if (InputConstants.isKeyDown(aM_.getWindow(), 262)) {
                f2 = -1.0f;
            } else {
                f2 = InputConstants.isKeyDown(aM_.getWindow(), 263) ? 1.0f : 0.0f;
            }
            event.b(f2);
            event.b(false);
            event.c(false);
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.b() && this.d.c().booleanValue() && this.e != null && !aM_.player.onGround() && aM_.player.isAlive()) {
            if ((event.d() instanceof ServerboundPlayerInputPacket) || (event.d() instanceof ServerboundPlayerCommandPacket)) {
                event.a(true);
            }
        }
    }

    private void d(boolean clearPositions) {
        if (clearPositions) {
            this.e = null;
            this.f = null;
            this.g = null;
        } else {
            this.i = 0.0f;
            this.h = 0.0f;
            this.k = false;
            this.j = false;
        }
    }
}


