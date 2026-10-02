package platform.api.handlers;

import platform.api.handlers.Handler_2;
import platform.inject.invokers.MultiPlayerGameModeInvoker;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.rotation.Look;

import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.event.events.client.TickEvent;
import platform.api.handlers.BaseHandler;
import platform.client.features.modules.player.WindHop;
import platform.client.utils.rotation.Rotation;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;

@Handler_2
public class UseableHandler extends BaseHandler implements Interface {
    private final List<a> b = new ArrayList();

    @Generated
    public List<a> a() {
        return this.b;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (!this.b.isEmpty()) {
            a task = (a) this.b.getFirst();
            WindHop windHop = Delta.h().d().t().aW();
            int hotbar = task.a().getItem() == Items.SPLASH_POTION ? InventoryUtil.b(task.a(), true) : InventoryUtil.a(task.a().getItem(), true);
            int inventory = task.a().getItem() == Items.SPLASH_POTION ? InventoryUtil.b(task.a(), false) : InventoryUtil.a(task.a().getItem(), false);
            if (task.d() == -1 && hotbar == -1 && inventory == -1) {
                this.b.remove(task);
                return;
            }
            task.c(task.d() + 1);
            if (task.d() == 0) {
                task.a(aM_.player.getInventory().getSelectedSlot());
                if (hotbar != -1) {
                    task.b(hotbar);
                    if (hotbar != aM_.player.getInventory().getSelectedSlot()) {
                        a(hotbar);
                        return;
                    }
                    return;
                }
                if (inventory != -1) {
                    int bundle = InventoryUtil.a(aM_.player.getInventory().getItem(inventory), task.a());
                    if (bundle != -1) {
                        aM_.player.connection.send(new ServerboundSelectBundleItemPacket(inventory < 9 ? 36 + inventory : inventory, bundle));
                    }
                    task.b((bundle == -1 || !aM_.player.getMainHandItem().isEmpty()) ? inventory : task.b());
                    Delta.h().d().v().a().a(inventory, aM_.player.getInventory().getSelectedSlot(), 1);
                    return;
                }
                return;
            }
            if (task.d() == 1) {
                if (task.a().getItem() == Items.WIND_CHARGE && windHop.m() && windHop.q().c().booleanValue()) {
                    float t = aM_.player.tickCount + aM_.getDeltaTracker().getGameTimeDeltaPartialTick(false);
                    float silent = (float) ((Math.sin(t * 0.31f) * 6.600001001477404d) + (Math.sin((t * 0.73f) + 1.1f) * 0.3000001491338646d));
                    Delta.h().d().k().a(new Rotation(Look.b() + silent, 90.0f + (silent / 2.0f)), 180.0f, 1, 3);
                }
                a(task);
                if (aM_.player.getInventory().getItem(task.c()).has(DataComponents.BUNDLE_CONTENTS)) {
                    aM_.player.getInventory().setItem(task.b(), ItemStack.EMPTY);
                    Delta.h().d().v().a().a(task.c(), 36 + task.b(), 1);
                } else if (task.c() > 8) {
                    Delta.h().d().v().a().a(task.b(), task.c(), 1);
                } else if (task.b() != aM_.player.getInventory().getSelectedSlot()) {
                    a(task.b());
                }
                this.b.remove(task);
            }
        }
    }

    public void a(int slot) {
        aM_.player.getInventory().setSelectedSlot(slot);
    }

    public void a(a task) {
        ((platform.inject.invokers.MultiPlayerGameModeInvoker) aM_.gameMode).invokeStartPrediction(aM_.level, sequence -> {
            return new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, aM_.player.getYRot(), aM_.player.getXRot());
        });
    }

    public void a(ItemStack itemStack) {
        this.b.add(new a(itemStack));
    }

    public static final class a {
        private final ItemStack a;
        private int b;
        private int c;
        private int d = -1;

        @Generated
        public void a(int selectedSlot) {
            this.b = selectedSlot;
        }

        @Generated
        public void b(int itemSlot) {
            this.c = itemSlot;
        }

        @Generated
        public void c(int ticks) {
            this.d = ticks;
        }

        @Generated
        public ItemStack a() {
            return this.a;
        }

        @Generated
        public int b() {
            return this.b;
        }

        @Generated
        public int c() {
            return this.c;
        }

        @Generated
        public int d() {
            return this.d;
        }

        public a(ItemStack itemStack) {
            this.a = itemStack;
        }
    }
}


