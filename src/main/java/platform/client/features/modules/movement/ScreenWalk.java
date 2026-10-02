package platform.client.features.modules.movement;

import static platform.api.module.Interface.aM_;

import platform.api.module.Category;
import platform.client.Delta;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.handlers.StopHandler;
import platform.api.module.setting.ModeSetting;
import platform.client.utils.player.MoveUtil;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import platform.inject.accessors.ConnectionAccessor;

@ModuleRegister(a = "Screen Walk", b = "Позволяет двигаться с открытым контейнером, задерживая пакеты инвентаря", c = Category.Movement)
public class ScreenWalk extends Module {
    private final ModeSetting mode = new ModeSetting("Обход перемещения предметов", "Ускоренный", "Ускоренный", "Медленный");
    private final List<QueuedPacket> queuedPackets = new ArrayList();
    private boolean awaitingServerClose = false;

    public ScreenWalk() {
        a(this.mode);
    }

    @EventTarget(a = 0)
    public void a(PacketEvent event) {
        StopHandler stopHandler = Delta.h().d().v().c();
        if (event.b()) {
            if (aM_.gui.screen() instanceof InventoryScreen) {
                Packet<?> rawPacket = event.d();
                if (rawPacket instanceof ServerboundContainerClickPacket clickPacket) {
                    if (MoveUtil.a()) {
                        boolean shulker;
                        if (clickPacket.buttonNum() == 1 && aM_.player.containerMenu.getCarried().getItem() instanceof BlockItem blockItem) {
                            shulker = blockItem.getBlock() instanceof ShulkerBoxBlock;
                        } else {
                            shulker = false;
                        }
                        if (shulker) {
                            stopHandler.a(2);
                        }
                        this.queuedPackets.add(new QueuedPacket(event.d(), this.mode.l("Медленный") ? this.queuedPackets.isEmpty() ? 1 : this.queuedPackets.size() + 1 : 2, shulker));
                        event.a(true);
                    }
                }
            }
            if (event.d() instanceof ServerboundContainerClosePacket) {
                if (MoveUtil.a() && (aM_.gui.screen() instanceof InventoryScreen)) {
                    event.a(true);
                    for (QueuedPacket queuedPacket : this.queuedPackets) {
                        stopHandler.a(queuedPacket.delay());
                    }
                }
                this.awaitingServerClose = false;
            }
        }
        if (event.c() && this.mode.l("Медленный")) {
            if (event.d() instanceof ClientboundOpenScreenPacket) {
                this.awaitingServerClose = true;
            }
            if (event.d() instanceof ClientboundContainerClosePacket) {
                this.awaitingServerClose = false;
            }
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        if (!this.awaitingServerClose && aM_.gui.screen() != null && !(aM_.gui.screen() instanceof ChatScreen) && !(aM_.gui.screen() instanceof SignEditScreen) && !(aM_.gui.screen() instanceof AnvilScreen) && !(aM_.gui.screen() instanceof CreativeModeInventoryScreen)) {
            for (KeyMapping keyMapping : new KeyMapping[]{aM_.options.keyUp, aM_.options.keyDown, aM_.options.keyLeft, aM_.options.keyRight, aM_.options.keyJump}) {
                keyMapping.setDown(InputConstants.isKeyDown(aM_.getWindow(), keyMapping.getDefaultKey().getValue()));
            }
        }
        if (!MoveUtil.a() && !this.queuedPackets.isEmpty()) {
            ConnectionAccessor connection = (ConnectionAccessor) aM_.player.connection.getConnection();
            if (this.mode.l("Медленный")) {
                connection.sendWithoutEvent(((QueuedPacket) this.queuedPackets.removeFirst()).packet(), null, true);
            } else {
                this.queuedPackets.forEach(queuedPacket -> {
                    connection.sendWithoutEvent(queuedPacket.packet(), null, true);
                });
                this.queuedPackets.clear();
            }
            if (this.queuedPackets.isEmpty() && aM_.gui.screen() == null) {
                connection.sendWithoutEvent(new ServerboundContainerClosePacket(aM_.player.containerMenu.containerId), null, true);
            }
        }
    }

    static final class QueuedPacket {
        private final Packet<?> packet;
        private final int delay;
        private final boolean isShulker;

        QueuedPacket(Packet<?> packet, int delay, boolean isShulker) {
            this.packet = packet;
            this.delay = delay;
            this.isShulker = isShulker;
        }

        public Packet<?> packet() {
            return this.packet;
        }

        public int delay() {
            return this.delay;
        }

        public boolean isShulker() {
            return this.isShulker;
        }
    }
}


