package platform.inject.mixin;
import platform.api.event.EventManager;
import platform.api.event.interfaces.IEvent;
import platform.api.event.events.render.WorldRenderEvent;
import platform.api.system.configs.ThemeInfo;
import platform.client.Delta;
import platform.client.features.modules.render.ShaderSky;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.buffers.GpuBufferSlice; import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.DeltaTracker; import net.minecraft.client.renderer.LevelRenderer; import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc; import org.joml.Vector4f; import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin; import org.spongepowered.asm.mixin.injection.At; import org.spongepowered.asm.mixin.injection.Inject; import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin{
 @Inject(method="render", at=@At("TAIL"))
 private void delta$skyTail(GraphicsResourceAllocator a,DeltaTracker dt,boolean outline,CameraRenderState cam,Matrix4fc proj,GpuBufferSlice fog,Vector4f sky,boolean frustum,CallbackInfo ci){
   if (ShaderSky.check()) {
       try { ShaderSky.getInstance().renderSky(); } catch (Throwable ignored) { System.out.println("[SkyTail] fail " + ignored); }
   }
   try {
       EventManager.a((IEvent) new WorldRenderEvent(dt.getGameTimeDeltaPartialTick(false)));
   } catch (Throwable ignored) { }
 }


 @Inject(method = "addSkyPass", at = @At("HEAD"), cancellable = true)
 private void delta$cancelSky(com.mojang.blaze3d.framegraph.FrameGraphBuilder builder, net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState, GpuBufferSlice fog, CallbackInfo ci) {
     if (ShaderSky.check()) {
         ci.cancel();
     }
 }

 @Inject(method = "addCloudsPass", at = @At("HEAD"), cancellable = true)
 private void delta$cancelClouds(com.mojang.blaze3d.framegraph.FrameGraphBuilder builder, net.minecraft.client.CloudStatus cloudStatus, net.minecraft.world.phys.Vec3 pos, long time, float partialTick, int renderDistance, float cloudHeight, int cloudColor, CallbackInfo ci) {
     if (ShaderSky.check()) {
         ci.cancel();
     }
 }


 @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;SCREEN_SIZE_TARGET_CLEAR_COLOR:Lorg/joml/Vector4fc;"))
 private Vector4fc delta$shaderSkyClearColor(Vector4fc original) {
     if (!ShaderSky.check()) return original;
     try {
         int themeInt = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
         float dim = 0.06f;
         float r = ((themeInt >> 16) & 0xFF) / 255f * dim;
         float g = ((themeInt >> 8) & 0xFF) / 255f * dim;
         float b = (themeInt & 0xFF) / 255f * dim;
         return new Vector4f(r, g, b, 1.0f);
     } catch (Throwable ignored) {
         return original;
     }
 }
}
