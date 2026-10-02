package platform.client.features.modules.player;

import platform.client.utils.text.StringUtils;
import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.api.module.Module;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.ModuleRegister;
import platform.api.event.events.client.PacketEvent;

import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.MultiModeSetting;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

@ModuleRegister(a = "Auto Accept", b = "Автоматически принимает выбранные запросы", c = Category.Player)
public class AutoAccept extends Module {
    public final MultiModeSetting b = new MultiModeSetting("Принимать запросы", new BooleanSetting("В клановую команду", true), new BooleanSetting("Телепортации", true));
    private final BooleanSetting c = new BooleanSetting("Принимать только друзей", true);
    private final List<String> d = List.of("просит телепортироваться", "хочет телепортироваться", "Заявка буудет автоматически отменена через", "просит телепортироваться к Вам.", "120 секунд", "has requested to teleport", "teleport to you", "This request will timeout after", "120 seconds");
    private final List<String> e = List.of("приглашает вас в клан", "invites you to the clan");

    public AutoAccept() {
        a(this.b, this.c);
    }

    @EventTarget
    public void a(PacketEvent event) {
        if (event.c() && event.d() instanceof ClientboundSystemChatPacket s2CPacket) {
            String chat = s2CPacket.content().getString().toLowerCase();
                if (this.c.c().booleanValue() && Delta.h().d().e().a().stream().noneMatch(friend -> {
                    return chat.contains(friend.a().toLowerCase());
                })) {
                    return;
                }
                if (this.b.a("Телепортации").c().booleanValue()) {
                    Stream<String> stream = this.d.stream();
                    Objects.requireNonNull(chat);
                    if (stream.anyMatch((v1) -> {
                        return chat.contains(v1);
                    })) {
                        aM_.player.connection.sendCommand("tpaccept");
                    }
                }
                if (this.b.a("В клановую команду").c().booleanValue()) {
                    Stream<String> stream2 = this.e.stream();
                    Objects.requireNonNull(chat);
                    if (stream2.anyMatch((v1) -> {
                        return chat.contains(v1);
                    })) {
                        aM_.player.connection.sendCommand("clan accept " + chat.split(StringUtils.a)[1]);
                    }
                }
        }
    }
}





