package platform.api.module.setting;

import platform.client.ui.element.Element_2;
import platform.client.ui.element.ColorElement;

public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, Integer defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public Element_2<?> d() {
        return new ColorElement(this);
    }
}



