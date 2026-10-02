package platform.inject.mixin;

import static platform.api.module.Interface.aM_;

import platform.client.Delta;
import platform.api.event.EventManager;
import platform.api.event.GlobalEvent;
import platform.api.event.interfaces.IEvent;
import platform.api.event.events.other.CrosshairTargetEvent;
import platform.api.event.events.render.HotbarEvent;
import platform.client.features.modules.player.OpenWalls;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Unique
    private static final String DELTA_WINDOW_TITLE = "NullClient";

    @Shadow
    public HitResult hitResult;

    @Shadow
    public Entity crosshairPickEntity;

    @Inject(method = "createTitle", at = @At("HEAD"), cancellable = true)
    private void createTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(DELTA_WINDOW_TITLE);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onGlobalTick(CallbackInfo ci) {
        EventManager.a((IEvent) new GlobalEvent());
    }

    @Inject(method = {"pick"}, at = {@At("TAIL")})
    private void onPick(float tickDelta, CallbackInfo ci) {
        CrosshairTargetEvent event = new CrosshairTargetEvent(tickDelta);
        EventManager.a((IEvent) event);
        if (event.a() && event.c() != null) {
            this.hitResult = event.c();
            this.crosshairPickEntity = null;
        }
        try {
            OpenWalls openWalls = Delta.h().d().t().openWalls();
            if (openWalls != null && openWalls.m() && aM_.player != null) {
                BlockHitResult hit = openWalls.a(aM_.player);
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    this.hitResult = hit;
                    this.crosshairPickEntity = null;
                }
            }
        } catch (Exception ignored) {}
    }

    @Redirect(method = {"handleKeybinds"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"))
    private void handleKeybinds(Inventory inventory, int slot) {
        HotbarEvent event = new HotbarEvent(slot);
        EventManager.a((IEvent) event);
        if (!event.a()) {
            inventory.setSelectedSlot(slot);
        }
    }


    @WrapOperation(method = {"run"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runTick(Z)V"))
    private void delta$guardRunTick(net.minecraft.client.Minecraft instance, boolean renderLevel, Operation<Void> original) {
        try {
            original.call(instance, renderLevel);
        } catch (Throwable t) {
            if (!platform.client.utils.player.CrashGuard.guard("game tick", t)) {
                throw t;
            }
        }
    }


    @WrapOperation(method = {"runTick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;renderFrame(Z)V"))
    private void delta$guardRenderFrame(net.minecraft.client.Minecraft instance, boolean renderLevel, Operation<Void> original) {
        try {
            original.call(instance, renderLevel);
        } catch (Throwable t) {
            if (!platform.client.utils.player.CrashGuard.guard("render frame", t)) {
                throw t;
            }
        }
    }


    @Inject(method = {"emergencySaveAndCrash"}, at = {@At("HEAD")}, cancellable = true)
    private void delta$guardEmergencyCrash(net.minecraft.CrashReport report, CallbackInfo ci) {
        try {
            if (platform.client.utils.player.CrashGuard.guard("crash report", report)) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
        }
    }
}

