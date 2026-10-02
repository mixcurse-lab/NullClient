package platform.inject.mixin;

import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.module.Interface;
import platform.client.Delta;
import platform.api.event.events.render.CrosshairEvent;
import platform.api.event.events.render.DrawEvent;
import platform.api.event.events.render.RemovalsEvent;
import platform.api.event.events.render.ScoreboardEvent;
import platform.client.features.modules.render.Animations;
import platform.client.utils.render.pipeline.DeltaRenderUtil;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void delta$beforeHudRender(GuiGraphicsExtractor context, DeltaTracker delta, CallbackInfo ci) {
        float tickDelta = delta.getGameTimeDeltaPartialTick(false);
        EventManager.a((IEvent) new DrawEvent(context, tickDelta, DrawEvent.a.D2D));
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void delta$afterHudRender(GuiGraphicsExtractor context, DeltaTracker delta, CallbackInfo ci) {
        DeltaRenderUtil.flush(context);
    }

    @Inject(method = "extractTabList", at = @At("RETURN"))
    private void delta$tabListFadeOut(GuiGraphicsExtractor context, DeltaTracker delta, CallbackInfo ci) {
        Animations animations = Delta.h().d().t().Q();
        if (animations.m() && animations.q().a("TAB").c().booleanValue() && animations.r().c() > 0.0f
            && !Interface.aM_.options.keyPlayerList.isDown()
            && Interface.aM_.player != null
            && !(Interface.aM_.isLocalServer() && Interface.aM_.player.connection.getListedOnlinePlayers().size() > 1)) {
            Scoreboard scoreboard = Interface.aM_.level.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
            if (objective != null) {
                context.nextStratum();
                Interface.aM_.gui.hud.getTabList().extractRenderState(context, context.guiWidth(), scoreboard, objective);
            }
        }
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At("HEAD"))
    private void delta$hotbarRaiseHead(GuiGraphicsExtractor context, DeltaTracker delta, CallbackInfo ci) {
        Animations animations = Delta.h().d().t().Q();
        if (animations.m() && animations.q().a("Поднятие хотбара").c().booleanValue()) {
            context.pose().pushMatrix();
            context.pose().translate(0.0f, (-16.0f) * animations.s().c());
        }
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At("RETURN"))
    private void delta$hotbarRaiseTail(GuiGraphicsExtractor context, DeltaTracker delta, CallbackInfo ci) {
        Animations animations = Delta.h().d().t().Q();
        if (animations.m() && animations.q().a("Поднятие хотбара").c().booleanValue()) {
            context.pose().popMatrix();
        }
    }

    @ModifyArg(method = "extractItemHotbar", index = 2, at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private int delta$hotbarSelectionSlot(int x) {
        Animations animations = Delta.h().d().t().Q();
        if (animations.m() && animations.q().a("Слот хотбара").c().booleanValue() && Interface.aM_.player != null) {
            return Math.round((x - (Interface.aM_.player.getInventory().getSelectedSlot() * 20)) + (animations.v() * 20.0f));
        }
        return x;
    }

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void delta$beforeCrosshair(GuiGraphicsExtractor context, DeltaTracker delta, CallbackInfo ci) {
        float tickDelta = delta.getGameTimeDeltaPartialTick(false);
        CrosshairEvent event = new CrosshairEvent(context, tickDelta);
        EventManager.a((IEvent) event);
        if (event.a()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void delta$scoreboard(GuiGraphicsExtractor context, DeltaTracker delta, CallbackInfo ci) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.SCOREBOARD);
        EventManager.a((IEvent) event);
        if (event.a()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "displayScoreboardSidebar", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Objective;getDisplayName()Lnet/minecraft/network/chat/Component;"))
    private Component delta$scoreboardTitle(Component original) {
        ScoreboardEvent event = new ScoreboardEvent(original);
        EventManager.a((IEvent) event);
        return event.b();
    }

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void delta$hideVanillaEffects(GuiGraphicsExtractor context, DeltaTracker delta, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "extractPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void delta$removalsPortal(GuiGraphicsExtractor graphics, float alpha, CallbackInfo ci) {
        RemovalsEvent event = new RemovalsEvent(RemovalsEvent.a.PORTAL);
        EventManager.a((IEvent) event);
        if (event.a()) {
            ci.cancel();
        }
    }

    @Redirect(method = "displayScoreboardSidebar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"))
    private void delta$scoreboardLine(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color, boolean shadow) {
        ScoreboardEvent event = new ScoreboardEvent(text);
        EventManager.a((IEvent) event);
        graphics.text(font, event.b(), x, y, color, shadow);
    }
}
