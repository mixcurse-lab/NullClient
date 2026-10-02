package platform.client.ui.screen;

import platform.api.annotation.Compile;
import platform.api.module.Category;
import platform.api.module.Module;
import platform.client.Delta;
import platform.client.ui.element.Element_2;
import platform.client.ui.element.TextField;
import platform.client.utils.math.MathUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.DeltaBlurProcessor;
import platform.client.utils.render.Draw2DProcessor;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ScaleUtil;
import platform.client.utils.render.pipeline.DeltaRenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static platform.api.module.Interface.aM_;

public class GUIScreen extends Screen {
    private static final float PANEL_WIDTH = 150.0f;
    private static final float PANEL_HEIGHT = 295.0f;
    private static final float PANEL_GAP = 8.0f;

    private final TextField search;
    private final List<GUIPanel> panels;
    private String hoveredDescription;

    public GUIScreen(Component title) {
        super(title);
        this.search = new TextField(TextField.a.GUI);
        this.search.a("Поиск");
        this.panels = new ArrayList<>();
        for (Category category : Category.values()) this.panels.add(new GUIPanel(category));
    }

    @Compile
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        DeltaBlurProcessor.setStrength(1.0f);
        DeltaRenderUtil.setShadowStrength(1.0f);
        DeltaRenderUtil.beginFrame();
        ScaleUtil.a(context, 2);
        super.extractRenderState(context, mouseX, mouseY, delta);

        float totalWidth = (PANEL_WIDTH * this.panels.size())
                + (PANEL_GAP * (this.panels.size() - 1));
        float screenWidth = aM_.getWindow().getGuiScaledWidth();
        float screenHeight = aM_.getWindow().getGuiScaledHeight();
        float panelX = (screenWidth - totalWidth) * 0.5f;
        float panelY = (screenHeight - PANEL_HEIGHT) * 0.5f - 14.0f;
        int scaledMouseX = (int) MathUtil.scale(mouseX, 2);
        int scaledMouseY = (int) MathUtil.scale(mouseY, 2);
        String query = this.search.g().toString().trim().toLowerCase();

        Draw2DProcessor draw = Delta.h().d().i();
        draw.a(context, 0.0f, 0.0f, screenWidth, screenHeight,
                ColorUtil.a(0xFF000000, 0.28f));

        for (int i = 0; i < this.panels.size(); i++) {
            GUIPanel panel = this.panels.get(i);
            panel.f().set(panelX + i * (PANEL_WIDTH + PANEL_GAP), panelY,
                    PANEL_WIDTH, PANEL_HEIGHT);
            List<Module> modules = new ArrayList<>();
            for (Module module : Delta.h().d().t().e()) {
                if (module.l() == panel.c()
                        && module.j().toLowerCase().contains(query)) {
                    modules.add(module);
                }
            }
            modules.sort(Comparator.comparing(Module::j, String.CASE_INSENSITIVE_ORDER));
            panel.a(modules);
            panel.a(context, scaledMouseX, scaledMouseY, delta);
        }

        float searchWidth = 300.0f;
        float searchY = panelY + PANEL_HEIGHT + 9.0f;
        this.search.b(new Vector2f(searchWidth, 24.0f));
        this.search.a(new Vector2f((screenWidth - searchWidth) * 0.5f, searchY));
        this.search.a(context, scaledMouseX, scaledMouseY, delta, 1.0f);
        renderDescription(context, screenWidth, panelY, delta);

        // Render color picker overlays outside scissor so they are not clipped
        for (GUIPanel panel : this.panels) {
            for (Module module : panel.d()) {
                if (!module.o()) continue;
                for (Element_2<?> element : module.d()) {
                    if (element.a()) {
                        element.a(context, scaledMouseX, scaledMouseY, delta);
                    }
                }
            }
        }

        ScaleUtil.a(context);
        DeltaRenderUtil.flush(context);
    }

    private void renderDescription(GuiGraphicsExtractor context, float screenWidth,
                                  float panelY, float delta) {
        Module hovered = this.panels.stream().map(GUIPanel::e)
                .filter(module -> module != null && module.k() != null && !module.k().isEmpty())
                .findFirst().orElse(null);
        if (hovered == null) {
            this.hoveredDescription = null;
            return;
        }
        String description = hovered.k();
        if (!description.equals(this.hoveredDescription)) {
            this.hoveredDescription = description;
        }
        float size = 10.0f;
        float textWidth = Fonts.c.a(description, size);
        float y = panelY - 14.0f;
        Fonts.c.a(context, description, (screenWidth - textWidth) * 0.5f + 0.5f,
                y + 0.5f, size, ColorUtil.a(0xFF000000, 0.5f));
        Fonts.c.a(context, description, (screenWidth - textWidth) * 0.5f, y,
                size, 0xFFFFFFFF);
    }

    @Compile
    public boolean mouseClicked(MouseButtonEvent event, boolean held) {
        double mouseX = MathUtil.scale(event.x(), 2);
        double mouseY = MathUtil.scale(event.y(), 2);
        int button = event.button();
        this.search.a(mouseX, mouseY, button);
        for (GUIPanel panel : this.panels) {
            if (panel.a(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(event, held);
    }

    @Compile
    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = MathUtil.scale(event.x(), 2);
        double mouseY = MathUtil.scale(event.y(), 2);
        int button = event.button();
        for (GUIPanel panel : this.panels) {
            if (panel.b(mouseX, mouseY, button)) return true;
        }
        return super.mouseReleased(event);
    }

    @Compile
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        double mouseX = MathUtil.scale(event.x(), 2);
        double mouseY = MathUtil.scale(event.y(), 2);
        int button = event.button();
        this.search.b(mouseX, mouseY, button);
        for (GUIPanel panel : this.panels) {
            if (panel.a(mouseX, mouseY, button,
                    MathUtil.scale(deltaX, 2), MathUtil.scale(deltaY, 2))) return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Compile
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horizontalAmount, double verticalAmount) {
        double scaledX = MathUtil.scale(mouseX, 2);
        double scaledY = MathUtil.scale(mouseY, 2);
        for (GUIPanel panel : this.panels) {
            if (panel.a(scaledX, scaledY, verticalAmount)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Compile
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        int modifiers = event.modifiers();
        if (keyCode == 70 && (modifiers & 2) != 0) {
            this.search.a(!this.search.j());
            return true;
        }
        if (this.search.j()) {
            this.search.a(keyCode, event.scancode(), modifiers);
            return true;
        }
        for (GUIPanel panel : this.panels) {
            if (panel.a(keyCode, event.scancode(), modifiers)) return true;
        }
        return super.keyPressed(event);
    }

    @Compile
    public boolean charTyped(CharacterEvent event) {
        char character = (char) event.codepoint();
        if (this.search.j()) {
            this.search.a(character, 0);
            return true;
        }
        for (GUIPanel panel : this.panels) {
            if (panel.a(character, 0)) return true;
        }
        return super.charTyped(event);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.panels.forEach(panel -> panel.b().c(0.0f));
    }

    @Override
    public void removed() {
        DeltaBlurProcessor.setStrength(1.0f);
        DeltaRenderUtil.setShadowStrength(1.0f);
        super.removed();
    }

    public List<GUIPanel> c() {
        return this.panels;
    }

    public boolean captureMouseBind(int keyCode) {
        for (GUIPanel panel : this.panels) {
            if (panel.a(keyCode, 0, 0)) return true;
        }
        return false;
    }
}
