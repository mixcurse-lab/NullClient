package platform.client.features.modules.render;

import static platform.api.module.Interface.aM_;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.WorldRenderEvent;
import platform.api.event.events.player.JumpEvent;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.jump.JumpCircleRenderer;
import platform.client.utils.render.jump.JumpGlowRenderer;
import platform.client.utils.render.jump.JumpWaveRenderer;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ColorSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@ModuleRegister(a = "Jump Circles", b = "Создаёт расширяющиеся кольца и волны в точках прыжка", c = Category.Render)
public class JumpCircles extends Module {
    public final SliderSetting zhizn = new SliderSetting("Время жизни", 800.0F, 200.0F, 3000.0F, 10.0F);
    public final SliderSetting radius = new SliderSetting("Радиус", 1.5F, 0.5F, 5.0F, 0.1F);
    public final ModeSetting rezhim = new ModeSetting("Режим", "Текстура", "Текстура", "Волна");
    public final SliderSetting silaVolny = new SliderSetting("Сила волны", 0.6F, 0.0F, 2.0F, 0.05F);
    public final SliderSetting radiusVolny = new SliderSetting("Радиус волны", 3.0F, 0.5F, 10.0F, 0.1F);
    public final SliderSetting vremyaVolny = new SliderSetting("Время волны", 600.0F, 150.0F, 2500.0F, 10.0F);
    public final BooleanSetting svechenie = new BooleanSetting("Свечение", false);
    public final SliderSetting iskazhenie = new SliderSetting("Искажение", 1.0F, 0.0F, 3.0F, 0.05F);
    public final BooleanSetting raduga = new BooleanSetting("Радужный цвет", false);
    public final ColorSetting tsvet = new ColorSetting("Цвет", ColorUtil.a(119, 101, 255, 255));

    private final List<Krug> krugi = new ArrayList<>();
    private final List<Volna> volny = new ArrayList<>();
    private final JumpCircleRenderer circleRenderer = new JumpCircleRenderer();
    private final JumpWaveRenderer waveRenderer = new JumpWaveRenderer();
    private final JumpGlowRenderer glowRenderer = new JumpGlowRenderer();
    private Level lastLevel;

    public JumpCircles() {
        tsvet.a(() -> !raduga.c());
        this.silaVolny.a(() -> this.rezhim.l("Волна"));
        this.radiusVolny.a(() -> this.rezhim.l("Волна"));
        this.vremyaVolny.a(() -> this.rezhim.l("Волна"));
        a(this.zhizn, this.radius, this.rezhim, this.silaVolny, this.radiusVolny, this.vremyaVolny, this.svechenie, this.iskazhenie, this.raduga, this.tsvet);
    }

    @EventTarget
    public void a(JumpEvent event) {
        Vec3 position = event.b().position();
        this.krugi.add(new Krug(position, System.currentTimeMillis(), this.zhizn.c()));
        this.volny.add(new Volna(position, System.currentTimeMillis(), this.vremyaVolny.c(),
                this.silaVolny.c() * this.iskazhenie.c(), this.radiusVolny.c()));
    }

    @EventTarget
    public void a(WorldRenderEvent event) {
        renderWorld(event.b());
    }

    private void renderWorld(float partialTicks) {
        if (aM_.player == null || aM_.level == null) {
            return;
        }
        if (aM_.level != this.lastLevel) {
            this.krugi.clear();
            this.volny.clear();
            this.lastLevel = aM_.level;
        }

        long now = System.currentTimeMillis();
        float time = (now % 1000000L) / 1000.0F;
        boolean waveMode = this.rezhim.l("Волна");
        boolean glowEnabled = this.svechenie.c().booleanValue();
        int fixedColor = this.raduga.c().booleanValue()
                ? (-16777216 | (Color.HSBtoRGB((now % 4000L) / 4000.0F, 1.0F, 1.0F) & 16777215))
                : (this.tsvet.c() | 0xFF000000);

        if (waveMode) {
            Iterator<Volna> volnaIterator = this.volny.iterator();
            while (volnaIterator.hasNext()) {
                Volna v = volnaIterator.next();
                if (v.zavershen(now)) {
                    volnaIterator.remove();
                    continue;
                }
                float syroi = v.syroiProgress(now);
                float progressKolca = easeOutCubic(syroi);
                float zatuhanie = zatuhanie(syroi);
                this.waveRenderer.render(v.poziciya(), v.maksRadius(), progressKolca, v.sila(), zatuhanie, fixedColor);
                if (glowEnabled) {
                    this.glowRenderer.render(v.poziciya(), v.maksRadius(), progressKolca, fixedColor, zatuhanie, time);
                }
            }
            return;
        }

        Iterator<Krug> krugIterator = this.krugi.iterator();
        while (krugIterator.hasNext()) {
            Krug krug = krugIterator.next();
            if (krug.zavershen(now)) {
                krugIterator.remove();
                continue;
            }
            float syroi = krug.syroiProgress(now);
            float progress = easeOutCubic(syroi);
            float fade = 1.0F - syroi;
            this.circleRenderer.render(krug.poziciya(), this.radius.c() * progress, fade, time, fixedColor, glowEnabled);
        }
    }

    @Override
    public void c() {
        super.c();
        this.krugi.clear();
        this.volny.clear();
        this.waveRenderer.release();
        this.glowRenderer.release();
    }

    private static float zatuhanie(float syroi) {
        float ogr = Math.clamp(syroi, 0.0F, 1.0F);
        float poyavlenie = gladkiyShag(ogr / 0.18F);
        float ischeznovenie = gladkiyShag((1.0F - ogr) / 0.82F);
        return Math.clamp(poyavlenie * ischeznovenie, 0.0F, 1.0F);
    }

    private static float gladkiyShag(float znachenie) {
        float t = Math.clamp(znachenie, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float easeOutCubic(float znachenie) {
        float obratnoe = 1.0F - Math.clamp(znachenie, 0.0F, 1.0F);
        return 1.0F - obratnoe * obratnoe * obratnoe;
    }

    private record Krug(Vec3 poziciya, long sozdan, float zhiznMs) {
        private float syroiProgress(long now) {
            return Math.clamp((now - sozdan) / zhiznMs, 0.0F, 1.0F);
        }

        private boolean zavershen(long now) {
            return now - sozdan >= zhiznMs;
        }
    }

    private record Volna(Vec3 poziciya, long sozdan, float zhiznMs, float sila, float maksRadius) {
        private float syroiProgress(long now) {
            return Math.clamp((now - sozdan) / zhiznMs, 0.0F, 1.0F);
        }

        private boolean zavershen(long now) {
            return now - sozdan >= zhiznMs;
        }
    }
}
