package platform.client.features.modules.misc;

import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.PacketEvent;

import platform.api.module.setting.BooleanSetting;
import platform.inject.accessors.ConnectScreenAccessor;

import java.util.UUID;

import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action;

@ModuleRegister(a = "RP Spoofs", b = "Сервер думает что ресурс пак загружен, хотя клиент его даже не скачивает", c = Category.Misc)
public class RPSpoofs extends Module {
    private final BooleanSetting b = new BooleanSetting("Только обязательные паки", false);
    private final BooleanSetting c = new BooleanSetting("Спуфить статусы загрузки", true);

    public RPSpoofs() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (!event.c() || !(event.d() instanceof ClientboundResourcePackPushPacket packet)) {
            return;
        }
        if (this.b.c() && !packet.required()) {
            return;
        }
        event.a(true);
        Connection connection = getConnection();
        if (connection == null) {
            return;
        }
        if (this.c.c()) {
            UUID id = packet.id();
            connection.send(new ServerboundResourcePackPacket(id, Action.ACCEPTED));
            connection.send(new ServerboundResourcePackPacket(id, Action.DOWNLOADED));
            connection.send(new ServerboundResourcePackPacket(id, Action.SUCCESSFULLY_LOADED));
        }
    }

    private Connection getConnection() {
        if (aM_.player != null && aM_.player.connection != null) {
            return aM_.player.connection.getConnection();
        }
        Screen screen = aM_.gui.screen();
        if (screen instanceof ConnectScreen) {
            return ((ConnectScreenAccessor) screen).getConnection();
        }
        return null;
    }
}
