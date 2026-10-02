package platform.client.features.modules.movement;

import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.player.PushEvent;
import platform.api.module.setting.BooleanSetting;

import platform.api.module.setting.MultiModeSetting;
import lombok.Generated;

@ModuleRegister(a = "No Push", b = "Отключает отталкивание от выбранных объектов", c = Category.Movement)
public class NoPush extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Отключить коллизию для", new BooleanSetting("Воды и лавы", false), new BooleanSetting("Блоков", false), new BooleanSetting("Энтити", false), new BooleanSetting("Граница", false), new BooleanSetting("Удочки", false));

    @Generated
    public MultiModeSetting q() {
        return this.b;
    }

    public NoPush() {
        a(this.b);
    }

    @EventTarget
    public void a(PushEvent event) {
        switch (event.b()) {
            case FLUIDS:
                event.a(this.b.a("Воды и лавы").c().booleanValue());
                break;
            case BLOCKS:
                event.a(this.b.a("Блоков").c().booleanValue());
                break;
            case ENTITIES:
                event.a(this.b.a("Энтити").c().booleanValue());
                break;
            case WORLD_BORDER:
                event.a(this.b.a("Граница").c().booleanValue());
                break;
            case FISHING_HOOK:
                event.a(this.b.a("Удочки").c().booleanValue());
                break;
        }
    }
}


