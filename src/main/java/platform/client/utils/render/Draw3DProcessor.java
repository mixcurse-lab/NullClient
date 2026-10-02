package platform.client.utils.render;

import platform.api.utils.auction.BatchProcessor;
import platform.api.system.interfaces.NativeMethodLookup;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.render.ColorUtil;

import platform.api.system.configs.BaseProcessor;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.client.utils.render.pipeline.DeltaRenderUtil;
import platform.api.event.events.render.DrawEvent;

import platform.api.annotation.Compile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

public class Draw3DProcessor extends BaseProcessor implements Interface {
    @Override
    @Compile
    public void setup() {
    }

    static {
        NativeMethodLookup.lookup(Draw3DProcessor.class, 29);
    }

    @Override
    public void unSetup() {
    }

    @EventTarget(a = 4)
    public void a(DrawEvent event) {
        if (event.c()) {
            Delta.h().d().l().b();
        }
    }

    public void a(PoseStack matrices, AABB box, int color, float width) {
        Delta.h().d().l().a(BatchProcessor.b.a(matrices, box, color, width));
    }

    public void a(Matrix4f matrix, float minX, float minY, float maxX, float maxY, int color, boolean corners, boolean healthBar, float healthPercent, int healthColor) {
        Delta.h().d().l().a(new BatchProcessor.a(matrix, minX, minY, maxX, maxY, color, corners, healthBar, healthPercent, healthColor));
    }

    public void a(PoseStack matrices, Vec3 start, Vec3 end, Vec3 control, int color, float width) {
        Delta.h().d().l().a(BatchProcessor.b.a(matrices, start, end, control, color, width));
    }

    public void a(GuiGraphicsExtractor context, ItemStack stack, float x, float y, int z, float alpha, float scale, boolean overlay) {
        if (!stack.isEmpty()) {
            DeltaRenderUtil.flush(context);
            context.pose().pushMatrix();
            context.pose().translate(x, y);
            context.pose().scale(scale, scale);
            context.item(stack, 0, 0);
            if (overlay) {
                context.itemDecorations(aM_.font, stack, 0, 0);
            }
            context.pose().popMatrix();
        }
    }
}



