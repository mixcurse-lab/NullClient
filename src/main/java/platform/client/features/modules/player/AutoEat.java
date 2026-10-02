package platform.client.features.modules.player;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.SliderSetting;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;

@ModuleRegister(a = "Auto Eat", b = "Автоматически утоляет голод при его падении", c = Category.Player)
public class AutoEat extends Module {
    private final SliderSetting b = new SliderSetting("Есть при голоде", 16.0f, 1.0f, 20.0f, 1.0f);

    public AutoEat() {
        a(this.b);
    }

    @EventTarget
    public void a(TickEvent event) {
        b(this.b.c().intValue());
    }

    public void b(int level) {
        int slot;
        if (aM_.player != null && aM_.player.getFoodData().getFoodLevel() < level && (slot = q()) >= 0) {
            Delta.h().d().v().k().a(slot);
        }
    }

    private int q() {
        int i;
        Set<Item> blacklist = Set.of(Items.CHORUS_FRUIT, Items.PUFFERFISH, Items.DRIED_KELP);
        Set<Item> raw = Set.of(Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT, Items.COD, Items.SALMON, Items.POTATO);
        int bestSlot = -1;
        int bestRank = Integer.MAX_VALUE;
        for (int slot = 0; slot < 36 && bestRank > 0; slot++) {
            ItemStack stack = aM_.player.getInventory().getItem(slot);
            if (stack.has(DataComponents.FOOD) && !blacklist.contains(stack.getItem()) && !stack.has(DataComponents.CUSTOM_NAME)) {
                if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                    i = 3;
                } else if (stack.is(Items.GOLDEN_APPLE)) {
                    i = 2;
                } else {
                    i = raw.contains(stack.getItem()) ? 1 : 0;
                }
                int rank = i;
                if (rank < bestRank) {
                    bestRank = rank;
                    bestSlot = slot;
                }
            }
        }
        return bestSlot;
    }
}





