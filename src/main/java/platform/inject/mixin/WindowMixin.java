package platform.inject.mixin;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {

    private interface Dwmapi extends StdCallLibrary {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class, W32APIOptions.DEFAULT_OPTIONS);
        int DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, IntByReference pvAttribute, int cbAttribute);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        try {
            long windowHandle = org.lwjgl.glfw.GLFW.glfwGetCurrentContext();
            if (windowHandle == 0L) {
                for (java.lang.reflect.Field field : this.getClass().getDeclaredFields()) {
                    if (field.getType() == long.class) {
                        field.setAccessible(true);
                        long val = field.getLong(this);
                        if (val != 0L) {
                            windowHandle = val;
                            break;
                        }
                    }
                }
            }
            if (windowHandle == 0L) return;

            long hwndLong = GLFWNativeWin32.glfwGetWin32Window(windowHandle);
            if (hwndLong != 0L) {
                WinDef.HWND hwnd = new WinDef.HWND(new Pointer(hwndLong));
                IntByReference trueRef = new IntByReference(1);

                Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, 20, trueRef, 4);

                Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, 19, trueRef, 4);
            }
        } catch (Throwable ignored) {

        }
    }
}

