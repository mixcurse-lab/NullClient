package platform.api.handlers;

import platform.api.handlers.Handler_2;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.player.InventoryUtil;

import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.event.events.client.TickEvent;
import platform.api.handlers.BaseHandler;
import platform.api.handlers.StopHandler;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.core.component.DataComponents;

@Handler_2
public class InventoryHandler extends BaseHandler implements Interface {
    private final List<a> b = new ArrayList();

    @Generated
    public List<a> a() {
        return this.b;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (!this.b.isEmpty()) {
            a task = (a) this.b.getFirst();
            StopHandler stopHandler = Delta.h().d().v().c();
            if (stopHandler.c() < task.c()) {
                int from = a(task.a());
                int to = task.d() ? task.b() : a(task.b());
                if (aM_.player.inventoryMenu.getSlot(from).getItem().has(DataComponents.BUNDLE_CONTENTS)) {
                    aM_.gameMode.handleContainerInput(aM_.player.inventoryMenu.containerId, from, 1, ContainerInput.PICKUP, aM_.player);
                    aM_.gameMode.handleContainerInput(aM_.player.inventoryMenu.containerId, to, 0, ContainerInput.PICKUP, aM_.player);
                    if (!aM_.player.inventoryMenu.getCarried().isEmpty()) {
                        aM_.gameMode.handleContainerInput(aM_.player.inventoryMenu.containerId, from, 0, ContainerInput.PICKUP, aM_.player);
                    }
                } else {
                    int swapButton = a(task.b(), to);
                    if (swapButton != -1) {
                        aM_.gameMode.handleContainerInput(aM_.player.inventoryMenu.containerId, from, swapButton, ContainerInput.SWAP, aM_.player);
                    } else {
                        int swapButton2 = a(task.a(), from);
                        if (swapButton2 != -1) {
                            aM_.gameMode.handleContainerInput(aM_.player.inventoryMenu.containerId, to, swapButton2, ContainerInput.SWAP, aM_.player);
                        } else if (from != to) {
                            aM_.gameMode.handleContainerInput(aM_.player.inventoryMenu.containerId, from, 0, ContainerInput.SWAP, aM_.player);
                            aM_.gameMode.handleContainerInput(aM_.player.inventoryMenu.containerId, to, 0, ContainerInput.SWAP, aM_.player);
                            aM_.gameMode.handleContainerInput(aM_.player.inventoryMenu.containerId, from, 0, ContainerInput.SWAP, aM_.player);
                        }
                    }
                }
                aM_.player.connection.send(new ServerboundContainerClosePacket(aM_.player.containerMenu.containerId));
                this.b.remove(task);
                if (!this.b.isEmpty()) {
                    stopHandler.a(((a) this.b.getFirst()).c());
                }
            }
        }
    }

    public void a(int fromSlot, int toSlot, int bypass) {
        a(new a(fromSlot, toSlot, bypass, false));
    }

    public void b(int fromSlot, int armorSlot, int bypass) {
        a(new a(fromSlot, 5 + armorSlot, bypass, true));
    }

    public void a(Item item, int toSlot, int bypass) {
        int slot = InventoryUtil.b(item);
        if (slot != -1) {
            a(new a(slot, toSlot, bypass, false));
        }
    }

    public void a(ItemStack stack, int toSlot, int bypass) {
        int slot = InventoryUtil.a(stack, false);
        if (slot != -1) {
            a(new a(slot, toSlot, bypass, false));
        }
    }

    private void a(a task) {
        if (task.a() != -1 && task.b() != -1) {
            if (this.b.isEmpty() && task.c > 0) {
                Delta.h().d().v().c().a(task.c);
            }
            this.b.add(task);
        }
    }

    private int a(int slot) {
        return (slot < 0 || slot > 8) ? slot : slot + 36;
    }

    private int a(int original, int normalized) {
        if (original == 40 || original == 45 || normalized == 45) {
            return 40;
        }
        if (normalized < 36 || normalized > 44) {
            return -1;
        }
        return normalized - 36;
    }

    static final class a {
        private final int a;
        private final int b;
        final int c;
        private final boolean d;

        @Generated
        public int a() {
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
        public boolean d() {
            return this.d;
        }

        public a(int from, int to, int bypass, boolean raw) {
            this.a = from;
            this.b = to;
            this.c = bypass;
            this.d = raw;
        }
    }
}


