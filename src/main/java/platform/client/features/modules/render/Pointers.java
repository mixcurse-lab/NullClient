package platform.client.features.modules.render;

import static platform.api.module.Interface.aM_;

import platform.api.module.Category;
import platform.client.Delta;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.DrawEvent;
import platform.client.utils.render.ColorUtil;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.api.module.setting.SliderSetting;
import platform.client.utils.rotation.Look;
import platform.client.utils.math.MathUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@ModuleRegister(a = "Pointers", b = "Указывает лучами направление к игрокам", c = Category.Render)
public class Pointers extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Визуальные настройки", new BooleanSetting("Фильтр по друзьям", false), new BooleanSetting("Трассировка до игрока", true), new BooleanSetting("Навигационная стрелка", true));
    private final SliderSetting c = new SliderSetting("Размер стрелки", 7.0f, 5.0f, 15.0f, 1.0f);
    private final SliderSetting d = new SliderSetting("Отступ от центра", 30.0f, 20.0f, 50.0f, 1.0f);
    private float e;

    public Pointers() {
        a(this.b, this.c, this.d);
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (event.c() && this.b.a("Трассировка до игрока").c().booleanValue()) {
            Vec3 cam = aM_.getEntityRenderDispatcher().camera.position();
            Vec3 start = new Vec3(0.0d, 0.0d, 27.0d).xRot((float) (-Math.toRadians(aM_.getEntityRenderDispatcher().camera.xRot()))).yRot((float) (-Math.toRadians(aM_.getEntityRenderDispatcher().camera.yRot()))).add(cam);
            for (Entity _e : aM_.level.getEntitiesOfClass(LocalPlayer.class, aM_.player.getBoundingBox().inflate(256.0), e -> true)) {
                if (_e != aM_.player && _e.isAlive()) {
                    Vec3 pos = MathUtil.a(_e, event.g()).add(0.0d, _e.getDimensions(_e.getPose()).height() / 2.0f, 0.0d);
                    boolean isFriend = Delta.h().d().e().d(_e.getName().getString());
                    if (!this.b.a("Фильтр по друзьям").c().booleanValue() || isFriend) {
                        event.e().a(event.h(), start, pos, start.add(pos).scale(0.5d), isFriend ? ColorUtil.a(0, 255, 0, 255) : ColorUtil.a(255, 255, 255, 255), 2.0f);
                    }
                }
            }
        }
        if (event.b() && this.b.a("Навигационная стрелка").c().booleanValue()) {
            this.e = MathUtil.c(this.e, this.e + Mth.wrapDegrees(Look.b() - this.e), 2.0f);
            for (Entity _e : aM_.level.getEntitiesOfClass(LocalPlayer.class, aM_.player.getBoundingBox().inflate(256.0), e -> true)) {
                if (_e != aM_.player && _e.isAlive()) {
                    boolean isFriend2 = Delta.h().d().e().d(_e.getName().getString());
                    if (!this.b.a("Фильтр по друзьям").c().booleanValue() || isFriend2) {
                        Vec3 pos2 = MathUtil.a(_e, event.g());
                        Vec3 eye = MathUtil.a(aM_.player, event.g());
                        float angle = Mth.wrapDegrees(((float) Math.toDegrees(Math.atan2(eye.x - pos2.x, pos2.z - eye.z))) - this.e);
                        float radians = (float) Math.toRadians(angle);
                        float cx = (aM_.getWindow().getGuiScaledWidth() / 2.0f) + (((float) Math.sin(radians)) * this.d.c().floatValue());
                        float cy = (aM_.getWindow().getGuiScaledHeight() / 2.0f) - (((float) Math.cos(radians)) * this.d.c().floatValue());
                        event.i().pose().pushMatrix();
                        event.i().pose().translate(cx, cy);
                        event.i().pose().rotate(radians);
                        event.d().a(event.i(), Identifier.fromNamespaceAndPath("delta", "pictures/pointer.png"), (-this.c.c().floatValue()) / 2.0f, (-this.c.c().floatValue()) / 2.0f, this.c.c().floatValue(), this.c.c().floatValue(), 0.0f, isFriend2 ? ColorUtil.a(85, 255, 85, InterfaceC0020Opcode.aL) : ColorUtil.a(255, 255, 255, InterfaceC0020Opcode.aL));
                        event.i().pose().popMatrix();
                    }
                }
            }
        }
    }
}


