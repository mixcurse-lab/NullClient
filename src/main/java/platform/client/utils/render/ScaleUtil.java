package platform.client.utils.render;

import static platform.api.module.Interface.aM_;

import platform.api.module.Interface;

import lombok.Generated;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ScaleUtil implements Interface {
    @Generated
    private ScaleUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void a(GuiGraphicsExtractor context, int scale) {
        Window window = aM_.getWindow();
        int previous = window.getGuiScale();
        int target = window.calculateScale(scale, aM_.isEnforceUnicode());
        window.setGuiScale(target);
        context.pose().pushMatrix();
        context.pose().scale((float) ((double) target / (double) previous), (float) ((double) target / (double) previous));
    }

    public static void a(GuiGraphicsExtractor context) {
        context.pose().popMatrix();
        aM_.getWindow().setGuiScale(aM_.getWindow().calculateScale(((Integer) aM_.options.guiScale().get()).intValue(), aM_.isEnforceUnicode()));
    }

    public static void b(GuiGraphicsExtractor context) {
        a(context, ((Integer) aM_.options.guiScale().get()).intValue());
    }

    public static void c(GuiGraphicsExtractor context) {
        context.pose().popMatrix();
        aM_.getWindow().setGuiScale(aM_.getWindow().calculateScale(2, aM_.isEnforceUnicode()));
    }
}



