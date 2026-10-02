package platform.client.features.modules.misc;

import platform.client.ui.screen.GUIScreen;
import platform.inject.invokers.MultiPlayerGameModeInvoker;
import static platform.api.module.Interface.aM_;
import platform.api.module.Module;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.player.InventoryUtil;
import platform.client.utils.player.ServerUtil;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Interface;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.PacketEvent;
import platform.api.event.events.client.TickEvent;

import platform.client.utils.timer.CounterUtil;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.SliderSetting;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import lombok.Generated;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;

@ModuleRegister(a = "Server Joiner", b = "Автоматически присоединяется к выбранному серверу", c = Category.Misc)
public class ServerJoiner extends Module implements Interface {
    private final ModeSetting b = new ModeSetting("Выбор сервера", "SpookyTime", "SpookyTime", "ReallyWorld");
    private final SliderSetting c = (SliderSetting) new SliderSetting("Укажите номер мира (1-54)", 1.0f, 1.0f, 54.0f, 1.0f).a(() -> {
        return Boolean.valueOf(this.b.l("ReallyWorld"));
    });
    private final CounterUtil d = new CounterUtil();
    private int e = -1;

    @Generated
    public ModeSetting q() {
        return this.b;
    }

    @Generated
    public SliderSetting r() {
        return this.c;
    }

    @Generated
    public CounterUtil s() {
        return this.d;
    }

    @Generated
    public int t() {
        return this.e;
    }

    public ServerJoiner() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(TickEvent e) {
        if (aM_.player == null || aM_.level == null) return;
        if (aM_.gui.screen() instanceof GUIScreen) return;

        if (this.b.l("SpookyTime")) {
            if (ServerUtil.a().contains("Хаб")) {
                int compassSlot = InventoryUtil.a(Items.COMPASS, true);
                if (compassSlot >= 0 && compassSlot <= 8 && aM_.gui.screen() == null) {
                    aM_.player.connection.send(new ServerboundSetCarriedItemPacket(compassSlot));
                    ((MultiPlayerGameModeInvoker) aM_.gameMode).invokeStartPrediction(aM_.level, sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, aM_.player.getYRot(), aM_.player.getXRot()));
                }
                if (this.e != -1) {
                    aM_.player.connection.send(new ServerboundContainerClickPacket(this.e, 0, (short)13, (byte)0, ContainerInput.PICKUP, Int2ObjectMaps.emptyMap(), HashedStack.EMPTY));
                    this.e = -1;
                }
                return;
            }
            if (!ServerUtil.a().isEmpty() && !ServerUtil.a().contains("Режим: Хаб # ")) {
                ChatUtil.a((Object) "Вы находитесь не в хабе SpookyTime, а значит модуль выключается!");
                a();
            }
            return;
        }
        if (this.b.l("ReallyWorld")) {
            if (ServerUtil.a().isEmpty()) {
                int compassSlot2 = InventoryUtil.a(Items.COMPASS, true);
                if (compassSlot2 >= 0 && compassSlot2 <= 8 && aM_.gui.screen() == null) {
                    aM_.player.connection.send(new ServerboundSetCarriedItemPacket(compassSlot2));
                    ((MultiPlayerGameModeInvoker) aM_.gameMode).invokeStartPrediction(aM_.level, sequence2 -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence2, aM_.player.getYRot(), aM_.player.getXRot()));
                }
                if (aM_.gui.screen() instanceof ContainerScreen screen) {
                    if (screen.getTitle().getString().contains("» Выбор сервера")) {
                        aM_.player.connection.send(new ServerboundContainerClickPacket(screen.getMenu().containerId, 0, (short)21, (byte)0, ContainerInput.PICKUP, Int2ObjectMaps.emptyMap(), HashedStack.EMPTY));
                    }
                    for (int i = 0; i < screen.getMenu().slots.size(); i++) {
                        Slot slot = screen.getMenu().slots.get(i);
                        if (slot.getItem().getHoverName().getString().contains("ГРИФ #" + this.c.c().intValue() + " (1.16.5+)") && screen.getTitle().getString().contains("Выбор мира грифа ")) {
                            if (this.d.a(5500L)) {
                                aM_.player.connection.send(new ServerboundContainerClickPacket(screen.getMenu().containerId, 0, (short)slot.index, (byte)0, ContainerInput.PICKUP, Int2ObjectMaps.emptyMap(), HashedStack.EMPTY));
                                this.d.b();
                                return;
                            }
                            return;
                        }
                    }
                }
                return;
            }
            ChatUtil.a((Object) "Вы находитесь не в лобби ReallyWorld, а значит модуль выключается!");
            a();
        }
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (this.b.l("SpookyTime") && event.c() && event.d() instanceof ClientboundOpenScreenPacket packet) {
            if (packet.getTitle().getString().contains("☫ Выберите режим:")) {
                this.e = packet.getContainerId();
            }
            event.a(true);
        }
    }
}

