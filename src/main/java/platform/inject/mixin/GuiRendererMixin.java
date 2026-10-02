package platform.inject.mixin;

import platform.client.utils.render.DeltaBlurProcessor;
import platform.client.utils.render.pipeline.DeltaPipelines;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import java.util.List;
import java.util.function.Supplier;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Shadow
    @Final
    private List<?> draws;

    @Unique
    private final IntList delta$frostedSplits = new IntArrayList();
    @Unique
    private boolean delta$previousElementWasFrosted;

    @Inject(method = "prepare", at = @At("HEAD"))
    private void delta$resetFrostedSplits(CallbackInfo ci) {
        this.delta$frostedSplits.clear();
        this.delta$previousElementWasFrosted = false;
    }

    @Inject(method = "addElementsToMeshes", at = @At("HEAD"))
    private void delta$resetFrostedFlagPerRange(net.minecraft.client.renderer.state.gui.GuiRenderState.TraverseRange range, CallbackInfo ci) {


        this.delta$previousElementWasFrosted = false;
    }

    @Inject(method = "addElementToMesh", at = @At("HEAD"))
    private void delta$markFrostedRuns(GuiElementRenderState elementState, CallbackInfo ci) {
        boolean frosted = delta$isFrostedPipeline(elementState.pipeline());
        if (frosted && !this.delta$previousElementWasFrosted) {
            this.delta$frostedSplits.add(this.draws.size());
        }
        this.delta$previousElementWasFrosted = frosted;
    }

    @WrapOperation(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;II)V"
            )
    )
    private void delta$captureBackdropBeforeFrostedRuns(
            GuiRenderer instance,
            Supplier<String> label,
            RenderTarget mainRenderTarget,
            GpuBufferSlice dynamicTransforms,
            int startIndex,
            int endIndex,
            Operation<Void> original
    ) {
        int cursor = startIndex;
        for (int i = 0; i < this.delta$frostedSplits.size(); i++) {
            int split = this.delta$frostedSplits.getInt(i);
            if (split < cursor || split >= endIndex) {
                continue;
            }
            if (split > cursor) {
                original.call(instance, label, mainRenderTarget, dynamicTransforms, cursor, split);
            }
            DeltaBlurProcessor.getInstance().run();
            cursor = split;
        }
        if (cursor < endIndex) {
            original.call(instance, label, mainRenderTarget, dynamicTransforms, cursor, endIndex);
        }
    }

    @Unique
    private static boolean delta$isFrostedPipeline(RenderPipeline pipeline) {
        return pipeline == DeltaPipelines.FROSTED || pipeline == DeltaPipelines.FROSTED_SHADOW;
    }
}
