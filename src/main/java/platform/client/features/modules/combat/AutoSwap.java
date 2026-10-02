package platform.client.features.modules.combat;

import static platform.api.module.Interface.aM_;

import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.client.Delta;
import platform.client.ui.screen.SwapScreen;
import platform.client.utils.player.InventoryUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import platform.api.module.setting.BindSetting;
import platform.api.module.setting.ModeSetting;

@ModuleRegister(a = "Auto Swap", b = "Мгновенно перекладывает выбранные предметы во вторую руку по нажатию клавиши", c = Category.Combat)
public class AutoSwap extends Module {
    private final ModeSetting mode = new ModeSetting("Режим перемещения", "Двойной", "Двойной", "Тройной");
    private final ModeSetting first = (ModeSetting) new ModeSetting("Первый предмет", "Сфера", "Сфера", "Тотем", "Золотое яблоко", "Щит").a(() -> this.mode.l("Двойной"));
    private final ModeSetting second = (ModeSetting) new ModeSetting("Второй предмет", "Тотем", "Сфера", "Тотем", "Золотое яблоко", "Щит").a(() -> this.mode.l("Двойной"));
    private final BindSetting swapKey = new BindSetting("Кнопка перемещения", 86).a(() -> {
        AutoTotem autoTotem = Delta.h().d().t().V();
        if (autoTotem != null && autoTotem.b) {
            return;
        }
        if (this.mode.l("Двойной")) {
            ItemStack offhand = aM_.player.getOffhandItem();
            Item target = offhand.getItem() == item(this.first) ? item(this.second) : item(this.first);
            int slot = InventoryUtil.c(target);
            if (slot != -1) {
                Delta.h().d().v().a().a(slot, 40, 1);
            }
        } else if (this.mode.l("Тройной")) {
            aM_.gui.setScreen(new SwapScreen(Component.literal("SwapMenu")));
        }
    });

    public AutoSwap() {
        a(this.swapKey, this.mode, this.first, this.second);
    }

    private Item item(ModeSetting setting) {
        switch (setting.c()) {
            case "Сфера":
                return Items.PLAYER_HEAD;
            case "Тотем":
                return Items.TOTEM_OF_UNDYING;
            case "Золотое яблоко":
                return Items.GOLDEN_APPLE;
            case "Щит":
                return Items.SHIELD;
            default:
                return null;
        }
    }
}
