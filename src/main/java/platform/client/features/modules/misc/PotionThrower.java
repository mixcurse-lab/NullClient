package platform.client.features.modules.misc;

import platform.api.utils.auction.AutoBuyEntry;
import platform.api.utils.auction.AutoBuyProcessor;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;

import platform.client.ui.screen.AssistantScreen;
import platform.api.module.setting.BindSetting;
import platform.api.module.setting.ModeSetting;
import lombok.Generated;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;

@ModuleRegister(a = "Potion Thrower", b = "Быстрое метание бафов через колесо или по клавише", c = Category.Misc)
public class PotionThrower extends Module implements Interface {
    private final ModeSetting b = new ModeSetting("Способ использования зелий", "Колесо выбора", "Колесо выбора", "Клавиша");
    private final AssistantScreen c = new AssistantScreen(Component.literal("Potion Thrower"));
    private final BindSetting d = (BindSetting) new BindSetting("Открыть меню зелий", 86, 0).a(() -> {
        aM_.gui.setScreen(this.c);
    }).b(() -> {
        if (aM_.gui.screen() == this.c) {
            this.c.b(this.c.b());
            if (aM_.gui.screen() == this.c) {
                aM_.gui.setScreen((Screen) null);
            }
        }
    }).a(() -> {
        return Boolean.valueOf(this.b.l("Колесо выбора"));
    });

    @Generated
    public AssistantScreen q() {
        return this.c;
    }

    public PotionThrower() {
        a(this.b, this.d);
        for (AutoBuyEntry potion : AutoBuyEntry.values()) {
            if (potion.d() == Items.SPLASH_POTION) {
                a(new BindSetting(potion.b(), -1).a(() -> {
                    Delta.h().d().v().b().a(potion.a());
                }).a(() -> {
                    return Boolean.valueOf(this.b.l("Клавиша"));
                }));
            }
        }
    }
}
