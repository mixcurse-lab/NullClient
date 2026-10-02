package platform.client.ui.screen;

import static platform.api.module.Interface.aM_;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

import platform.api.module.Interface;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.system.configs.ThemeInfo;
import platform.api.system.interfaces.Action;
import platform.client.Delta;
import platform.client.utils.other.Marker_2;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.render.AnimationUtil;
import platform.client.utils.render.ColorUtil;
import platform.client.utils.render.EasingList;
import platform.client.utils.render.Fonts;
import platform.client.utils.render.pipeline.DeltaRenderUtil;
import lombok.Generated;


public class RadialScreen implements Interface {
    private Segment[] segments;
    private final float innerRadius;
    private final float outerRadius;
    private int selected = -1;

    public RadialScreen(int count, float inner, float outer) {
        this.segments = new Segment[count];
        this.innerRadius = inner;
        this.outerRadius = outer;
    }

    @Generated
    public int b() {
        return this.selected;
    }

    @Generated
    public void e(int selectedSlot) {
        this.selected = selectedSlot;
    }


    public int a() {
        return this.segments.length;
    }


    public void a(int slot) {
        Segment[] next = new Segment[this.segments.length - 1];
        System.arraycopy(this.segments, 0, next, 0, slot);
        System.arraycopy(this.segments, slot + 1, next, slot, (this.segments.length - slot) - 1);
        this.segments = next;
        if (this.selected >= this.segments.length) {
            this.selected = -1;
        }
    }


    public void b(int count) {
        Segment[] next = new Segment[count];
        System.arraycopy(this.segments, 0, next, 0, Math.min(this.segments.length, count));
        this.segments = next;
    }

    public void a(int index, ItemStack icon, Action action, boolean editable) {
        this.segments[index] = new Segment(icon, null, action, editable);
    }

    public void a(int index, ItemStack icon, String label, Action action, boolean editable) {
        this.segments[index] = new Segment(icon, label, action, editable);
    }


    public boolean a(double mouseX, double mouseY, int button, Vector2f center) {
        int slot = a(mouseX, mouseY, center);
        if (slot < 0) {
            return false;
        }
        Segment segment = this.segments[slot];
        if (segment != null) {
            if (button == 1 && segment.editable()) {
                if (!segment.icon().isEmpty()) {
                    segment.icon(ItemStack.EMPTY);
                    return true;
                }

                if (this.segments.length > 1) {
                    a(slot);
                    this.selected = -1;
                    return true;
                }
                return true;
            }
            if (button == 0 && segment.action() != null) {
                segment.action().execute();
            }
        }
        this.selected = slot;
        return true;
    }

    private static final int SLICE_STEPS_DEG = 3;


    private void slice(GuiGraphicsExtractor context, float cx, float cy,
                       float innerR, float outerR, double start, double end, int color, float inflate) {
        Matrix3x2fStack pose = context.pose();
        double span = end - start;
        int steps = Math.max(8, (int) Math.ceil(Math.toDegrees(span) / SLICE_STEPS_DEG));
        double stepAngle = span / steps;
        int bands = 4;
        float bandHeight = (outerR - innerR) / (float) bands;
        for (int band = 0; band < bands; band++) {
            float r0 = innerR + (bandHeight * (float) band);
            float rMid = r0 + (bandHeight * 0.5f);

            float halfChord = ((float) (rMid * Math.sin(stepAngle * 0.5d))) + 0.15f + inflate;
            pose.pushMatrix();
            pose.translate(cx, cy);
            pose.rotate((float) (start + (stepAngle * 0.5d)));
            for (int i = 0; i < steps; i++) {
                DeltaRenderUtil.queueRect(context,
                        r0 - inflate, -halfChord,
                        r0 + bandHeight + inflate, halfChord,
                        color, 0.0f, 0.0f, 0, null);
                pose.rotate((float) stepAngle);
            }
            pose.popMatrix();
        }
    }

    public void a(GuiGraphicsExtractor context, int mouseX, int mouseY, Vector2f center) {
        float cx = center.x;
        float cy = center.y;
        float iconRadius = (this.innerRadius + this.outerRadius) * 0.5f;
        int count = this.segments.length;
        float tickDelta = aM_.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        boolean assistantOpen = aM_.gui.screen() instanceof AssistantScreen;
        for (int slot = 0; slot < count; slot++) {
            double startAngle = (-1.5707963285412472d) + ((6.2831872368967865d / ((double) count)) * ((double) slot));
            double endAngle = startAngle + (6.2831872368967865d / ((double) count));
            double gapStart = startAngle + (((endAngle - startAngle) * 0.010000000036845655d) / 2.0d);
            double gapEnd = endAngle - (((endAngle - startAngle) * 0.010000000036845655d) / 2.0d);
            boolean isSelected = slot == this.selected && !assistantOpen;
            Segment segment = this.segments[slot];
            if (segment == null) {
                continue;
            }
            segment.anim().a(0.0f, 1.0f, 0.3f, EasingList.g, tickDelta);
            segment.anim().a(slot == a((double) mouseX, (double) mouseY, center));
            int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
            int baseColor = ColorUtil.a(Delta.h().d().o().a(ThemeInfo.BACKGROUND_GUI).a(), 150);
            int hoverColor = ColorUtil.a(baseColor, primary, segment.anim().c());
            int amount = (!assistantOpen || segment.icon().isEmpty()) ? -1 : InventoryUtil.c(segment.icon(), false);
            int fillColor = ((isSelected || amount == 0) && !segment.icon().isEmpty()) ? ColorUtil.a(255, 128, 128, 80) : hoverColor;
            double midAngle = (startAngle + endAngle) * 0.5d;
            float lift = segment.anim().c() * 4.0f;
            float offsetX = ((float) Math.cos(midAngle)) * lift;
            float offsetY = ((float) Math.sin(midAngle)) * lift;

            int outlineColor = ColorUtil.a(ColorUtil.b(ColorUtil.a(fillColor, 255), 0.45f), 200);
            slice(context, cx + offsetX, cy + offsetY, this.innerRadius, this.outerRadius, gapStart, gapEnd, outlineColor, 0.75f);
            slice(context, cx + offsetX, cy + offsetY, this.innerRadius, this.outerRadius, gapStart, gapEnd, fillColor, 0.0f);
            float iconX = cx + offsetX + (((float) Math.cos(midAngle)) * iconRadius);
            float iconY = cy + offsetY + (((float) Math.sin(midAngle)) * iconRadius);
            if (!segment.icon().isEmpty()) {
                Delta.h().d().j().a(context, segment.icon(), iconX - 8.0f, iconY - 8.0f, 0, 1.0f, 1.0f, false);
                if (assistantOpen && amount > 0) {
                    String label = String.valueOf(amount);
                    Fonts.e.a(context, label, (iconX + 8.0f) - Fonts.e.a(label, 10.0f), (iconY + 10.0f) - Fonts.e.a(10.0f), 10.0f, ColorUtil.a(255, 255, 255, amount > 0 ? 235 : InterfaceC0020Opcode.al));
                }
            } else {
                Fonts.e.a(context, Marker_2.b, iconX - (Fonts.e.a(Marker_2.b, 14.0f) * 0.5f), iconY - (Fonts.e.a(14.0f) * 0.5f), 14.0f, ColorUtil.a(255, 255, 255, 255));
            }
        }

        int hoveredSlot = a((double) mouseX, (double) mouseY, center);
        float baseY = cy + this.outerRadius + 8.0f;
        boolean hasSegment = hoveredSlot >= 0 && this.segments[hoveredSlot] != null && !this.segments[hoveredSlot].icon().isEmpty();
        if (hasSegment) {
            Segment hovered = this.segments[hoveredSlot];
            String name = hovered.label() != null ? hovered.label() : hovered.icon().getHoverName().getString();
            Fonts.e.a(context, name, cx - (Fonts.e.a(name, 10.0f) * 0.5f), baseY, 10.0f, ColorUtil.a(255, 255, 255, 255));
        }
        if (assistantOpen) {
            String delHint = "ПКМ – удалить слот";
            String addHint = "Y – добавить слот";
            float hintY = hasSegment ? (baseY + Fonts.d.a(10.0f) + 3.0f) : baseY;
            Fonts.d.a(context, delHint, cx - (Fonts.d.a(delHint, 7.0f) * 0.5f), hintY, 7.0f, ColorUtil.a(255, 255, 255, InterfaceC0020Opcode.cG));
            Fonts.d.a(context, addHint, cx - (Fonts.d.a(addHint, 7.0f) * 0.5f), hintY + Fonts.d.a(10.0f), 7.0f, ColorUtil.a(255, 255, 255, InterfaceC0020Opcode.cG));
        }
    }


    public ItemStack c(int slot) {
        Segment segment = this.segments[Math.floorMod(slot, this.segments.length)];
        return segment != null ? segment.icon() : ItemStack.EMPTY;
    }


    public void a(int slot, Action action) {
        Segment segment = this.segments[Math.floorMod(slot, this.segments.length)];
        if (segment != null) {
            segment.action(action);
        }
    }


    public Action d(int slot) {
        Segment segment = this.segments[Math.floorMod(slot, this.segments.length)];
        return segment != null ? segment.action() : null;
    }


    public void a(int slot, ItemStack stack) {
        Segment segment = this.segments[Math.floorMod(slot, this.segments.length)];
        if (segment != null) {
            segment.icon(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        }
    }


    public void a(int from, int to) {
        Segment first = this.segments[Math.floorMod(from, this.segments.length)];
        Segment second = this.segments[Math.floorMod(to, this.segments.length)];
        if (first == null || second == null || first == second) {
            return;
        }
        ItemStack icon = first.icon();
        String label = first.label();
        first.icon(second.icon());
        first.label(second.label());
        second.icon(icon);
        second.label(label);
    }


    public int a(double mouseX, double mouseY, Vector2f center) {
        double distance = Math.hypot(mouseX - ((double) center.x), mouseY - ((double) center.y));
        if (distance < ((double) this.innerRadius) * 0.25d) {
            return -1;
        }
        double angle = Math.atan2(mouseY - ((double) center.y), mouseX - ((double) center.x)) + 1.5707964162115484d;
        if (angle < 0.0d) {
            angle += 6.2831872368967865d;
        }
        return Math.min((int) (angle / (6.2831872368967865d / ((double) this.segments.length))), this.segments.length - 1);
    }


    private static final class Segment {
        private final AnimationUtil anim = new AnimationUtil();
        private ItemStack icon;
        private String label;
        private final boolean editable;
        private Action action;

        Segment(ItemStack icon, String label, Action action, boolean editable) {
            this.icon = icon;
            this.label = label;
            this.action = action;
            this.editable = editable;
        }

        AnimationUtil anim() {
            return this.anim;
        }

        ItemStack icon() {
            return this.icon;
        }

        void icon(ItemStack value) {
            this.icon = value;
        }

        String label() {
            return this.label;
        }

        void label(String value) {
            this.label = value;
        }

        boolean editable() {
            return this.editable;
        }

        Action action() {
            return this.action;
        }

        void action(Action value) {
            this.action = value;
        }
    }
}
