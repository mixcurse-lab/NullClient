package platform.api.module.setting;

import platform.client.ui.element.Element_2;
import platform.client.ui.element.BooleanElement;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, Boolean defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public Element_2<?> d() {
        return new BooleanElement(this);
    }
}



