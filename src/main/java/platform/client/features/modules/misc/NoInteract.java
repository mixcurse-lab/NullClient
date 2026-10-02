package platform.client.features.modules.misc;

import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.module.ModuleRegister;

import platform.api.module.setting.BooleanSetting;
import lombok.Generated;

@ModuleRegister(a = "No Interact", b = "Блокирует случайное взаимодействие с контейнерами и блоками", c = Category.Misc)
public class NoInteract extends Module {
    private final BooleanSetting b = new BooleanSetting("Учитывать включённую Aura", true);

    @Generated
    public BooleanSetting q() {
        return this.b;
    }

    public NoInteract() {
        a(this.b);
    }
}








