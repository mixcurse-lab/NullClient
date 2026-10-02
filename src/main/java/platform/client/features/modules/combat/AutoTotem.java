package platform.client.features.modules.combat;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.player.InventoryUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.TickEvent;

import platform.api.module.setting.BooleanSetting;
import platform.client.utils.timer.CounterUtil;
import platform.api.module.setting.MultiModeSetting;
import platform.api.module.setting.SliderSetting;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;

@ModuleRegister(a = "Auto Totem", b = "Берёт тотем бессмертия в руку при падении здоровья ниже заданного значения", c = Category.Combat)
public class AutoTotem extends Module implements Interface {
    private final SliderSetting c = new SliderSetting("Порог здоровья", 6.0f, 1.0f, 20.0f, 0.5f);
    private final MultiModeSetting d = new MultiModeSetting("Дополнительные опции", new BooleanSetting("Возвращать предмет", true), new BooleanSetting("Сначала обычные тотемы", true), new BooleanSetting("Не во время еды", false));
    private final BooleanSetting e = new BooleanSetting("Умное перемещение", true);
    private final CounterUtil f = new CounterUtil();
    private final float[] g = new float[20];
    private int h = -1;
    public boolean b;

    public AutoTotem() {
        a(this.c, this.d, this.e);
    }

    @Override
    public void b() {
        super.b();
        this.h = -1;
    }

    @Override
    public void c() {
        super.c();
        this.h = -1;
        this.b = false;
    }

    @EventTarget
    public void a(TickEvent event) {
        System.arraycopy(this.g, 0, this.g, 1, 19);
        this.g[0] = aM_.player.getHealth() + (aM_.player.hasEffect(MobEffects.ABSORPTION) ? aM_.player.getAbsorptionAmount() : 0.0f);
        if (this.d.a("Не во время еды").c().booleanValue() && t()) {
            return;
        }
        Boolean should = s();
        this.b = should != null ? should.booleanValue() : this.b;
        if (should != null) {
            if (should.booleanValue()) {
                if (!this.f.a(400L) || !q()) {
                    return;
                }
            } else if (!r()) {
                return;
            }
            this.f.b();
        }
    }

    private boolean q() {
        ItemStack offhand = aM_.player.getOffhandItem();
        boolean preferPlain = this.d.a("Сначала обычные тотемы").c().booleanValue();
        if (a(offhand) && (!preferPlain || !offhand.isEnchanted())) {
            return false;
        }
        int slot = preferPlain ? InventoryUtil.a(Items.TOTEM_OF_UNDYING, false, true) : -1;
        if (slot == -1 && !a(offhand)) {
            slot = InventoryUtil.b(Items.TOTEM_OF_UNDYING);
        }
        if (slot == -1) {
            return false;
        }
        if (this.d.a("Возвращать предмет").c().booleanValue() && this.h == -1 && !offhand.isEmpty()) {
            this.h = slot;
        }
        Delta.h().d().v().a().a(slot, 40, 1);
        return true;
    }

    private boolean r() {
        ItemStack offhand = aM_.player.getOffhandItem();
        if (this.d.a("Возвращать предмет").c().booleanValue() && this.h != -1 && (offhand.isEmpty() || a(offhand))) {
            Delta.h().d().v().a().a(this.h, 40, 1);
        }
        this.h = -1;
        return false;
    }

    private Boolean s() {
        if (aM_.player.getCooldowns().isOnCooldown(Items.TOTEM_OF_UNDYING.getDefaultInstance())) {
            return false;
        }
        ItemStack active = aM_.player.getUseItem();
        if (this.e.c().booleanValue() && t() && active.getItem().getUseDuration(active, aM_.player) > 5) {
            int left = aM_.player.getUseItemRemainingTicks();
            if (left < 10) {
                return null;
            }
            if (left < 30 && ((this.g[19] - this.g[0]) / 20.0f) * left < this.g[0]) {
                return null;
            }
        }
        if (this.g[0] <= this.c.c().floatValue()) {
            return true;
        }
        return this.g[0] >= this.c.c().floatValue() + 0.5f ? false : null;
    }

    private boolean t() {
        ItemStack active = aM_.player.getUseItem();
        return (!aM_.player.isUsingItem() || active.isEmpty() || active.get(DataComponents.FOOD) == null) ? false : true;
    }

    private boolean a(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING;
    }
}


