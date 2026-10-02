package platform.client.features.modules.player;

import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.module.ModuleRegister;

import platform.client.utils.timer.CounterUtil;
import platform.api.module.setting.SliderSetting;
import lombok.Generated;

@ModuleRegister(a = "Item Scroller", b = "Позволяет быстро перекладывать предметы в окнах прокруткой", c = Category.Player)
public class ItemScroller extends Module {
    private final SliderSetting b = new SliderSetting("Задержка между слотами", 50.0f, 0.0f, 100.0f, 1.0f);
    private final CounterUtil c = new CounterUtil();

    @Generated
    public SliderSetting q() {
        return this.b;
    }

    @Generated
    public CounterUtil r() {
        return this.c;
    }

    public ItemScroller() {
        a(this.b);
    }
}





