package platform.client.ui.element;

import static platform.api.module.Interface.aM_;
import platform.api.annotation.Compile;
import platform.api.system.configs.ConfigProcessor;
import platform.api.system.configs.ModuleProcessor;
import platform.api.system.configs.ThemeInfo;
import platform.client.Delta;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.event.events.render.DrawEvent;
import platform.client.utils.lib.json.JSONArray;
import platform.client.utils.lib.json.JSONObject;
import platform.client.utils.render.AnimationUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.EasingList;
import platform.api.module.setting.BindSetting;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ColorSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.api.module.setting.Setting;
import platform.api.module.setting.SliderSetting;
import platform.api.module.setting.StringSetting;
import platform.client.utils.input.CursorUtil;
import platform.client.utils.math.MathUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;

public class DragProcessor extends ConfigProcessor<DragInfo> implements Interface {
    private DragInfo g = null;
    private final Set<Screen> f = new HashSet<>();
    private final b h = new b();
    private final b i = new b();
    private final AnimationUtil j = new AnimationUtil();
    private final float[] k = new float[128];
    private final float[] l = new float[32];
    private float lastWidth = -1.0f;
    private float lastHeight = -1.0f;

    @Override
    protected String b() {
        return "drag.json";
    }

    private boolean healed = false;
    private String lastGoodJson = null;

    @Override
    @Compile
    protected List<DragInfo> a(String json) throws Exception {
        JSONArray array = new JSONArray(json);
        float screenW = (float) aM_.getWindow().getGuiScaledWidth();
        float screenH = (float) aM_.getWindow().getGuiScaledHeight();
        List<DragInfo> broken = new ArrayList<>();
        for (int i = 0; i < array.a(); i++) {
            JSONObject obj = array.j(i);
            String name = obj.l("name");
            if (name == null) {
                continue;
            }
            for (DragInfo dragInfo : e()) {
                if (!dragInfo.j().equals(name)) {
                    continue;
                }
                float x = obj.f("x");
                float y = obj.f("y");
                float width = obj.f("width");
                float height = obj.f("height");
                if (width > 0.5f && width < 4096.0f && !Float.isNaN(width)) {
                    dragInfo.c(width);
                }
                if (height > 0.5f && height < 4096.0f && !Float.isNaN(height)) {
                    dragInfo.d(height);
                }
                float effectiveW = screenW > 0.0f ? screenW : 10000.0f;
                float effectiveH = screenH > 0.0f ? screenH : 10000.0f;
                boolean sanePos = !Float.isNaN(x) && !Float.isNaN(y)
                        && x >= -1.0f && x <= effectiveW + 1.0f
                        && y >= -1.0f && y <= effectiveH + 1.0f;
                if (!sanePos) {
                    broken.add(dragInfo);
                } else {
                    dragInfo.a(MathUtil.b(x, 0.0f, screenW > 0.0f ? Math.max(0.0f, screenW - dragInfo.f()) : x));
                    dragInfo.b(MathUtil.b(y, 0.0f, screenH > 0.0f ? Math.max(0.0f, screenH - dragInfo.g()) : y));
                }
                if (obj.m("settings")) {
                    JSONArray settings = obj.i("settings");
                    if (settings != null) {
                        for (int s = 0; s < settings.a(); s++) {
                            JSONObject setObj = settings.j(s);
                            String setName = setObj.l("name");
                            if (setName == null) {
                                continue;
                            }
                            for (Setting<?> setting : dragInfo.e().b()) {
                                if (setting.i().equalsIgnoreCase(setName)) {
                                    a(setting, setObj);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!broken.isEmpty()) {
            ModuleProcessor.layoutCentered(broken, screenW, screenH);
            this.healed = true;
        }
        this.lastGoodJson = json;
        return new ArrayList<>(e());
    }

    @Override
    @Compile
    protected String a(List<DragInfo> data) throws Exception {
        // Build a lookup of the last known-good values from the previously loaded JSON
        java.util.Map<String, JSONObject> fallback = new java.util.HashMap<>();
        if (this.lastGoodJson != null && !this.lastGoodJson.isEmpty() && !this.lastGoodJson.equals("[]")) {
            try {
                JSONArray prev = new JSONArray(this.lastGoodJson);
                for (int i = 0; i < prev.a(); i++) {
                    JSONObject o = prev.j(i);
                    String n = o.l("name");
                    if (n != null) fallback.put(n, o);
                }
            } catch (Exception ignored) {}
        }
        JSONArray array = new JSONArray();
        for (DragInfo dragInfo : data) {
            JSONObject obj = new JSONObject();
            obj.c("name", dragInfo.j());
            float x = dragInfo.c();
            float y = dragInfo.d();
            float width = dragInfo.f();
            float height = dragInfo.g();
            // If the widget hasn't been rendered yet (size still zero), use fallback values
            if (width <= 0.5f && height <= 0.5f) {
                JSONObject fb = fallback.get(dragInfo.j());
                if (fb != null) {
                    float fbX = fb.f("x");
                    float fbY = fb.f("y");
                    float fbW = fb.f("width");
                    float fbH = fb.f("height");
                    if (!Float.isNaN(fbX)) x = fbX;
                    if (!Float.isNaN(fbY)) y = fbY;
                    if (!Float.isNaN(fbW) && fbW > 0.5f) width = fbW;
                    if (!Float.isNaN(fbH) && fbH > 0.5f) height = fbH;
                }
            }
            obj.b("x", x);
            obj.b("y", y);
            obj.b("width", width);
            obj.b("height", height);
            JSONArray settings = new JSONArray();
            for (Setting<?> setting : dragInfo.e().b()) {
                JSONObject setObj = new JSONObject();
                setObj.c("name", setting.i());
                if (setting instanceof MultiModeSetting) {
                    JSONArray modesArr = new JSONArray();
                    for (BooleanSetting child : ((MultiModeSetting) setting).c()) {
                        JSONObject childObj = new JSONObject();
                        childObj.c("name", child.i());
                        childObj.b("value", child.c());
                        modesArr.a(childObj);
                    }
                    setObj.c("value", modesArr);
                } else {
                    setObj.c("value", setting.c());
                }
                settings.a(setObj);
            }
            obj.c("settings", settings);
            array.a(obj);
        }
        return array.E(2);
    }

    @SuppressWarnings("unchecked")
    private void a(Setting<?> setting, JSONObject setObj) {
        if (setting instanceof MultiModeSetting) {
            JSONArray modesArr = setObj.i("value");
            if (modesArr == null) {
                return;
            }
            for (int i = 0; i < modesArr.a(); i++) {
                JSONObject childObj = modesArr.j(i);
                BooleanSetting child = ((MultiModeSetting) setting).a(childObj.l("name"));
                if (child != null) {
                    child.a(childObj.b("value"));
                }
            }
        } else if (setting instanceof BooleanSetting) {
            ((Setting<Boolean>) setting).a(setObj.b("value"));
        } else if (setting instanceof SliderSetting slider) {
            float value = setObj.f("value");
            ((Setting<Float>) setting).a(Math.max(slider.a, Math.min(slider.b, value)));
        } else if (setting instanceof ModeSetting mode) {
            String value = setObj.l("value");
            if (value != null && mode.k().stream().anyMatch(m -> m.equalsIgnoreCase(value))) {
                ((Setting<String>) setting).a(value);
            }
        } else if (setting instanceof BindSetting || setting instanceof ColorSetting) {
            ((Setting<Integer>) setting).a(setObj.h("value"));
        } else if (setting instanceof StringSetting) {
            ((Setting<String>) setting).a(setObj.l("value"));
        }
    }

    @Generated
    public DragInfo g() { return this.g; }

    @Override
    public void setup() {
        boolean failed = false;
        try {
            super.setup();
        } catch (Exception e) {
            failed = true;
        }
        if (failed) {
            this.healed = true;
        }
        if (this.healed) {
            savePositions();
        }
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ChatScreen && this.f.add(screen)) {
                for (DragInfo info : e()) {
                    info.e().setEnabled(false);
                }
                this.register(screen);
            }
        });
    }

    private void register(Screen screen) {
        ScreenMouseEvents.allowMouseClick(screen).register((s, event) -> {
            if (event.button() == 0 && this.b(event.x(), event.y(), 0)) {
                return false;
            }
            return true;
        });
        ScreenMouseEvents.beforeMouseClick(screen).register((s, event) -> {
            if (event.button() == 1) {
                double x = event.x();
                double y = event.y();
                boolean hit = false;
                for (DragInfo info : e()) {
                    float w = Math.max(info.f(), 24.0f) + 6.0f;
                    float h = Math.max(info.g(), 12.0f) + 6.0f;
                    if (MathUtil.a(x, y, info.a() - 3.0f, info.b() - 3.0f, w, h)) {
                        info.e().setEnabled(!info.e().g());
                        hit = true;
                        return;
                    }
                }
                if (!hit) {
                    for (DragInfo info : e()) {
                        info.e().setEnabled(false);
                    }
                }
            }
        });
        ScreenMouseEvents.beforeMouseDrag(screen).register((s, event, deltaX, deltaY) -> {
            if (event.button() == 0) {
                this.a(event.x(), event.y());
            }
        });
        ScreenMouseEvents.beforeMouseRelease(screen).register((s, event) -> {
            if (event.button() == 0) {
                this.a(0);
                double x = event.x();
                double y = event.y();
                boolean onElement = false;
                for (DragInfo info : e()) {
                    if (info.e().g()) {
                        for (Element_2<?> element : info.e().c()) {
                            if (element.a() && MathUtil.a(x, y, element.d().x, element.d().y, element.d().z, element.d().w)) {
                                element.a(x, y, 0);
                                onElement = true;
                            }
                        }
                    }
                }
                if (!onElement) {
                    for (DragInfo info : e()) {
                        info.e().setEnabled(false);
                    }
                }
            }
        });
    }

    public void a(double mouseX, double mouseY) {
        if (this.g != null && this.g.k() != 2) {
            float newX = (float) mouseX + (float) this.g.h();
            float newY = (float) mouseY + (float) this.g.i();
            a(newX, newY, this.g);
        }
    }

    public void a(int mouseButton) {
        boolean wasDragging = this.g != null;
        this.g = null;
        CursorUtil.a(CursorUtil.a.DEFAULT);
        if (wasDragging) {
            savePositions();
        }
    }

    public void savePositions() {
        try {
            File dir = d();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, b());
            java.nio.file.Files.writeString(file.toPath(), a(this.e()), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Failed to save " + b() + ": " + e);
        }
    }

    public boolean b(double mouseX, double mouseY, int mouseButton) {
        for (DragInfo info : e()) {
            if (info.k() != 2 && MathUtil.a(mouseX, mouseY, info.a() - 3.0f, info.b() - 3.0f, Math.max(info.f(), 24.0f) + 6.0f, Math.max(info.g(), 12.0f) + 6.0f)) {
                CursorUtil.a(CursorUtil.a.HAND);
                info.a((double) info.a() - mouseX);
                info.b((double) info.b() - mouseY);
                this.g = info;
                return true;
            }
        }
        return false;
    }

    @EventTarget(a = 4)
    public void a(DrawEvent event) {
        if (event.b()) {
            float currentWidth = (float) aM_.getWindow().getGuiScaledWidth();
            float currentHeight = (float) aM_.getWindow().getGuiScaledHeight();
            if (this.lastWidth > 0.0f && this.lastHeight > 0.0f && (currentWidth != this.lastWidth || currentHeight != this.lastHeight)) {
                for (DragInfo info : e()) {
                    float oldX = info.c();
                    float oldY = info.d();
                    float newX = currentWidth > 0.0f ? oldX / this.lastWidth * currentWidth : oldX;
                    float newY = currentHeight > 0.0f ? oldY / this.lastHeight * currentHeight : oldY;
                    info.a(MathUtil.b(newX, 0.0f, Math.max(0.0f, currentWidth - info.f())));
                    info.b(MathUtil.b(newY, 0.0f, Math.max(0.0f, currentHeight - info.g())));
                }
                savePositions();
            }
            this.lastWidth = currentWidth;
            this.lastHeight = currentHeight;
            long now = System.currentTimeMillis();
            for (DragInfo info : e()) {
                info.a(now);
            }
            if (aM_.gui.screen() instanceof ChatScreen) {
                if (this.g == null && !this.h.a() && !this.i.a()) {
                    h();
                }
                this.h.a(this.g != null, event.g());
                this.i.a(this.g != null, event.g());
                if (this.h.a() || this.i.a()) {
                    b(event);
                }
            } else {
                for (DragInfo info : e()) {
                    info.e().setEnabled(false);
                }
                if (this.g != null) {
                    h();
                }
            }
        }
    }

    public void c(DrawEvent event) {
        if (!(aM_.gui.screen() instanceof ChatScreen)) {
            return;
        }
        this.j.a(this.g != null);
        this.j.a(0.0f, 1.0f, 0.3f, EasingList.i, event.g());
        float alpha = this.j.c();
        if (alpha <= 0.01f || e().isEmpty()) {
            return;
        }
        float scale = (float) aM_.getWindow().getGuiScale();
        int count = Math.min(32, e().size());
        for (int index = 0; index < count; index++) {
            DragInfo info = e().get(index);
            int offset = index * 4;
            if (info != null) {
                float width = info.f() * scale;
                float height = info.g() * scale;
                this.k[offset] = (info.a() + (info.f() / 2.0f)) * scale;
                this.k[offset + 1] = (info.b() + (info.g() / 2.0f)) * scale;
                this.k[offset + 2] = Math.max(34.0f * scale, (float) Math.sqrt((width * width) + (height * height)) * 0.5f);
                this.k[offset + 3] = height;
                this.l[index] = info == this.g ? 2.25f : 0.55f;
            } else {
                this.k[offset] = 0.0f;
                this.k[offset + 1] = 0.0f;
                this.k[offset + 2] = 1.0f;
                this.k[offset + 3] = 1.0f;
                this.l[index] = 0.0f;
            }
        }
        double[] cursorX = new double[1];
        double[] cursorY = new double[1];
        org.lwjgl.glfw.GLFW.glfwGetCursorPos(aM_.getWindow().handle(), cursorX, cursorY);
        int themeColor = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        int[] rgb = ColorUtil.b(themeColor);
        int dark = ((int) (rgb[0] * 0.55f) << 16) | ((int) (rgb[1] * 0.55f) << 8) | (int) (rgb[2] * 0.55f);
        GravityGrid.a().a(aM_.getWindow().getWidth(), aM_.getWindow().getHeight(), this.k, this.l, count, (float) cursorX[0], (float) cursorY[0], alpha, themeColor, dark);
    }

    private void a(float x, float y, DragInfo dragInfo) {
        int status = dragInfo.k();
        if (status == 2) {
            this.h.a(null);
            this.i.a(null);
            return;
        }
        boolean onlyY = status == 1;
        if (onlyY) {
            x = dragInfo.a();
        }
        float maxX = Math.max(0.0f, aM_.getWindow().getGuiScaledWidth() - dragInfo.f());
        float maxY = Math.max(0.0f, aM_.getWindow().getGuiScaledHeight() - dragInfo.g());
        float x2 = MathUtil.b(x, 0.0f, maxX);
        float y2 = MathUtil.b(y, 0.0f, maxY);
        float snappedX = onlyY ? x2 : a(a.X, x2, dragInfo);
        if (onlyY) {
            this.h.a(null);
        }
        float snappedY = a(a.Y, y2, dragInfo);
        dragInfo.a(MathUtil.b(snappedX, 0.0f, maxX));
        dragInfo.b(MathUtil.b(snappedY, 0.0f, maxY));
    }

    private float a(a axis, float pos, DragInfo dragInfo) {
        float size = axis.b(dragInfo);
        float[] points = {pos, pos + (size / 2.0f), pos + size};
        b guide = axis == a.X ? this.h : this.i;
        float bestDistance = 25.0f;
        Float bestGuide = null;
        float snappedPos = pos;
        Iterator<Float> it = a(axis, dragInfo).iterator();
        while (it.hasNext()) {
            float guidePos = it.next().floatValue();
            for (int point = 0; point < 3; point++) {
                float distance = Math.abs(points[point] - guidePos);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestGuide = Float.valueOf(guidePos);
                    float offset = point == 0 ? 0.0f : point == 1 ? size / 2.0f : size;
                    snappedPos = guidePos - offset;
                }
            }
        }
        if (bestGuide != null && bestDistance < 5.0f) {
            pos = snappedPos;
            guide.a(bestGuide);
        } else {
            guide.a(null);
        }
        return pos;
    }

    private List<Float> a(a axis, DragInfo currentElement) {
        List<Float> guides = new ArrayList<>();
        guides.add(Float.valueOf(0.0f));
        guides.add(Float.valueOf(axis.a() / 2.0f));
        guides.add(Float.valueOf(axis.a()));
        for (DragInfo other : e()) {
            if (other != currentElement && (other.f() != 0.0f || other.g() != 0.0f)) {
                float pos = axis.a(other);
                float size = axis.b(other);
                guides.add(Float.valueOf(pos));
                guides.add(Float.valueOf(pos + (size / 2.0f)));
                guides.add(Float.valueOf(pos + size));
            }
        }
        return guides;
    }

    private void b(DrawEvent event) {
        if (this.h.a()) {
            event.d().a(event.i(), this.h.c().floatValue() - 0.5f, 0.0f, 0.5f, aM_.getWindow().getGuiScaledHeight(), ColorUtil.a(255, 255, 255, (int) (this.h.b().c() * 200.0f)));
        }
        if (this.i.a()) {
            event.d().a(event.i(), 0.0f, this.i.c().floatValue() - 0.5f, aM_.getWindow().getGuiScaledWidth(), 0.5f, ColorUtil.a(255, 255, 255, (int) (this.i.b().c() * 200.0f)));
        }
    }

    private void h() {
        CursorUtil.a(CursorUtil.a.DEFAULT);
        this.g = null;
        this.i.a(null);
        this.h.a(null);
    }

    static class b {
        private final AnimationUtil a = new AnimationUtil();
        private Float b = null;
        private boolean c = false;

        b() {
        }

        @Generated
        public AnimationUtil b() {
            return this.a;
        }

        @Generated
        public Float c() {
            return this.b;
        }

        @Generated
        public boolean d() {
            return this.c;
        }

        void a(Float newPosition) {
            if (newPosition == null) {
                this.c = false;
            } else if (!newPosition.equals(this.b)) {
                this.b = newPosition;
                this.c = true;
            }
        }

        void a(boolean active, float tickDelta) {
            if (this.b != null) {
                boolean should = active && this.c;
                this.a.a(active && this.c);
                this.a.a(0.0f, 1.0f, 0.3f, EasingList.i, tickDelta);
                if (!should && this.a.c() <= 0.0f) {
                    this.b = null;
                }
            }
        }

        boolean a() {
            return this.b != null && this.a.c() > 0.0f;
        }
    }

    enum a {
        X,
        Y;

        float a() {
            return this == X ? aM_.getWindow().getGuiScaledWidth() : aM_.getWindow().getGuiScaledHeight();
        }

        float a(DragInfo info) {
            return this == X ? info.a() : info.b();
        }

        float b(DragInfo info) {
            return this == X ? info.f() : info.g();
        }
    }
}


