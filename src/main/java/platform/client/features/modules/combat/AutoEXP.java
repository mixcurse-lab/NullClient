package platform.client.features.modules.combat;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.rotation.Look;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.HotbarEvent;
import platform.api.event.events.client.TickEvent;
import platform.client.utils.rotation.Rotation;

import platform.api.module.setting.BindSetting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

@ModuleRegister(a = "Auto EXP", b = "Бросает бутылочки опыта под себя, пока удерживается заданная клавиша", c = Category.Combat)
public class AutoEXP extends Module {
    private boolean c;
    private final BindSetting b = new BindSetting("Кнопка активации", -1, 0).a(() -> {
        d(true);
    }).b(() -> {
        d(false);
    });
    private final int[] d = {-1, -1};

    public AutoEXP() {
        a(this.b);
    }

    @EventTarget
    public void a(HotbarEvent event) {
        if (this.c) {
            event.a(true);
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (aM_.player == null) {
            return;
        }
        if (aM_.gui.screen() != null) {
            d(false);
        }
        if (this.c) {
            q();
        }
        if (!this.c && this.d[0] != -1) {
            aM_.player.getInventory().setSelectedSlot(this.d[0]);
            if (this.d[1] != -1) {
                Delta.h().d().v().a().a(7, this.d[1], 1);
            }
            this.d[0] = -1;
            this.d[1] = -1;
        }
    }

    private void q() {
        int invSlot;
        boolean inHand = aM_.player.getMainHandItem().getItem() == Items.EXPERIENCE_BOTTLE;
        if (!inHand) {
            int hotbarSlot = InventoryUtil.a(Items.EXPERIENCE_BOTTLE, true);
            if (hotbarSlot != -1) {
                if (aM_.player.getInventory().getSelectedSlot() != hotbarSlot) {
                    aM_.player.getInventory().setSelectedSlot(hotbarSlot);
                    return;
                }
                return;
            } else {
                if (Delta.h().d().v().a().a().isEmpty() && this.c && (invSlot = InventoryUtil.b(Items.EXPERIENCE_BOTTLE)) != -1) {
                    if (this.d[1] == -1) {
                        this.d[1] = invSlot;
                    }
                    if (aM_.player.getInventory().getSelectedSlot() != 7) {
                        aM_.player.getInventory().setSelectedSlot(7);
                    }
                    Delta.h().d().v().a().a(invSlot, 7, 1);
                    return;
                }
                return;
            }
        }
        Delta.h().d().k().a(new Rotation(Look.b(), 86.0f), 70.0f, 1, 3);
        if (Rotation.b().d() > 83.0f) {
            aM_.gameMode.useItem(aM_.player, InteractionHand.MAIN_HAND);
            aM_.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private void d(boolean value) {
        this.c = value;
        if (!value || aM_.player == null) {
            return;
        }
        this.d[0] = aM_.player.getInventory().getSelectedSlot();
    }

    @Override
    public void c() {
        super.c();
        this.c = false;
        this.d[0] = -1;
        this.d[1] = -1;
    }
}


