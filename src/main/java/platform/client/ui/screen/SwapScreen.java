package platform.client.ui.screen;

import static platform.api.module.Interface.aM_;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2f;

import platform.api.annotation.Compile;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.interfaces.Action;
import platform.client.Delta;
import platform.client.utils.math.MathUtil;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.DeltaBlurProcessor;
import platform.client.utils.render.Draw2DProcessor;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.ScaleUtil;
import platform.client.utils.render.pipeline.DeltaRenderUtil;


public class SwapScreen extends Screen {
    public record Choice(String name, ItemStack icon) {}

    private static final List<Choice> CHOICES = List.of(
            new Choice("Сфера", new ItemStack(net.minecraft.world.item.Items.PLAYER_HEAD)),
            new Choice("Тотем", new ItemStack(net.minecraft.world.item.Items.TOTEM_OF_UNDYING)),
            new Choice("Золотое яблоко", new ItemStack(net.minecraft.world.item.Items.GOLDEN_APPLE)),
            new Choice("Щит", new ItemStack(net.minecraft.world.item.Items.SHIELD))
    );

    private final RadialScreen b;
    private int pickerSlot = -1;
    private float pickerX;
    private float pickerY;
    private static final float PICKER_WIDTH = 120.0f;
    private static final float PICKER_ROW = 18.0f;

    public SwapScreen(Component title) {
        super(title);
        this.b = new RadialScreen(3, 75.0f, 101.25f);
        for (int i = 0; i < 3; i++) {
            this.b.a(i, ItemStack.EMPTY, c(i), true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void extractBlurredBackground(GuiGraphicsExtractor context) {
    }

    private Vector2f center() {
        return new Vector2f(aM_.getWindow().getGuiScaledWidth() * 0.5f, aM_.getWindow().getGuiScaledHeight() * 0.5f);
    }


    private Action c(int slot) {
        return () -> {
            ItemStack stack = this.b.c(slot);
            if (stack.isEmpty()) {
                this.pickerSlot = slot;
                return;
            }
            swapToOffhand(stack);
            aM_.player.closeContainer();
        };
    }

    private void swapToOffhand(ItemStack stack) {
        int from = InventoryUtil.a(stack, false);
        if (from == -1) {
            from = InventoryUtil.c(stack.getItem());
        }
        if (from != -1) {
            Delta.h().d().v().a().a(from, 40, 1);
        }
    }

    @Compile
    public boolean mouseClicked(MouseButtonEvent event, boolean held) {
        double mouseX = MathUtil.scale(event.x(), 2);
        double mouseY = MathUtil.scale(event.y(), 2);
        int button = event.button();

        if (this.pickerSlot != -1 && button == 0) {
            int index = pickerIndexAt((float) mouseX, (float) mouseY);
            if (index != -1) {
                this.b.a(this.pickerSlot, CHOICES.get(index).icon());
            }
            this.pickerSlot = -1;
            return true;
        }
        if (button != 0) {
            this.pickerSlot = -1;
        }
        int before = this.b.a();
        boolean handled = this.b.a(mouseX, mouseY, button, center());
        if (this.b.a() != before) {
            for (int i = 0; i < this.b.a(); i++) {
                this.b.a(i, c(i));
            }
        }
        return handled ? true : super.mouseClicked(event, held);
    }

    @Compile
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256 && this.pickerSlot != -1) {
            this.pickerSlot = -1;
            return true;
        }
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
        float hintY = center.y + 113.0f;
        String delHint = "ПКМ - очистить слот";
        String addHint = "Y - добавить слот";
        Fonts.d.a(context, delHint, center.x - (Fonts.d.a(delHint, 7.0f) * 0.5f), hintY, 7.0f, ColorUtil.a(255, 255, 255, 120));
        Fonts.d.a(context, addHint, center.x - (Fonts.d.a(addHint, 7.0f) * 0.5f), hintY + Fonts.d.a(10.0f), 7.0f, ColorUtil.a(255, 255, 255, 120));
        if (this.pickerSlot != -1) {
            this.pickerX = ((float) mx) + 10.0f;
            this.pickerY = ((float) my) - ((CHOICES.size() * PICKER_ROW) * 0.5f);
            drawPicker(context, (float) mx, (float) my);
        }
        ScaleUtil.a(context);
        DeltaRenderUtil.flush(context);
    }

    private int pickerIndexAt(float mouseX, float mouseY) {
        if (mouseX < this.pickerX || mouseX >= this.pickerX + PICKER_WIDTH) return -1;
        int index = (int) ((mouseY - this.pickerY) / PICKER_ROW);
        return (index >= 0 && index < CHOICES.size()) ? index : -1;
    }


    private void drawPicker(GuiGraphicsExtractor context, float mouseX, float mouseY) {
        Draw2DProcessor draw = Delta.h().d().i();
        float height = CHOICES.size() * PICKER_ROW;
        draw.a(context, this.pickerX, this.pickerY, PICKER_WIDTH, height, 8.0f,
                Delta.h().d().o().a(ThemeInfo.BACKGROUND_GUI).a(), 1.0f,
                Delta.h().d().o().a(ThemeInfo.BACKGROUND_GUI).a(), 11.0f);
        draw.a(context, this.pickerX, this.pickerY, PICKER_WIDTH, height, 8.0f, 0.5f, ColorUtil.a(255, 255, 255, 60));
        for (int index = 0; index < CHOICES.size(); index++) {
            Choice choice = CHOICES.get(index);
            float rowY = this.pickerY + (index * PICKER_ROW);
            boolean hovered = pickerIndexAt(mouseX, mouseY) == index;
            if (hovered) {
                draw.a(context, this.pickerX + 2.0f, rowY + 1.0f, PICKER_WIDTH - 4.0f, 16.0f, 6.0f, ColorUtil.a(255, 255, 255, 12));
            }
            Delta.h().d().j().a(context, choice.icon(), this.pickerX + 5.0f, (rowY + 9.0f) - 6.0f, 0, 1.0f, 0.75f, false);
            Fonts.e.a(context, choice.name(), this.pickerX + 5.0f + 12.0f + 4.0f, (rowY + 9.0f) - (Fonts.e.a(6.5f) * 0.5f), 6.5f, hovered ? ColorUtil.a(255, 255, 255, 220) : ColorUtil.a(255, 255, 255, 140));
        }
    }
}
