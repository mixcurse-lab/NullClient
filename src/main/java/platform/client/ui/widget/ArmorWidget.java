package platform.client.ui.widget;

import static platform.api.module.Interface.aM_;

import platform.client.Delta;
import platform.api.module.InterfaceC0020Opcode;
import platform.api.event.events.render.DrawEvent;
import platform.client.utils.render.ScaleUtil;
import platform.client.ui.element.DragInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class ArmorWidget extends Widget implements platform.api.module.Interface {
    public ArmorWidget() {
        super(new DragInfo("Броня", 0.0f, 0.0f, 0.0f, 0.0f));
        j().a(this);
    }

    @Override
    public void a(DrawEvent event) {
        GuiGraphicsExtractor context = event.i();
        if (context == null) {
            super.a(event);
            return;
        }
        if (event.b() && aM_.player != null && !aM_.player.isSpectator()) {
            EquipmentSlot[] armorSlots = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
            int count = 0;
            for (EquipmentSlot slot : armorSlots) {
                if (!aM_.player.getItemBySlot(slot).isEmpty()) count++;
            }
            if (count > 0) {
                ScaleUtil.b(context);
                context.pose().pushMatrix();
                context.pose().translate(0.0f, (-16.0f) * Delta.h().d().t().Q().s().c());
                int startX = ((aM_.getWindow().getGuiScaledWidth() / 2) - 91) + 182 + 4;
                int startY = aM_.getWindow().getGuiScaledHeight() - 22;
                int epta = startX + ((aM_.player.getMainArm() != HumanoidArm.LEFT || aM_.player.getOffhandItem().isEmpty()) ? 0 : 30);

                context.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath("delta", "textures/armor_hotbar.png"), epta, startY, 0.0f, 0.0f, (count * 20) + 2, 22, 82, 22);
                int index = 0;
                for (EquipmentSlot slot : armorSlots) {
                    ItemStack stack = aM_.player.getItemBySlot(slot);
                    if (!stack.isEmpty()) {
                        int x = epta + 3 + (index * 20);
                        int y = startY + 3;
                        context.item(stack, x, y);
                        context.itemDecorations(aM_.font, stack, x, y);
                        index++;
                    }
                }
                context.pose().popMatrix();
                ScaleUtil.c(context);
            }
        }
        super.a(event);
    }
}
