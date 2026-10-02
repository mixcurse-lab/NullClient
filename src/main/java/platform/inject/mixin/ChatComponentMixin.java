package platform.inject.mixin;

import static platform.api.module.Interface.aM_;

import platform.client.Delta;
import platform.client.features.modules.render.Animations;
import platform.client.utils.render.EasingList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$1")
public abstract class ChatComponentMixin {
    @WrapOperation(method = "accept(Lnet/minecraft/client/multiplayer/chat/GuiMessage$Line;IF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;handleMessage(IFLnet/minecraft/util/FormattedCharSequence;)Z"))
    private boolean delta$chatAppear(ChatComponent.ChatGraphicsAccess graphics, int y, float alpha, FormattedCharSequence text, Operation<Boolean> original, @Local(argsOnly = true) GuiMessage.Line line) {
        Animations animations = Delta.h().d().t().Q();
        if (!animations.m() || !animations.q().a("Появление сообщений").c().booleanValue()) {
            return original.call(graphics, y, alpha, text);
        }
        double t = Math.max(0.0d, Math.min(1.0d, ((double) ((aM_.gui.hud.getGuiTicks() - line.addedTime()) + aM_.getDeltaTracker().getGameTimeDeltaPartialTick(false))) / 9.0d));
        double progress = EasingList.p.ease((float) t);
        float slide = (float) (-(1.0d - progress) * 8.0d);
        graphics.updatePose(pose -> {
            Matrix3x2fStack stack = (Matrix3x2fStack) pose;
            stack.pushMatrix();
            stack.translate(slide, 0.0f);
        });
        boolean result = original.call(graphics, y, alpha * (float) progress, text);
        graphics.updatePose(pose -> ((Matrix3x2fStack) pose).popMatrix());
        return result;
    }
}
