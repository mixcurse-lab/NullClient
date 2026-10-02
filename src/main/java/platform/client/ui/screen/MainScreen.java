package platform.client.ui.screen;

import platform.client.utils.render.ScaleUtil;
import platform.client.utils.render.pipeline.DeltaRenderUtil;
import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.ui.shader.GradientUtil;
import platform.client.ui.widget.EffectMarker;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Interface;
import platform.api.module.InterfaceC0020Opcode;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;
import platform.api.system.configs.ThemeInfo;
import platform.client.utils.render.Draw2DProcessor;
import platform.client.utils.render.AnimationUtil;
import platform.client.ui.element.Button;
import platform.api.annotation.Compile;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;

public class MainScreen extends Screen {
    private static final float[] a;
    private static boolean menuSoundPlayed = false;
    private final AnimationUtil b;
    private final Button c;
    private final Button d;
    private final Button e;
    private final Button f;
    private final List<Button> g;
    private final List<EffectMarker.a> h;
    private float i;
    private float j;
    private float k;
    private float l;

    @Compile
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        DeltaRenderUtil.beginFrame();
        super.extractRenderState(context, mouseX, mouseY, delta);
        this.b.a(aM_.gui.screen() instanceof MainScreen);
        this.b.a(0.0f, 1.0f, 0.15f, EasingList.g, delta);
        float fMin = Math.min(1.0f, this.b.c() / 0.9f);
        double dA = MathUtil.scale(mouseX, 2);
        double dA2 = MathUtil.scale(mouseY, 2);
        ScaleUtil.a(context, 2);
        int iMethod_4486 = aM_.getWindow().getGuiScaledWidth();
        int iMethod_4502 = aM_.getWindow().getGuiScaledHeight();
        a(context, iMethod_4486, iMethod_4502, (int) dA, (int) dA2, 1.25f - (EasingList.s.ease(fMin) * 0.2f));
        a(iMethod_4486, iMethod_4502);
        a(context, iMethod_4486 * 0.5f, ((iMethod_4502 - this.c.c()) * 0.5f) - 58.0f, fMin);
        Iterator<Button> it = this.g.iterator();
        while (it.hasNext()) {
            it.next().a(context, (int) dA, (int) dA2, delta, fMin);
        }
        a(context, fMin, (int) dA);
        EffectMarker.a(context, delta, this.h);
        ScaleUtil.a(context);
        DeltaRenderUtil.flush(context);
    }

    @Compile
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean held) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        List<EffectMarker.a> list = this.h;
        List<Button> list2 = this.g;
        double dA = MathUtil.scale(mouseX, 2);
        double dA2 = MathUtil.scale(mouseY, 2);
        EffectMarker.a(list, (float) dA, (float) dA2);
        float f = this.k + 1.75f + (this.i * 59.5f);
        if (MathUtil.a(dA, dA2, f, this.l + 1.75f, 16.0f, 16.0f)) {
            this.j = ((float) dA) - f;
            return true;
        }
        for (Button button2 : list2) {
            if (button2.e() != null && MathUtil.a(dA, dA2, button2.f(), button2.g(), button2.b(), button2.c())) {
                button2.e().run();
                return true;
            }
        }
        return super.mouseClicked(event, held);
    }

    @Compile
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.j >= 0.0f) return true;
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Compile
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        if (this.j < 0.0f) return super.mouseReleased(event);
        double dA = MathUtil.scale(event.x(), 2);
        float fMethod_15363 = Mth.clamp((((((float) dA) - this.j) - this.k) - 1.75f) / 59.5f, 0.0f, 1.0f);
        this.j = -1.0f;
        if (fMethod_15363 < 0.95f) return true;
        System.exit(0);
        return true;
    }

    static {
        NativeMethodLookup.lookup(MainScreen.class, 16);
        a = new float[2];
    }

    public MainScreen() {
        super(Component.empty());
        this.b = new AnimationUtil();
        this.h = new ArrayList();
        this.j = -1.0f;
        if (!menuSoundPlayed) {
            menuSoundPlayed = true;
            try {
                File flag = new File(new File(aM_.gameDirectory, "configs"), "general" + File.separator + "menu_sound_played.json");
                if (!flag.exists()) {
                    flag.getParentFile().mkdirs();
                    Files.writeString(flag.toPath(), "{\"menuSoundPlayed\":true}");
                    Delta.h().d().t().sounds().a("load_menu.wav", 1.0f);
                }
            } catch (Throwable ignored) {
            }
        }
        if (aM_.gui.screen() instanceof MainScreen) {
            this.b.c(1.0f);
            this.b.d(1.0f);
            this.b.e(1.0f);
        }
        this.c = new Button(88.0f, 38.0f, "\u041e\u0434\u0438\u043d\u043e\u0447\u043d\u044b\u0439 \u0420\u0435\u0436\u0438\u043c", () -> {
            aM_.gui.setScreen(new SelectWorldScreen(null));
        });
        this.d = new Button(88.0f, 38.0f, "\u0421\u0435\u0442\u0435\u0432\u0430\u044f \u0418\u0433\u0440\u0430", () -> {
            aM_.gui.setScreen(new JoinMultiplayerScreen(null));
        });
        this.e = new Button(181.0f, 30.0f, "\u0412\u044b\u0431\u043e\u0440 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u0430", () -> {
            aM_.gui.setScreen(new AltScreen());
        });
        this.f = new Button(79.0f, 19.5f, "\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438", () -> {
            aM_.gui.setScreen(new OptionsScreen(null, aM_.options, false));
        });
        this.g = List.of(this.c, this.d, this.e, this.f);
    }

    public void onClose() {
    }

    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }

    public static void a(GuiGraphicsExtractor context, int width, int height, int mouseX, int mouseY, float scale) {
        float marginX = width * 0.025f;
        float marginY = height * 0.025f;
        float[] fArr = a;
        fArr[0] = fArr[0] + ((Mth.clamp((((mouseX / (float)width) - 0.5f) * 2.0f) * marginX, (-marginX) * 0.9f, marginX * 0.9f) - a[0]) * 0.03f);
        float[] fArr2 = a;
        fArr2[1] = fArr2[1] + ((Mth.clamp((((mouseY / (float)height) - 0.5f) * 2.0f) * marginY, (-marginY) * 0.9f, marginY * 0.9f) - a[1]) * 0.03f);
        context.pose().pushMatrix();
        context.pose().translate(width / 2.0f, height / 2.0f);
        context.pose().scale(scale, scale);
        context.pose().translate(-width / 2.0f, -height / 2.0f);
        Delta.h().d().i().a(context, Identifier.fromNamespaceAndPath("delta", "pictures/main.png"), -marginX + a[0], -marginY + a[1], width + (marginX * 2.0f), height + (marginY * 2.0f), 0.0f, -1);
        context.pose().popMatrix();
    }

    private void a(int width, int height) {
        float mainY = (height - this.c.c()) / 2.0f;
        float mainX = (((width - this.c.b()) - 5.0f) - this.d.b()) / 2.0f;
        this.c.a(mainX, mainY);
        this.d.a(mainX + this.c.b() + 5.0f, mainY);
        this.e.a((width - this.e.b()) / 2.0f, mainY + this.c.c() + 5.0f);
        this.k = (width - 79.0f) / 2.0f;
        this.l = height * 0.85f;
        this.f.a((width - this.f.b()) / 2.0f, (this.l - this.f.c()) - 5.0f);
    }

    private void a(GuiGraphicsExtractor context, float open, int mouseX) {
        float target = this.j >= 0.0f ? Mth.clamp((((mouseX - this.j) - this.k) - 1.75f) / 59.5f, 0.0f, 1.0f) : 0.0f;
        this.i += (target - this.i) * 0.25f;
        float scale = 0.85f + (0.15f * EasingList.s.ease(open));
        Draw2DProcessor draw = Delta.h().d().i();
        context.pose().pushMatrix();
        context.pose().translate(this.k + 39.5f, this.l + 9.75f);
        context.pose().scale(scale, scale);
        context.pose().translate(-this.k - 39.5f, -this.l - 9.75f);
        float knobX = this.k + 1.75f + (this.i * 59.5f);
        float knobY = this.l + 1.75f;
        float centerX = knobX + 8.0f;
        float centerY = knobY + 8.0f;
        draw.b(context, this.k, this.l, 79.0f, 19.5f, 8.0f, ColorUtil.a(11, 11, 13, InterfaceC0020Opcode.bN), open);
        draw.a(context, this.k, this.l, 79.0f, 19.5f, 8.0f, 0.5f, ColorUtil.a(255, 255, 255, (int) (15.0f * open)));
        Fonts.e.c(context, "\u0412\u044b\u0439\u0442\u0438 \u0438\u0437 \u0438\u0433\u0440\u044b", this.k + 9.0f, this.l + 5.75f, 7.0f, ColorUtil.a(ColorUtil.a(220, 80, 80, 255), this.i * open), ((knobX - 3.0f) - this.k) - 9.0f);
        int knob = ColorUtil.a(ColorUtil.a(255, 255, 255, 13), ColorUtil.a(220, 80, 80, 40), this.i);
        draw.a(context, knobX, knobY, 16.0f, 16.0f, 7.0f, ColorUtil.a(knob, (ColorUtil.b(knob)[3] / 255.0f) * open));
        float iconLineH = Fonts.a.a(8.5f);
        context.pose().pushMatrix();
        context.pose().translate(centerX, centerY);
        context.pose().rotate((float) Math.toRadians((-90.0f) + (180.0f * this.i)));
        context.pose().translate(-centerX, -centerY);
        Fonts.a.a(context, "c", (centerX - (Fonts.a.a("c", 8.5f) / 2.0f)) + 1.0f, centerY - (iconLineH / 2.0f), 8.5f, ColorUtil.a(ColorUtil.a(-1, ColorUtil.a(220, 80, 80, 255), this.i), open));
        context.pose().popMatrix();
        context.pose().popMatrix();
    }

    private void a(GuiGraphicsExtractor context, float centerX, float titleY, float open) {
        float titleWidth = Fonts.e.a("NullClient", 12.0f);
        float scale = 0.85f + (0.15f * EasingList.s.ease(open));
        context.pose().pushMatrix();
        context.pose().translate(centerX, titleY + 8.0f);
        context.pose().scale(scale, scale);
        context.pose().translate(-centerX, -titleY - 8.0f);
        int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        Fonts.e.a(context, GradientUtil.a("NullClient", primary, 5.0f, 0.5f), centerX - (titleWidth / 2.0f), titleY + 1.5f, 12.0f, (double) open);
        Fonts.e.a(context, "26.2", centerX - (Fonts.e.a("26.2", 12.0f) / 2.0f), titleY + 15.0f, 12.0f, ColorUtil.a(255, 255, 255, (int) (160.0f * open)));
        context.pose().popMatrix();
    }
}




