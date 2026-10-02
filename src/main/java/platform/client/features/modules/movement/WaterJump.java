package platform.client.features.modules.movement;

import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;

@ModuleRegister(a = "Water Jump", b = "Подбрасывает вас вверх при попадании на сыпучий блок под водой", c = Category.Movement)
public class WaterJump extends Module implements Interface {
}


