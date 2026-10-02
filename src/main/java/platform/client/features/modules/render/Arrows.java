package platform.client.features.modules.render;

import static platform.api.module.Interface.aM_;

import platform.api.module.Category;
import platform.client.Delta;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.DrawEvent;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.AnimationUtil;
import platform.api.module.setting.ColorSetting;
import platform.api.module.setting.SliderSetting;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

@ModuleRegister(a = "Arrows", b = "Рисует стрелки вокруг прицела в сторону игроков за экраном", c = Category.Render)
public class Arrows extends Module {
    private final SliderSetting arrowsDistance = new SliderSetting("Дистанция", 20.0f, 1.0f, 20.0f, 0.5f);
    private final SliderSetting arrowSize = new SliderSetting("Размер стрелки", 15.0f, 10.0f, 25.0f, 0.5f);
    private final ColorSetting arrowColor = new ColorSetting("Цвет стрелки", ColorUtil.a(255, 255, 255, 255));
    private final ColorSetting friendColor = new ColorSetting("Цвет друзей", ColorUtil.a(0, 255, 0, 255));
    private final AnimationUtil distanceAnimation = new AnimationUtil();
    private final AnimationUtil yawAnimation = new AnimationUtil();
    private final AnimationUtil pitchAnimation = new AnimationUtil();
    private final AnimationUtil cameraYawAnimation = new AnimationUtil();
    private final List<Arrow> arrows = new ArrayList<>();
    private float lastCameraYaw;
    private float smoothCameraYaw;
    private boolean yawInitialized;

    public Arrows() {
        a(this.arrowsDistance, this.arrowSize, this.arrowColor, this.friendColor);
    }

    @Override
    public void c() {
        super.c();
        this.arrows.clear();
        this.yawInitialized = false;
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (!event.b() || aM_.player == null || aM_.level == null || !aM_.options.getCameraType().isFirstPerson()) {
            return;
        }
        if (aM_.getEntityRenderDispatcher().camera == null) {
            return;
        }

        float size = 45.0f + this.arrowsDistance.c().floatValue();
        if (aM_.gui.screen() instanceof InventoryScreen) {
            size += 80.0f;
        }
        if (aM_.player.isShiftKeyDown()) {
            size -= 20.0f;
        }
        if (aM_.player.input.getMoveVector().y != 0.0f || aM_.player.input.getMoveVector().x != 0.0f) {
            size += 10.0f;
        }

        float strafeInput = aM_.player.input.getMoveVector().x;
        float forwardInput = aM_.player.input.getMoveVector().y;

        float camYaw = aM_.getEntityRenderDispatcher().camera.yRot();
        if (!this.yawInitialized) {
            this.lastCameraYaw = camYaw;
            this.smoothCameraYaw = camYaw;
            this.yawInitialized = true;
        }
        this.smoothCameraYaw += Mth.wrapDegrees(camYaw - this.lastCameraYaw);
        this.lastCameraYaw = camYaw;

        this.yawAnimation.a(strafeInput * 5.0f, 0.8f);
        this.pitchAnimation.a(forwardInput * 5.0f, 0.8f);
        this.cameraYawAnimation.a(this.smoothCameraYaw, 1.5f);
        this.distanceAnimation.a(size, 0.8f);

        for (Arrow arrow : this.arrows) {
            arrow.active = false;
        }

        for (AbstractClientPlayer player : aM_.level.players()) {
            if (!isValidPlayer(player)) {
                continue;
            }
            Arrow arrow = findArrow(player);
            if (arrow == null) {
                arrow = new Arrow(player);
                this.arrows.add(arrow);
            }
            arrow.active = true;
        }

        for (int i = this.arrows.size() - 1; i >= 0; i--) {
            Arrow arrow = this.arrows.get(i);
            if (!arrow.active) {
                arrow.fade.a(0.0f, 1.2f);
                if (arrow.fade.a() <= 0.01f) {
                    this.arrows.remove(i);
                }
            }
        }

        int screenWidth = aM_.getWindow().getGuiScaledWidth();
        int screenHeight = aM_.getWindow().getGuiScaledHeight();
        float centerX = screenWidth / 2.0f;
        float centerY = screenHeight / 2.0f;
        double cameraYaw = this.cameraYawAnimation.a();
        double cos = Mth.cos((float) Math.toRadians(cameraYaw));
        double sin = Mth.sin((float) Math.toRadians(cameraYaw));

        for (Arrow arrow : this.arrows) {
            float alpha = arrow.active ? arrow.fade.a(1.0f, 1.2f) : arrow.fade.a();
            if (alpha <= 0.01f) {
                continue;
            }

            Player player = arrow.player;
            Vec3Holder pos = interpolated(player, event.g());
            double playerX = pos.x - aM_.getEntityRenderDispatcher().camera.position().x;
            double playerZ = pos.z - aM_.getEntityRenderDispatcher().camera.position().z;
            double rotY = -(playerZ * cos - playerX * sin);
            double rotX = -(playerX * cos + playerZ * sin);
            float angle = (float) (Math.atan2(rotY, rotX) * 180.0 / Math.PI);
            float x = ((float) (this.distanceAnimation.a() * alpha * Mth.cos((float) Math.toRadians(angle)))) + centerX + this.yawAnimation.a();
            float y = ((float) (this.distanceAnimation.a() * alpha * Mth.sin((float) Math.toRadians(angle)))) + centerY + this.pitchAnimation.a();
            boolean isFriend = Delta.h().d().e().d(player.getName().getString());
            int baseColor = isFriend ? (this.friendColor.c() | 0xFF000000) : (this.arrowColor.c() | 0xFF000000);
            int color = ColorUtil.a(baseColor, alpha);
            drawArrow(event, x, y, angle, color);
        }
    }

    private void drawArrow(DrawEvent event, float x, float y, float angle, int color) {
        float size = this.arrowSize.c().floatValue();
        float halfSize = size * 0.5f;
        event.i().pose().pushMatrix();
        event.i().pose().translate(x, y);
        event.i().pose().rotate((float) Math.toRadians(angle + 90.0f));
        event.d().a(event.i(), Identifier.fromNamespaceAndPath("delta", "pictures/triangle.png"), -halfSize, -halfSize, size, size, 0.0f, color);
        event.i().pose().popMatrix();
    }

    private Arrow findArrow(Player player) {
        for (Arrow arrow : this.arrows) {
            if (arrow.player == player) {
                return arrow;
            }
        }
        return null;
    }

    private boolean isValidPlayer(Player player) {
        return player != aM_.player && player.isAlive();
    }

    private static Vec3Holder interpolated(Player player, float partialTicks) {
        double x = player.xo + ((player.getX() - player.xo) * ((double) partialTicks));
        double z = player.zo + ((player.getZ() - player.zo) * ((double) partialTicks));
        return new Vec3Holder(x, z);
    }

    private record Vec3Holder(double x, double z) {
    }

    private static final class Arrow {
        private final Player player;
        private final AnimationUtil fade = new AnimationUtil();
        private boolean active;

        private Arrow(Player player) {
            this.player = player;
            this.fade.b(0.0f);
        }
    }
}
