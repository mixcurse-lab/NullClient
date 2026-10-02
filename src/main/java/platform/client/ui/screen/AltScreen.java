package platform.client.ui.screen;

import platform.client.utils.render.ScaleUtil;
import platform.client.utils.render.ScissorUtil;
import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.ui.shader.GradientUtil;
import static platform.api.module.Interface.aM_;
import platform.api.module.Interface;
import platform.api.module.InterfaceC0020Opcode;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.math.MathUtil;

import platform.api.system.configs.ThemeInfo;
import platform.client.Delta;
import platform.client.services.Processor_2;
import platform.client.utils.render.Draw2DProcessor;
import platform.api.auth.AccountConstructor;
import platform.client.utils.render.pipeline.DeltaRenderUtil;

import platform.client.utils.render.AnimationUtil;
import platform.client.ui.element.TextField;
import platform.api.annotation.Compile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.util.Mth;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import java.util.UUID;

public class AltScreen extends Screen {
    private final AnimationUtil a;
    private final AnimationUtil b;
    private final TextField c;
    private final List<a> d;
    a e;
    private a f;
    private AccountConstructor g;
    private float h;
    private boolean i;

    @Compile
    public void extractRenderState(GuiGraphicsExtractor context, int mx, int my, float delta) {
        DeltaRenderUtil.beginFrame();
        super.extractRenderState(context, mx, my, delta);
        this.a.a(aM_.gui.screen() instanceof AltScreen);
        this.a.a(0.0f, 1.0f, 0.15f, EasingList.g, delta);
        float fMin = Math.min(1.0f, this.a.c() / 0.9f);
        double dA = MathUtil.scale(mx, 2);
        double dA2 = MathUtil.scale(my, 2);
        ScaleUtil.a(context, 2);
        int w = aM_.getWindow().getGuiScaledWidth();
        int h2 = aM_.getWindow().getGuiScaledHeight();
        float f = (EasingList.s.ease(fMin) * 0.2f) + 1.05f;
        MainScreen.a(context, w, h2, (int) dA, (int) dA2, f);
        float f2 = (w - 190.0f) * 0.5f;
        float f3 = (h2 - 250.0f) * 0.5f;
        a(context, w, f2, f3, fMin);
        a(context, w, h2, f, f2, f3, (int) dA, (int) dA2, fMin);
        a(context, f2, f3, (int) dA, (int) dA2, delta, fMin);
        ScaleUtil.a(context);
        DeltaRenderUtil.flush(context);
    }

    @Compile
    public boolean mouseClicked(MouseButtonEvent event, boolean held) {
        double dA = MathUtil.scale(event.x(), 2);
        double dA2 = MathUtil.scale(event.y(), 2);
        int button = event.button();
        this.c.a(dA, dA2, button);
        float fMethod_32118 = this.c.d().x;
        float fMethod_32119 = this.c.d().y;
        float fMethod_321110 = this.c.e().x;
        float f = fMethod_32118 + fMethod_321110;
        if (MathUtil.a(dA, dA2, 5.0f + f, fMethod_32119, 18.0f, 18.0f)) {
            a(this.c.g().toString());
            return true;
        }
        if (MathUtil.a(dA, dA2, 28.0f + f, fMethod_32119, 150.0f - fMethod_321110, 18.0f)) {
            a(b());
            return true;
        }
        if (MathUtil.a(dA, dA2, fMethod_32118 + 7.0f, fMethod_32119 + 33.0f, 160.0f, 20.0f)) {
            this.d.forEach(account -> account.g = true);
            a().clear();
            Delta.h().d().h().a((AccountConstructor) null);
            saveAccounts();
            return true;
        }
        if (this.e == null) {
            return super.mouseClicked(event, held);
        }
        if (button == 1) {
            a(this.e);
            return true;
        }
        this.f = this.e;
        a aVar = this.e;
        if (aVar == null) {
            return true;
        }
        this.h = ((float) dA2) - aVar.e;
        this.i = false;
        return true;
    }

    @Compile
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        this.c.b(MathUtil.scale(event.x(), 2), MathUtil.scale(event.y(), 2), event.button());
        if (this.f == null) {
            return super.mouseDragged(event, deltaX, deltaY);
        }
        double dA = MathUtil.scale(event.y(), 2) - ((double) this.h);
        a aVar = this.f;
        if (aVar == null) {
            return true;
        }
        if (Math.abs(dA - ((double) aVar.e)) <= 8.0d) {
            return true;
        }
        this.i = true;
        return true;
    }

    @Compile
    public boolean mouseReleased(MouseButtonEvent event) {
        AccountConstructor accountConstructor;
        if (this.f == null) {
            return super.mouseReleased(event);
        }
        if (!this.i) {
            a aVar = this.f;
            if (aVar != null) {
                if (aVar.f) {
                    a aVar2 = this.f;
                    if (aVar2 != null) {
                        AccountConstructor accountConstructor2 = aVar2.b;
                        a aVar3 = this.f;
                        if (aVar3 != null && (accountConstructor = aVar3.b) != null) {
                            boolean z = !accountConstructor.d();
                            if (accountConstructor2 != null) {
                                accountConstructor2.b(z);
                            }
                            saveAccounts();
                        }
                    }
                } else {
                    a aVar4 = this.f;
                    if (aVar4 != null) {
                        a(aVar4.b);
                    }
                }
            }
            return true;
        }
        c();
        this.f = null;
        return true;
    }

    @Compile
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        this.b.a(((float) dy) * 29.0f);
        return true;
    }

    @Compile
    public boolean charTyped(CharacterEvent event) {
        if (!this.c.j()) {
            return super.charTyped(event);
        }
        this.c.a((char) event.codepoint(), 0);
        return true;
    }

    @Compile
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int modifiers = event.modifiers();
        TextField textField = this.c;
        if ((modifiers & 2) != 0 && keyCode == 86) {
            d();
            return true;
        }
        if (!textField.j()) {
            if (keyCode != 256) {
                return super.keyPressed(event);
            }
            aM_.gui.setScreen(new MainScreen());
            return true;
        }
        if (keyCode == 257) {
            a(textField.g().toString());
            return true;
        }
        textField.a(event);
        return true;
    }

    static {
        NativeMethodLookup.lookup(AltScreen.class, 15);
    }

    public AltScreen() {
        super(Component.empty());
        this.a = new AnimationUtil();
        this.b = new AnimationUtil();
        this.c = new TextField(TextField.a.ALT_MANAGER);
        this.d = new ArrayList();
        this.c.a("\u041d\u0438\u043a\u043d\u0435\u0439\u043c");
        a().forEach(account -> this.d.add(new a(account)));
    }

    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    }

    private void a(GuiGraphicsExtractor context, int w, float px, float py, float open) {
        Draw2DProcessor draw = Delta.h().d().i();
        AccountConstructor selected = Delta.h().d().h().a();
        Fonts.e.a(context, GradientUtil.a("\u041c\u0435\u043d\u0435\u0434\u0436\u0435\u0440 \u0410\u043a\u043a\u0430\u0443\u043d\u0442\u043e\u0432", a(open), 5.0f, 0.5f), (w - Fonts.e.a("\u041c\u0435\u043d\u0435\u0434\u0436\u0435\u0440 \u0410\u043a\u043a\u0430\u0443\u043d\u0442\u043e\u0432", 11.0f)) / 2.0f, py - 28.0f, 11.0f, 0.0f);
        String info = (selected != null ? selected.b() : "\u041d\u0435 \u0432\u044b\u0431\u0440\u0430\u043d") + "  |  " + a().size() + " \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u043e\u0432";
        Fonts.b.a(context, info, (w - Fonts.b.a(info, 7.0f)) / 2.0f, py - 13.5f, 7.0f, ColorUtil.a(255, 255, 255, (int) (255.0f * open)));
        draw.b(context, px, py, 190.0f, 250.0f, 8.0f, ColorUtil.a(11, 11, 13, InterfaceC0020Opcode.bN), open);
        draw.a(context, px, py, 190.0f, 250.0f, 8.0f, 0.5f, ColorUtil.a(255, 255, 255, (int) (15.0f * open)));
    }

    private void a(GuiGraphicsExtractor context, int w, int h, float scale, float px, float py, int mx, int my, float open) {
        Draw2DProcessor draw = Delta.h().d().i();
        AccountConstructor selected = Delta.h().d().h().a();
        int accent = a(open);
        float listTop = py + 10.0f;
        float listBottom = py + 218.0f;
        float listHeight = listBottom - listTop;
        float overflow = Math.min(0.0f, listHeight - (this.d.size() * 29.0f));
        float offset = this.b.a(overflow, 0.0f, 0.5f);
        boolean drag = this.f != null && this.i;
        float baseY = listTop + offset;
        float dragSlot = (my - this.h) - baseY;
        List<a> visual = (List) this.d.stream().sorted(Comparator.comparing(a2 -> !a2.b.d())).collect(Collectors.toCollection(ArrayList::new));
        if (drag) {
            a(visual, dragSlot);
        }
        this.e = null;
        float selectedSlot = -1.0f;
        int index = 0;
        float scX = (w / 2.0f) + ((px - (w / 2.0f)) * scale);
        float scY = (h / 2.0f) + (((listTop - 2.0f) - (h / 2.0f)) * scale);
        float scW = 190.0f * scale;
        float scH = (listHeight + 1.0f) * scale;
        context.enableScissor((int) scX, (int) scY, (int) (scX + scW), (int) (scY + scH));
        for (a account : visual) {
            float targetSlot = account.g ? account.d : index * 29.0f;
            if (account != this.f || !drag) {
                account.a(context, draw, accent, px + 1.0f, baseY, targetSlot, listTop, listBottom, mx, my, open);
            }
            if (account.b == selected) {
                selectedSlot = targetSlot;
            }
            if (!account.g) {
                index++;
            }
        }
        if (drag) {
            this.f.d = dragSlot;
            this.f.a(context, draw, accent, px + 1.0f, baseY, dragSlot, listTop, listBottom, mx, my, open);
        }
        context.disableScissor();
        this.d.removeIf(v0 -> v0.a());
        if (selected != this.g) {
            this.g = selected;
            if (selectedSlot >= 0.0f) {
                float top = selectedSlot + offset;
                float desired = top < 0.0f ? -selectedSlot : top + 29.0f > listHeight ? (listHeight - 29.0f) - selectedSlot : offset;
                this.b.a(MathUtil.b(desired, overflow, 0.0f) - offset);
            }
        }
        float content = Math.max(listHeight, this.d.size() * 29.0f);
        float thumb = (listHeight * listHeight) / content;
        draw.a(context, px + 182.5f, listTop, 1.5f, listHeight, 0.75f, ColorUtil.a(255, 255, 255, (int) (20.0f * open)));
        draw.a(context, px + 182.5f, listTop - ((offset / Math.max(1.0f, content - listHeight)) * (listHeight - thumb)), 1.5f, thumb, 0.75f, accent);
    }

    private void a(GuiGraphicsExtractor context, float px, float py, int mx, int my, float delta, float open) {
        Draw2DProcessor draw = Delta.h().d().i();
        a(open);
        int white = ColorUtil.a(222, 222, 222, (int) (222.0f * open));
        float fieldY = py + 224.0f;
        float randomWidth = Fonts.a.a("H", 8.0f) + 3.0f + Fonts.d.a("\u0421\u043b\u0443\u0447\u0430\u0439\u043d\u044b\u0439", 7.0f) + 14.0f;
        float fieldWidth = 146.0f - randomWidth;
        float right = px + 11.0f + fieldWidth;
        this.c.a(new Vector2f(px + 7.0f, fieldY));
        this.c.b(new Vector2f(fieldWidth, 18.0f));
        this.c.a(context, mx, my, delta, open);
        a(context, draw, right + 0.5f, fieldY, 18.0f, 18.0f, 5.0f, null, "m", ColorUtil.a(255, 255, 255, 10), white, open, mx, my);
        a(context, draw, right + 27.0f, fieldY, randomWidth, 18.0f, 6.0f, "\u0421\u043b\u0443\u0447\u0430\u0439\u043d\u044b\u0439", "", ColorUtil.a(255, 255, 255, 10), white, open, mx, my);
        a(context, draw, px + 15.0f, py + 257.0f, 160.0f, 20.0f, 6.0f, "\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0432\u0441\u0435 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u044b", null, ColorUtil.a(220, 80, 80, 20), ColorUtil.a(220, 80, 80, (int) (255.0f * open)), open, mx, my);
    }

    private int a(float open) {
        int rgba = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        return (rgba & 16777215) | (((int) (((rgba >>> 24) & 255) * open)) << 24);
    }

    private void a(GuiGraphicsExtractor context, Draw2DProcessor draw, float x, float y, float width, float height, float radius, String text, String icon, int background, int content, float open, int mx, int my) {
        float fA;
        boolean hover = MathUtil.a(mx, my, x, y, width, height);
        draw.a(context, x, y, width, height, radius, background);
        draw.a(context, x, y, width, height, radius, 0.25f, ColorUtil.a(255, 255, 255, (int) ((hover ? 25 : 12) * open)));
        if (icon != null) {
            fA = Fonts.a.a(icon, 8.0f) + (text != null ? 3.0f : 0.0f);
        } else {
            fA = 0.0f;
        }
        float iconWidth = fA;
        float startX = x + (((width - iconWidth) - (text != null ? Fonts.d.a(text, 7.0f) : 0.0f)) / 2.0f);
        if (icon != null) {
            Fonts.a.a(context, icon, startX, y + ((height - 10.0f) / 2.0f), 10.0f, content);
        }
        if (text != null) {
            Fonts.d.a(context, text, startX + 2.0f, y + ((height - 8.5f) / 2.0f), 7.0f, content);
        }
    }

    private List<AccountConstructor> a() {
        return Delta.h().d().h().e();
    }

    private void a(AccountConstructor account) {
        Delta.h().d().h().a(account);
        saveAccounts();
    }

    private void saveAccounts() {
        Delta.h().d().h().save();
    }

    private String b() {
        StringBuilder name = new StringBuilder();
        int syllables = 2 + ((int) (Math.random() * 3.0d));
        for (int i = 0; i < syllables; i++) {
            char c = "bcdfghjklmnpqrstvwz".charAt((int) (Math.random() * ((double) "bcdfghjklmnpqrstvwz".length())));
            name.append(c);
            if (Math.random() < 0.11999994994142604d) {
                name.append(c);
            }
            char v = "aeiouy".charAt((int) (Math.random() * ((double) "aeiouy".length())));
            name.append(v);
            if (Math.random() < 0.1000000000145568d) {
                name.append(v);
            }
        }
        if (Math.random() < 0.30000018030598535d) {
            name.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        }
        if (Math.random() < 0.15000000097794938d) {
            name.append('_');
        }
        if (Math.random() < 0.25d) {
            int digits = 1 + ((int) (Math.random() * 3.0d));
            for (int i2 = 0; i2 < digits; i2++) {
                name.append((char) (48 + ((int) (Math.random() * 10.0d))));
            }
        }
        if (name.length() < 5) {
            return b();
        }
        return name.length() > 16 ? name.substring(0, 16) : name.toString();
    }

    private void a(String raw) {
        String name = raw.trim();
        if (name.isEmpty() || a().stream().anyMatch(other -> other.b().equalsIgnoreCase(name))) {
            return;
        }
        AccountConstructor account = new AccountConstructor(name);
        a(account);
        a().add(account);
        this.d.add(new a(account));
        saveAccounts();
        this.c.a();
    }

    private void a(a account) {
        account.g = true;
        boolean wasSelected = account.b.c();
        a().remove(account.b);
        if (wasSelected) {
            a(a().stream().findFirst().orElse(null));
        }
        saveAccounts();
    }

    private void a(List<a> visual, float draggedY) {
        int from = visual.indexOf(this.f);
        int favorites = (int) visual.stream().filter(account -> account.b.d()).count();
        int lo = this.f.b.d() ? 0 : favorites;
        int hi = this.f.b.d() ? favorites - 1 : visual.size() - 1;
        int to = Math.max(lo, Math.min(hi, Math.round(draggedY / 29.0f)));
        if (from < 0 || from == to) {
            return;
        }
        visual.remove(from);
        visual.add(to, this.f);
        this.d.clear();
        this.d.addAll(visual);
    }

    private void c() {
        List<AccountConstructor> list = a();
        Stream<AccountConstructor> map = this.d.stream().map(account -> account.b);
        Objects.requireNonNull(list);
        List<AccountConstructor> ordered = map.filter(v1 -> list.contains(v1)).toList();
        list.clear();
        list.addAll(ordered);
        saveAccounts();
    }

    private void d() {
        String clip = GLFW.glfwGetClipboardString(aM_.getWindow().handle());
        if (clip == null) {
            return;
        }
        String name = clip.replaceAll("[^a-zA-Z0-9_]", "");
        a(name.substring(0, Math.min(16, name.length())));
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    class a {
        final AccountConstructor b;
        private final float[] c = new float[4];
        float d = Float.NaN;
        float e;
        boolean f;
        boolean g;

        a(AccountConstructor data) {
            this.b = data;
        }

        private boolean a() {
            return this.g && this.c[3] < 0.01f;
        }

        void a(GuiGraphicsExtractor context, Draw2DProcessor draw, int accent, float px, float baseY, float targetSlot, float listTop, float listBottom, int mx, int my, float open) {
            this.d = Float.isNaN(this.d) ? targetSlot : MathUtil.c(this.d, targetSlot, 1.4f);
            this.e = baseY + this.d;
            boolean over = !this.g && ((float) my) > listTop && ((float) my) < listBottom && MathUtil.a((double) mx, (double) my, px + 7.0f, this.e, 168.0f, 25.0f);
            this.f = over && ((float) mx) > px + 156.0f;
            if (over) {
                AltScreen.this.e = this;
            }
            float[] target = new float[4];
            target[0] = over ? 1.0f : 0.0f;
            target[1] = this.b.c() ? 1.0f : 0.0f;
            target[2] = this.b.d() ? 1.0f : 0.0f;
            target[3] = this.g ? 0.0f : 1.0f;
            for (int idx = 0; idx < 4; idx++) {
                this.c[idx] = MathUtil.c(this.c[idx], target[idx], 1.5f);
            }
            float hover = this.c[0];
            float select = this.c[1];
            float fav = this.c[2];
            float a = open * this.c[3];
            if (this.e + 25.0f < listTop - 2.0f || this.e > listBottom + 2.0f) {
                return;
            }
            if (select > 0.01f) {
                draw.a(context, px + 7.0f, this.e, 168.0f, 25.0f, 6.0f, ColorUtil.a(accent, 0.1f * select * a));
            } else if (hover > 0.01f) {
                draw.a(context, px + 7.0f, this.e, 168.0f, 25.0f, 6.0f, ColorUtil.a(255, 255, 255, (int) (6.0f * hover * a)));
            }
            draw.a(context, px + 7.0f, this.e, 168.0f, 25.0f, 6.0f, 0.5f, ColorUtil.a(ColorUtil.a(255, 255, 255, (int) (8.0f * a)), ColorUtil.a(255, 205, 60, (int) (30.0f * a)), fav));
            UUID uuid = this.b.e();
            String skinUrl = "https://mc-heads.net/avatar/" + uuid + "/32";
            draw.a(context, px + 11.5f, this.e + 4.0f, 16.5f, 16.5f, 3.0f, ColorUtil.a(11, 11, 13, (int) (255.0f * a)));
            Fonts.d.a(context, this.b.b(), px + 34.0f, this.e + 7.5f, 8.0f, ColorUtil.a(255, 255, 255, (int) (255.0f * a)));
            if (fav > 0.01f || hover > 0.01f) {
                Fonts.a.a(context, "\\", px + 159.0f, this.e + 8.0f, 9.0f, ColorUtil.a(ColorUtil.a(255, 255, 255, (int) ((this.f ? InterfaceC0020Opcode.bW : 45) * hover * a)), ColorUtil.a(255, 205, 60, (int) (255.0f * a)), fav));
            }
        }
    }
}



