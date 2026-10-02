package platform.client.features.modules.render;

import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.module.setting.BooleanSetting;
import lombok.Generated;

@ModuleRegister(a = "Item Physic", b = "Добавляет физику предметам, лежащим на земле", c = Category.Render)
public class ItemPhysic extends Module {
    private final BooleanSetting b = new BooleanSetting("Уменьшить размер предметов", false);

    @Generated
    public BooleanSetting q() {
        return this.b;
    }

    public ItemPhysic() {
        a(this.b);
    }
}


