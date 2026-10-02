package platform.client.features.modules.movement;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import platform.api.module.Category;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import static platform.api.module.Interface.aM_;
import platform.api.event.interfaces.EventTarget;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;
import platform.api.module.setting.BindSetting;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;
import platform.client.Delta;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.rotation.Look;
import platform.client.utils.rotation.Rotation;
import platform.client.utils.text.ChatUtil;
import platform.inject.accessors.ConnectionAccessor;

@ModuleRegister(a = "Wind Boost", b = "Двойной бросок ветрового заряда с удержанием 1-го пакета в буфере и одновременным выстрелом", c = Category.Movement)
public class WindBoost extends Module {
    private final ModeSetting mode = new ModeSetting("Режим", "При включении", "При включении", "Прыжок", "По бинду", "Авто");
    private final BindSetting bindKey = new BindSetting("Кнопка", -1).a(this::tryStartBoost).a(() -> {
        return Boolean.valueOf(this.mode.l("По бинду"));
    });
    private final SliderSetting pingDelay = new SliderSetting("Пинг задержка (мс)", 1000.0f, 500.0f, 2000.0f, 50.0f);
    private final SliderSetting burstDelay = new SliderSetting("Задержка 2-го заряда (тики)", 2.0f, 1.0f, 6.0f, 1.0f);
    private final BooleanSetting doubleCharge = new BooleanSetting("Двойной бросок (2 заряда)", true);
    private final BooleanSetting takeMace = new BooleanSetting("Брать булаву в воздухе", true);
    private final BooleanSetting searchInventory = new BooleanSetting("Искать в инвентаре", true);
    private final BooleanSetting swingArm = new BooleanSetting("Взмах рукой", true);
    private final BooleanSetting silentRotation = new BooleanSetting("Тихий поворот", true);

    private record DelayedPacket(Packet<?> packet, long sendTime) {
    }

    private final Queue<DelayedPacket> delayedNetworkPackets = new ConcurrentLinkedQueue<>();
    private final List<Packet<?>> bufferedChargePackets = new ArrayList<>();

    private long lastUseTime = 0L;
    private int phase = 0;
    private int phaseTimer = 0;
    private int originalSlot = -1;
    private int windSlot = -1;
    private boolean isPingSpoofing = false;
    private boolean isBufferingCharge = false;
    private long spoofStartTime = 0L;

    @Generated
    public ModeSetting r() { return this.mode; }

    @Generated
    public int s() { return this.phase; }

    @Override
    public void b() {
        super.b();
        if (aM_.player == null || aM_.level == null) {
            return;
        }
        if (this.mode.l("При включении")) {
            startBoost();
        }
    }

    @Override
    public void c() {
        super.c();
        flushAllPackets();
        resetBoost();
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (aM_.player == null || !event.b()) {
            return;
        }
        Packet<?> packet = event.d();
        if (this.isBufferingCharge && (packet instanceof ServerboundUseItemPacket || packet instanceof ServerboundSetCarriedItemPacket)) {
            this.bufferedChargePackets.add(packet);
            event.a(true);
            return;
        }
        if (this.isPingSpoofing && (packet instanceof ServerboundKeepAlivePacket || packet instanceof ServerboundPongPacket || packet instanceof ServerboundMovePlayerPacket)) {
            this.delayedNetworkPackets.add(new DelayedPacket(packet, System.currentTimeMillis() + (long) this.pingDelay.c().floatValue()));
            event.a(true);
        }
    }

    @EventTarget
    public void a(TickEvent event) {
        long now = System.currentTimeMillis();

        while (!this.delayedNetworkPackets.isEmpty()) {
            DelayedPacket first = this.delayedNetworkPackets.peek();
            if (first != null && now >= first.sendTime()) {
                this.delayedNetworkPackets.poll();
                sendSilent(first.packet());
            } else {
                break;
            }
        }

        if (aM_.player == null || aM_.level == null || aM_.gameMode == null) {
            flushAllPackets();
            resetBoost();
            return;
        }

        if (this.phase > 0 && this.silentRotation.c().booleanValue()) {
            Delta.h().d().k().a(new Rotation(Look.b(), 90.0f), 180.0f, 1, 3);
        }

        if (this.phase == 0) {
            if (this.isPingSpoofing && now - this.spoofStartTime > (long) this.pingDelay.c().floatValue() + 500L) {
                this.isPingSpoofing = false;
                flushAllPackets();
            }
            if (this.mode.l("Прыжок")) {
                if (aM_.options.keyJump.isDown() && aM_.player.onGround() && now - this.lastUseTime >= 500L) {
                    startBoost();
                }
            } else if (this.mode.l("Авто")) {
                if (aM_.player.onGround() && now - this.lastUseTime >= 500L) {
                    startBoost();
                } else if (!aM_.player.onGround() && aM_.player.getDeltaMovement().y > 0.05d && now - this.lastUseTime >= 600L) {
                    startBoost();
                }
            }
            return;
        }

        this.phaseTimer++;
        int maxDelay = this.burstDelay.c().intValue();

        switch (this.phase) {
            case 1 -> {
                if (aM_.player.onGround()) {
                    aM_.player.jumpFromGround();
                }
                equipWindSlot();
                this.phase = 2;
                this.phaseTimer = 0;
            }
            case 2 -> {
                if (this.phaseTimer >= 1) {
                    this.isBufferingCharge = true;
                    throwWindCharge();
                    this.phase = 3;
                    this.phaseTimer = 0;
                }
            }
            case 3 -> {
                if (this.phaseTimer >= maxDelay) {
                    this.isBufferingCharge = false;
                    for (Packet<?> p : this.bufferedChargePackets) {
                        sendSilent(p);
                    }
                    this.bufferedChargePackets.clear();
                    if (this.doubleCharge.c().booleanValue()) {
                        equipWindSlot();
                        throwWindCharge();
                    }
                    this.phase = 4;
                    this.phaseTimer = 0;
                }
            }
            case 4 -> {
                if (this.phaseTimer >= 2) {
                    int targetSlot = -1;
                    if (this.takeMace.c().booleanValue()) {
                        targetSlot = InventoryUtil.a(Items.MACE, true);
                    }
                    if (targetSlot == -1) {
                        targetSlot = this.originalSlot;
                    }
                    if (targetSlot != -1) {
                        Inventory inv = aM_.player.getInventory();
                        inv.setSelectedSlot(targetSlot);
                        aM_.player.connection.send(new ServerboundSetCarriedItemPacket(inv.getSelectedSlot()));
                    }
                    this.phase = 0;
                    this.phaseTimer = 0;
                    if (this.mode.l("При включении")) {
                        a(false);
                    }
                }
            }
        }
    }

    private void tryStartBoost() {
        startBoost();
    }

    private void startBoost() {
        if (aM_.player == null || aM_.level == null || this.phase != 0) {
            return;
        }
        int slot = findWindChargeSlot();
        if (slot == -1) {
            ChatUtil.a("&cWind Boost&7 - заряды ветра не найдены");
            return;
        }
        this.isPingSpoofing = true;
        this.spoofStartTime = System.currentTimeMillis();
        this.originalSlot = aM_.player.getInventory().getSelectedSlot();
        this.windSlot = slot;
        this.phase = 1;
        this.phaseTimer = 0;
        this.isBufferingCharge = false;
        this.bufferedChargePackets.clear();
    }

    private void equipWindSlot() {
        int current = findWindChargeSlot();
        if (current != -1) {
            this.windSlot = current;
        }
        Inventory inv = aM_.player.getInventory();
        if (this.windSlot >= 0 && this.windSlot <= 8) {
            inv.setSelectedSlot(this.windSlot);
            aM_.player.connection.send(new ServerboundSetCarriedItemPacket(inv.getSelectedSlot()));
        } else if (this.windSlot >= 9 && this.windSlot <= 35) {
            aM_.gameMode.handleContainerInput(aM_.player.inventoryMenu.containerId, this.windSlot, this.originalSlot, ContainerInput.SWAP, aM_.player);
        }
    }

    private void throwWindCharge() {
        InteractionHand hand = this.windSlot == 40 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        aM_.gameMode.useItem(aM_.player, hand);
        if (this.swingArm.c().booleanValue()) {
            aM_.player.swing(hand);
        }
        this.lastUseTime = System.currentTimeMillis();
    }

    private void sendSilent(Packet<?> packet) {
        ((ConnectionAccessor) aM_.player.connection.getConnection()).sendWithoutEvent(packet, null, true);
    }

    private void flushAllPackets() {
        for (Packet<?> p : this.bufferedChargePackets) {
            sendSilent(p);
        }
        this.bufferedChargePackets.clear();
        while (!this.delayedNetworkPackets.isEmpty()) {
            DelayedPacket delayedPacket = this.delayedNetworkPackets.poll();
            if (delayedPacket != null) {
                sendSilent(delayedPacket.packet());
            }
        }
    }

    private void resetBoost() {
        this.phase = 0;
        this.phaseTimer = 0;
        this.originalSlot = -1;
        this.windSlot = -1;
        this.isPingSpoofing = false;
        this.isBufferingCharge = false;
        this.bufferedChargePackets.clear();
    }

    private int findWindChargeSlot() {
        if (aM_.player == null) {
            return -1;
        }
        if (aM_.player.getOffhandItem().is(Items.WIND_CHARGE)) {
            return 40;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack stack = aM_.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(Items.WIND_CHARGE)) {
                return i;
            }
        }
        if (this.searchInventory.c().booleanValue()) {
            for (int i = 9; i < 36; i++) {
                ItemStack stack = aM_.player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.is(Items.WIND_CHARGE)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
