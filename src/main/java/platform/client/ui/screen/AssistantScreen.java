package platform.client.ui.screen;

import static platform.api.module.Interface.aM_;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector2f;

import platform.api.annotation.Compile;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.interfaces.Action;
import platform.api.utils.auction.AutoBuyEntry;
import platform.client.Delta;
import platform.client.utils.math.MathUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.DeltaBlurProcessor;
import platform.client.utils.render.Draw2DProcessor;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ScaleUtil;
import platform.client.utils.render.pipeline.DeltaRenderUtil;


public class AssistantScreen extends Screen {
    private final RadialScreen b;
    private final List<AutoBuyEntry> c;
    private Vector2f d;
    private Vector2f e;
    private int f;
    private int g;

    public AssistantScreen(Component title) {
        super(title);
        this.b = new RadialScreen(2, 75.0f, 101.25f);
        this.c = Arrays.stream(AutoBuyEntry.values()).filter(info -> info.d() == Items.SPLASH_POTION).toList();
        this.d = null;
        this.e = null;
        this.f = -1;
        this.g = -1;
        a(this.b.a());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void extractBlurredBackground(GuiGraphicsExtractor context) {

    }

    public RadialScreen a() {
        return this.b;
    }


    public int b() {
        return this.g;
    }


    public void a(int count) {
        this.b.b(Math.max(count, 1));
        for (int i = 0; i < this.b.a(); i++) {
            this.b.a(i, ItemStack.EMPTY, c(i), true);
        }
    }


    public void a(int slot, AutoBuyEntry potion) {
        this.b.a(slot, potion.a(), potion.b(), c(slot), true);
    }

    private Vector2f center() {
        return new Vector2f(aM_.getWindow().getGuiScaledWidth() * 0.5f, aM_.getWindow().getGuiScaledHeight() * 0.5f);
    }

    @Compile
    public boolean mouseClicked(MouseButtonEvent event, boolean held) {
        double mouseX = MathUtil.scale(event.x(), 2);
        double mouseY = MathUtil.scale(event.y(), 2);
        int button = event.button();
        if (button == 0 && this.f != -1 && this.d != null && this.e != null) {
            for (int index = 0; index < this.c.size(); index++) {
                float rowY = this.d.y + ((float) index * 18.0f);
                if (MathUtil.a(mouseX, mouseY, this.d.x, rowY, this.e.x, 18.0f)) {
                    AutoBuyEntry potion = this.c.get(index);
                    for (int segment = 0; segment < this.b.a(); segment++) {
                        if (potion.a(this.b.c(segment))) {
                            c();
                            return true;
                        }
                    }
                    int target = this.f;
                    this.b.a(target, potion.a(), potion.b(), c(target), true);
                    c();
                    return true;
                }
            }
            c();
            return true;
        }
        if (button == 2) {
            return true;
        }
        Vector2f center = center();
        int slot;
        if (button == 0 && (slot = this.b.a(mouseX, mouseY, center)) >= 0 && !this.b.c(slot).isEmpty()) {
            this.g = slot;
            return true;
        }
        int before = this.b.a();
        boolean handled = this.b.a(mouseX, mouseY, button, center);
        if (this.b.a() != before) {
            c();
            for (int i = 0; i < this.b.a(); i++) {
                this.b.a(i, c(i));
            }
        }
        return handled ? true : super.mouseClicked(event, held);
    }

    @Compile
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && this.g != -1) {
            b(this.b.a(MathUtil.scale(event.x(), 2), MathUtil.scale(event.y(), 2), center()));
            return true;
        }
        return super.mouseReleased(event);
    }


    @Compile
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 89) {
            addSlot();
            return true;
        }
        return super.keyPressed(event);
    }

    private void addSlot() {
        int newSlot = this.b.a();
        this.b.b(newSlot + 1);
        this.b.a(newSlot, ItemStack.EMPTY, c(newSlot), true);
    }


    public void b(int target) {
        if (this.g == -1) {
            return;
        }
        if (target >= 0 && target != this.g) {
            this.b.a(this.g, target);
        } else {
            this.b.d(this.g).execute();
        }
        this.g = -1;
    }

    @Compile
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        DeltaBlurProcessor.setStrength(1.0f);
        DeltaRenderUtil.setShadowStrength(1.0f);
        DeltaRenderUtil.beginFrame();
        super.extractRenderState(context, mouseX, mouseY, delta);
        double mx = MathUtil.scale(mouseX, 2);
        double my = MathUtil.scale(mouseY, 2);
        ScaleUtil.a(context, 2);
        Vector2f center = center();
        this.b.a(context, (int) mx, (int) my, center);
        if (this.g != -1 && this.b.a(mx, my, center) != this.g) {
            Delta.h().d().j().a(context, this.b.c(this.g), (float) mx - 8.0f, (float) my - 8.0f, 0, 1.0f, 1.0f, false);
        }
        if (this.f != -1 && this.d != null) {
            a(context, (float) mx, (float) my);
        }
        ScaleUtil.a(context);
        DeltaRenderUtil.flush(context);
    }


    private void a(GuiGraphicsExtractor context, float mouseX, float mouseY) {
        Draw2DProcessor draw = Delta.h().d().i();
        int background = Delta.h().d().o().a(ThemeInfo.BACKGROUND_GUI).a();
        draw.a(context, this.d.x, this.d.y, this.e.x, this.e.y, 8.0f, background, 1.0f, background, 11.0f);
        draw.a(context, this.d.x, this.d.y, this.e.x, this.e.y, 8.0f, 0.5f, ColorUtil.a(255, 255, 255, 60));
        for (int index = 0; index < this.c.size(); index++) {
            AutoBuyEntry potion = this.c.get(index);
            float rowY = this.d.y + ((float) index * 18.0f);
            boolean hovered = MathUtil.a(mouseX, mouseY, this.d.x, rowY, this.e.x, 18.0f);
            if (hovered) {
                draw.a(context, this.d.x + 2.0f, rowY + 1.0f, this.e.x - 4.0f, 16.0f, 6.0f, ColorUtil.a(255, 255, 255, 12));
            }
            Delta.h().d().j().a(context, potion.a(), this.d.x + 5.0f, (rowY + 9.0f) - 6.0f, 0, 1.0f, 0.75f, false);
            Fonts.e.a(context, potion.b(), this.d.x + 5.0f + 12.0f + 4.0f, (rowY + 9.0f) - (Fonts.e.a(6.5f) * 0.5f), 6.5f, hovered ? ColorUtil.a(255, 255, 255, 220) : ColorUtil.a(255, 255, 255, InterfaceC0020Opcode.ap));
        }
    }


    private Action c(int slot) {
        return () -> {
            if (this.b.c(slot).isEmpty()) {
                this.f = slot;
                this.d = d(slot);
                this.e = d();
            } else {
                Delta.h().d().v().b().a(this.b.c(slot));
                aM_.player.closeContainer();
            }
        };
    }

    private void c() {
        this.f = -1;
        this.d = null;
        this.e = null;
    }


    private Vector2f d() {
        float maxLabelWidth = 0.0f;
        for (AutoBuyEntry potion : this.c) {
            maxLabelWidth = Math.max(maxLabelWidth, Fonts.e.a(potion.b(), 6.5f));
        }
        return new Vector2f(26.0f + maxLabelWidth + 10.0f, (float) this.c.size() * 18.0f);
    }


    private Vector2f d(int slot) {
        Vector2f center = center();
        double angleStep = 6.283185307179586d / ((double) this.b.a());
        double startAngle = -1.5707963267948966d + (angleStep * ((double) slot));
        return new Vector2f(
                center.x + (((float) Math.cos(startAngle + (angleStep * 0.5d))) * 88.125f) + 12.0f,
                center.y + (((float) Math.sin(startAngle + (angleStep * 0.5d))) * 88.125f) + 12.0f);
    }
}
