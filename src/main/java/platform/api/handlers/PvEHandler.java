package platform.api.handlers;

import platform.api.handlers.Handler_2;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.player.MoveUtil;
import platform.client.utils.player.ServerUtil;

import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.event.events.player.InputEvent;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.handlers.BaseHandler;
import platform.api.handlers.InventoryHandler;
import platform.client.utils.rotation.Rotation;

import java.util.ArrayDeque;
import java.util.Deque;
import lombok.Generated;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;

@Handler_2
public class PvEHandler extends BaseHandler implements Interface {
    private final Deque<b> b = new ArrayDeque();

    interface b {
        boolean a();
    }

    @Generated
    public Deque<b> a() {
        return this.b;
    }

    @EventTarget
    public void a(TickEvent event) {
        if (!this.b.isEmpty() && this.b.peek().a()) {
            this.b.poll();
        }
    }

    @EventTarget(a = 4)
    public void a(InputEvent event) {
        if (!this.b.isEmpty()) {
            MoveUtil.b(event);
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (this.b.isEmpty() || !event.c()) {
            return;
        }
        ClientboundDisguisedChatPacket class_7439VarD = (ClientboundDisguisedChatPacket) event.d();
        if (class_7439VarD instanceof ClientboundDisguisedChatPacket) {
            ClientboundDisguisedChatPacket gameMsg = class_7439VarD;
            if (gameMsg.message().getString().equals("Данная команда недоступна в режиме AFK")) {
                Delta.h().d().v().g().a(7);
            }
        }
    }

    public boolean a(ItemStack tool, double startPct, double endPct) {
        ItemStack class_1799VarMethod_7972;
        for (b task : this.b) {
            if (task instanceof c) {
                return false;
            }
        }
        if (a(tool) > startPct) {
            return true;
        }
        if (!InventoryUtil.a(tool, aM_.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING), 1)) {
            ChatUtil.a((Object) "На предмете нету починки, отмена.");
            return true;
        }
        ItemStack main = aM_.player.getMainHandItem();
        ItemStack off = aM_.player.getOffhandItem();
        if (main.isEmpty() || ItemStack.isSameItemSameComponents(main, tool)) {
            class_1799VarMethod_7972 = (off.isEmpty() || off.getItem() == tool.getItem()) ? ItemStack.EMPTY : off.copy();
        } else {
            class_1799VarMethod_7972 = main.copy();
        }
        ItemStack restore = class_1799VarMethod_7972;
        this.b.add(new c(tool.getItem(), endPct, aM_.player.getInventory().getSelectedSlot(), restore));
        return false;
    }

    static double a(ItemStack stack) {
        if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
            return 100.0d;
        }
        return (1.0d - (((double) stack.getDamageValue()) / ((double) stack.getMaxDamage()))) * 100.0d;
    }

    static final class c implements Interface, b {
        private final Item b;
        private final double c;
        private final int d;
        private final ItemStack e;
        private int f;
        private int g;

        c(Item tool, double endPct, int toolBarSlot, ItemStack restore) {
            this.b = tool;
            this.c = endPct;
            this.d = toolBarSlot;
            this.e = restore;
        }

        @Override
        public boolean a() {
            InventoryHandler handler = Delta.h().d().v().a();
            ItemStack offHand = aM_.player.getOffhandItem();
            ItemStack mainHand = aM_.player.getMainHandItem();
            switch (this.f) {
                case 0:
                    if (aM_.gui.screen() != null) {
                        aM_.gui.screen().onClose();
                    }
                    int i = this.g + 1;
                    this.g = i;
                    if (i >= 3 && handler.a().isEmpty()) {
                        handler.a(this.d, 40, 1);
                        this.f = 1;
                        return false;
                    }
                    return false;
                case 1:
                    if (!handler.a().isEmpty() || offHand.getItem() != this.b) {
                        return false;
                    }
                    if (PvEHandler.a(offHand) >= this.c) {
                        this.f = 3;
                        return false;
                    }
                    int onBar = InventoryUtil.a(Items.EXPERIENCE_BOTTLE, true);
                    if (onBar != -1) {
                        aM_.player.getInventory().setSelectedSlot(onBar);
                        this.f = 2;
                        return false;
                    }
                    int inStorage = InventoryUtil.b(Items.EXPERIENCE_BOTTLE);
                    if (inStorage != -1) {
                        handler.a(inStorage, this.d, 1);
                        this.f = 2;
                        return false;
                    }
                    Delta.h().d().v().i().a().addFirst(new a(Items.EXPERIENCE_BOTTLE, 128, 2000));
                    return false;
                case 2:
                    if (!handler.a().isEmpty() || offHand.getItem() != this.b) {
                        return false;
                    }
                    if (PvEHandler.a(offHand) >= this.c) {
                        this.g = 0;
                        this.f = 3;
                        return false;
                    }
                    if (mainHand.isEmpty()) {
                        this.f = 1;
                        return false;
                    }
                    if (mainHand.getItem() != Items.EXPERIENCE_BOTTLE) {
                        int bar = InventoryUtil.a(Items.EXPERIENCE_BOTTLE, true);
                        aM_.player.getInventory().setSelectedSlot(bar != -1 ? bar : this.d);
                        return false;
                    }
                    Delta.h().d().k().a(new Rotation(aM_.player.getYRot(), 90.0f), 360.0f, 1, 1);
                    aM_.gameMode.useItem(aM_.player, InteractionHand.MAIN_HAND);
                    return false;
                case 3:
                    int i2 = this.g + 1;
                    this.g = i2;
                    if (i2 >= 3) {
                        handler.a(this.d, 40, 1);
                        this.g = 0;
                        this.f = 4;
                        return false;
                    }
                    return false;
                case 4:
                    int i3 = this.g + 1;
                    this.g = i3;
                    if (i3 >= 3) {
                        if (this.e.isEmpty() || InventoryUtil.a(this.e, false) == -1) {
                            return true;
                        }
                        handler.a(InventoryUtil.a(this.e, false), 40, 1);
                        return true;
                    }
                    return false;
                default:
                    return false;
            }
        }
    }

    static final class a implements Interface, b {
        final Item b;
        final int c;
        final int d;
        int e;
        int f;

        a(Item targetItem, int need, int priceLimit) {
            this.b = targetItem;
            this.c = need;
            this.d = priceLimit;
        }

        @Override
        public boolean a() {
            switch (this.e) {
                case 0:
                    if (aM_.gui.screen() != null) {
                        aM_.gui.screen().onClose();
                    }
                    int i = this.f + 1;
                    this.f = i;
                    if (i >= 3) {
                        aM_.player.connection.sendChat("/ah search " + (this.b == Items.EXPERIENCE_BOTTLE ? "Опыт" : new ItemStack(this.b).getHoverName().getString()));
                        this.e = 1;
                        this.f = 0;
                        return false;
                    }
                    return false;
                case 1:
                    ContainerScreen class_476Var = (ContainerScreen) aM_.gui.screen();
                    if (!(class_476Var instanceof ContainerScreen)) {
                        int i2 = this.f + 1;
                        this.f = i2;
                        if (i2 > 20) {
                            ChatUtil.a((Object) "Аукцион не открылся, повторяю.");
                            this.f = 0;
                            this.e = 0;
                            return false;
                        }
                        return false;
                    }
                    ContainerScreen screen = class_476Var;
                    if (screen.getTitle().getString().startsWith("☃") || screen.getTitle().getString().startsWith("0A2z")) {
                        this.e = 2;
                        this.f = 0;
                        return false;
                    }
                    return false;
                case 2:
                    int i3 = this.f + 1;
                    this.f = i3;
                    if (i3 >= 15) {
                        this.e = 3;
                        this.f = 0;
                        return false;
                    }
                    return false;
                case 3:
                    ContainerScreen class_476Var2 = (ContainerScreen) aM_.gui.screen();
                    if (!(class_476Var2 instanceof ContainerScreen)) {
                        this.f = 0;
                        this.e = 0;
                        return false;
                    }
                    ContainerScreen screen2 = class_476Var2;
                    if (InventoryUtil.a(this.b) >= this.c) {
                        int i4 = this.f + 1;
                        this.f = i4;
                        if (i4 >= 6) {
                            aM_.gui.screen().onClose();
                            return true;
                        }
                        return false;
                    }
                    this.f = 0;
                    if (aM_.player.tickCount % 7 == 0) {
                        Slot offer = null;
                        for (int i5 = 0; i5 < screen2.getMenu().slots.size() - 36; i5++) {
                            Slot slot = (Slot) screen2.getMenu().slots.get(i5);
                            if (!slot.getItem().isEmpty() && slot.getItem().getItem() == this.b && ServerUtil.a.a(slot.getItem()) > 0 && ServerUtil.a.a(slot.getItem()) <= this.d && (offer == null || ServerUtil.a.a(slot.getItem()) < ServerUtil.a.a(offer.getItem()))) {
                                offer = slot;
                            }
                        }
                        if (offer == null) {
                            aM_.gameMode.handleContainerInput(screen2.getMenu().containerId, 50, 0, ContainerInput.QUICK_MOVE, aM_.player);
                            return false;
                        }
                        aM_.gameMode.handleContainerInput(screen2.getMenu().containerId, offer.index, 0, ContainerInput.QUICK_MOVE, aM_.player);
                        return false;
                    }
                    return false;
                default:
                    return false;
            }
        }
    }
}



