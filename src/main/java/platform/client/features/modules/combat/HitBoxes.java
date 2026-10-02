package platform.client.features.modules.combat;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.other.BoundingBoxEvent;

import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

@ModuleRegister(a = "Hit Boxes", b = "Увеличивает хитбокс выбранных сущностей, упрощая попадания по ним", c = Category.Combat)
public class HitBoxes extends Module {
    private final SliderSetting b = new SliderSetting("Расширение X и Z", 0.5f, 0.0f, 1.0f, 0.1f);
    private final SliderSetting c = new SliderSetting("Расширение Y", 0.5f, 0.0f, 1.0f, 0.1f);
    private final ModeSetting d = new ModeSetting("Цель", "Игроки", "Игроки", "Мобы", "Все");

    public HitBoxes() {
        a(this.b, this.c, this.d);
    }

    @EventTarget
    public void a(BoundingBoxEvent event) {
        Entity entity = event.c();
        if (entity.getId() == aM_.player.getId()) {
            return;
        }
        if (Delta.h().d().e().d(entity.getName().getString())) {
            return;
        }
        boolean targets = (this.d.l("Игроки") && entity instanceof Player) || (this.d.l("Мобы") && entity instanceof Mob) || this.d.l("Все");
        if (targets) {
            AABB box = event.b();
            AABB changedBox = new AABB(box.minX - ((double) (this.b.c().floatValue() / 2.0f)), box.minY, box.minZ - ((double) (this.b.c().floatValue() / 2.0f)), box.maxX + ((double) (this.b.c().floatValue() / 2.0f)), box.maxY + ((double) this.c.c().floatValue()), box.maxZ + ((double) (this.b.c().floatValue() / 2.0f)));
            event.a(changedBox);
        }
    }
}


