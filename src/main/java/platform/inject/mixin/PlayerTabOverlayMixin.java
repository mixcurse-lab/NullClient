package platform.inject.mixin;

import static platform.api.module.Interface.aM_;

import platform.client.features.modules.render.Animations;
import platform.client.Delta;
import platform.api.module.Interface;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerTabOverlay.class})
public abstract class PlayerTabOverlayMixin {
    @Inject(method = {"setVisible"}, at = {@At("HEAD")})
    private void setVisible(boolean visible, CallbackInfo ci) {
        Animations animations = Delta.h().d().t().Q();
        if (animations.m() && animations.q().a("TAB").c().booleanValue()) {
            animations.r().a(visible);
        }
    }

    @Inject(method = {"extractRenderState"}, at = {@At("HEAD")})
    private void headRender(GuiGraphicsExtractor context, int scaledWindowWidth, Scoreboard scoreboard, @Nullable Objective objective, CallbackInfo ci) {
        Animations animations = Delta.h().d().t().Q();
        if (animations.m() && animations.q().a("TAB").c().booleanValue()) {
            context.pose().pushMatrix();
            context.pose().translate(0.0f, (-200.0f) * (1.0f - animations.r().c()));
        }
    }

    @Inject(method = {"extractRenderState"}, at = {@At("RETURN")})
    private void render(GuiGraphicsExtractor context, int scaledWindowWidth, Scoreboard scoreboard, @Nullable Objective objective, CallbackInfo ci) {
        Animations animations = Delta.h().d().t().Q();
        if (animations.m() && animations.q().a("TAB").c().booleanValue()) {
            context.pose().popMatrix();
        }
    }
}

