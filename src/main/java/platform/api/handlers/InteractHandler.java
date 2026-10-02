package platform.api.handlers;

import platform.api.handlers.Handler_2;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.text.ChatUtil;

import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.event.events.player.ClickEvent;
import platform.api.event.events.render.HotbarEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.handlers.BaseHandler;
import platform.api.handlers.InventoryHandler;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.world.InteractionHand;

@Handler_2
public class InteractHandler extends BaseHandler implements Interface {
    private final List<a> b = new ArrayList();
    private static boolean useKeyHeld;

    @Generated
    public List<a> b() {
        return this.b;
    }

    public void a(int slot) {
        if (this.b.isEmpty() && !platform.client.utils.player.UseItemUtil.b() && Delta.h().d().v().a().a().isEmpty()) {
            this.b.add(new a(slot));
        }
    }

    public boolean a() {
        return !this.b.isEmpty();
    }

    private static void c(boolean down) {
        aM_.options.keyUse.setDown(down);
        useKeyHeld = down;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (this.b.isEmpty()) {
            if (useKeyHeld) {
                c(false);
            }
            return;
        }
        if (aM_.player == null || aM_.player.tickCount <= 40) {
            return;
        }
        InventoryHandler inventoryHandler = Delta.h().d().v().a();
        a task = (a) this.b.getFirst();
        boolean inventory = task.b() > 8;
        task.a(task.d() + 1);
        if (task.d() == 1) {
            if (inventory) {
                inventoryHandler.a(task.b(), task.a(), 2);
            } else {
                aM_.player.getInventory().setSelectedSlot(task.b());
            }
        } else if (!task.c() && task.d() > 0 && inventoryHandler.a().isEmpty()) {
            c(true);
            if (aM_.player.isUsingItem()) {
                task.a(true);
            } else {
                aM_.gameMode.useItem(aM_.player, InteractionHand.MAIN_HAND);
            }
        } else if (task.c() && task.d() > 0 && !aM_.player.isUsingItem() && inventoryHandler.a().isEmpty()) {
            c(false);
            if (inventory) {
                inventoryHandler.a(task.a(), task.b(), 2);
            } else {
                aM_.player.getInventory().setSelectedSlot(task.a());
            }
            this.b.remove(task);
        }
        if (task.d() >= 60) {
            c(false);
            ChatUtil.a((Object) "Использование предмета не удалось по неизвестной причине");
            this.b.remove(task);
        }
    }

    @EventTarget
    public void a(HotbarEvent event) {
        if (a()) {
            event.a(true);
        }
    }

    @EventTarget
    public void a(ClickEvent event) {
        if (a() && event.h() == 1) {
            event.a(true);
        }
    }

    public static final class a {
        private final int a = Interface.aM_.player.getInventory().getSelectedSlot();
        private final int b;
        private boolean c;
        private int d;

        @Generated
        public void a(boolean returned) {
            this.c = returned;
        }

        @Generated
        public void a(int ticks) {
            this.d = ticks;
        }

        @Generated
        public int a() {
            return this.a;
        }

        @Generated
        public int b() {
            return this.b;
        }

        @Generated
        public boolean c() {
            return this.c;
        }

        @Generated
        public int d() {
            return this.d;
        }

        public a(int eatSlot) {
            this.b = eatSlot;
        }
    }
}



