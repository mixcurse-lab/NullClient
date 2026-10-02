package platform.client.features.modules.render;

import static platform.api.module.Interface.aM_;
import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.CrosshairEvent;
import platform.api.event.events.render.DrawEvent;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.Draw2DProcessor;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.api.module.setting.SliderSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.EntityHitResult;

@ModuleRegister(a = "Crosshair", b = "Отображает настраиваемый прицел на экране", c = Category.Render)
public class Crosshair extends Module {
    private final SliderSetting b = new SliderSetting("Расстояние от центра", 0.0f, 0.0f, 6.0f, 0.5f);
    private final SliderSetting c = new SliderSetting("Длина сегментов", 2.5f, 2.0f, 5.0f, 0.5f);
    private final MultiModeSetting d = new MultiModeSetting("Параметры прицела", new BooleanSetting("Адаптивность", false), new BooleanSetting("Контур", true), new BooleanSetting("Центральная метка", false));

    public Crosshair() {
        a(this.b, this.c, this.d);
    }

    @EventTarget
    public void a(CrosshairEvent e) {
        if (aM_.options.getCameraType().isFirstPerson()) {
            e.a(true);
        }
    }

    @EventTarget
    public void a(DrawEvent event) {
        if (event.b() && aM_.options.getCameraType().isFirstPerson()) {
            a(event, aM_.getWindow().getGuiScaledWidth() / 2.0f, aM_.getWindow().getGuiScaledHeight() / 2.0f, 1.0f - aM_.player.getAttackStrengthScale(event.g()));
        }
    }

    private void a(DrawEvent drawEvent, float centerX, float centerY, float cooldown) {
        Draw2DProcessor draw2D = drawEvent.d();
        GuiGraphicsExtractor context = drawEvent.i();
        float actualGap = this.d.a("Адаптивность").c().booleanValue() ? this.b.c().floatValue() + (8.0f * cooldown) : this.b.c().floatValue();
        int color = aM_.hitResult instanceof EntityHitResult ? ColorUtil.a(255, 64, 64) : -1;
        if (this.d.a("Контур").c().booleanValue()) {
            draw2D.a(context, (centerX + actualGap) - 0.5f, (centerY - 0.5f) - 0.5f, this.c.c().floatValue() + 1.0f, 2.0f, 0.0f, ColorUtil.a(0, 0, 0));
            draw2D.a(context, ((centerX - actualGap) - this.c.c().floatValue()) - 0.5f, (centerY - 0.5f) - 0.5f, this.c.c().floatValue() + 1.0f, 2.0f, 0.0f, ColorUtil.a(0, 0, 0));
            draw2D.a(context, (centerX - 0.5f) - 0.5f, ((centerY - actualGap) - this.c.c().floatValue()) - 0.5f, 2.0f, this.c.c().floatValue() + 1.0f, 0.0f, ColorUtil.a(0, 0, 0));
            draw2D.a(context, (centerX - 0.5f) - 0.5f, (centerY + actualGap) - 0.5f, 2.0f, this.c.c().floatValue() + 1.0f, 0.0f, ColorUtil.a(0, 0, 0));
            draw2D.a(context, centerX + actualGap, centerY - 0.5f, this.c.c().floatValue(), 1.0f, 0.0f, color);
            draw2D.a(context, (centerX - actualGap) - this.c.c().floatValue(), centerY - 0.5f, this.c.c().floatValue(), 1.0f, 0.0f, color);
            draw2D.a(context, centerX - 0.5f, (centerY - actualGap) - this.c.c().floatValue(), 1.0f, this.c.c().floatValue(), 0.0f, color);
            draw2D.a(context, centerX - 0.5f, centerY + actualGap, 1.0f, this.c.c().floatValue(), 0.0f, color);
        } else {
            draw2D.a(context, centerX + actualGap, centerY - 0.5f, this.c.c().floatValue(), 1.0f, 0.0f, color);
            draw2D.a(context, (centerX - actualGap) - this.c.c().floatValue(), centerY - 0.5f, this.c.c().floatValue(), 1.0f, 0.0f, color);
            draw2D.a(context, centerX - 0.5f, (centerY - actualGap) - this.c.c().floatValue(), 1.0f, this.c.c().floatValue(), 0.0f, color);
            draw2D.a(context, centerX - 0.5f, centerY + actualGap, 1.0f, this.c.c().floatValue(), 0.0f, color);
        }
        if (this.d.a("Центральная метка").c().booleanValue() && actualGap > 0.0f) {
            float x = centerX - 0.5f;
            float y = centerY - 0.5f;
            if (this.d.a("Контур").c().booleanValue()) {
                draw2D.a(context, x - 0.5f, y - 0.5f, 2.0f, 2.0f, 0.0f, ColorUtil.a(0, 0, 0));
                draw2D.a(context, x, y, 1.0f, 1.0f, 0.0f, color);
            } else {
                draw2D.a(context, x, y, 1.0f, 1.0f, 0.0f, color);
            }
        }
    }
}


