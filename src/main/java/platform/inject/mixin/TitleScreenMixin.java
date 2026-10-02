package platform.inject.mixin;

import static platform.api.module.Interface.aM_;

import platform.api.module.Interface;
import platform.client.ui.screen.MainScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void delta$init(CallbackInfo ci) {
        System.out.println("[Delta] TitleScreenMixin.init called!");
        if (aM_ == null) {
            System.out.println("[Delta] aM_ is null!");
            return;
        }
        if (aM_.gui == null) {
            System.out.println("[Delta] aM_.gui is null!");
            return;
        }
        if (!(aM_.gui.screen() instanceof MainScreen)) {
            System.out.println("[Delta] Replacing TitleScreen with MainScreen");
            aM_.gui.setScreen(new MainScreen());
            ci.cancel();
        }
    }
}

