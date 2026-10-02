package platform.client.features.modules.movement;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.SliderSetting;
import net.minecraft.world.phys.BlockHitResult;

@ModuleRegister(a = "Fast Break", b = "Ускоряет разрушение блоков, обрабатывая добычу несколько раз за тик", c = Category.Movement)
public class FastBreak extends Module {
    private final SliderSetting b = new SliderSetting("Интенсивность копания", 2.0f, 1.0f, 5.0f, 0.25f);

    public FastBreak() {
        a(this.b);
    }

    @EventTarget
    public void a(TickEvent event) {
        if (aM_.options.keyAttack.isDown() && aM_.gameMode.isDestroying()) {
            if (aM_.hitResult instanceof BlockHitResult class_3965Var) {
                BlockHitResult hit = class_3965Var;
                for (int i = 0; i < this.b.c().intValue() - 1; i++) {
                    aM_.gameMode.continueDestroyBlock(hit.getBlockPos(), hit.getDirection());
                }
            }
        }
    }
}


