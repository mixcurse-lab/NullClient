package platform.client.features.commands;

import platform.api.utils.account.FriendConstructor;
import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.player.ServerUtil;

import platform.api.command.BaseCommand;
import platform.api.command.Command;
import platform.api.event.interfaces.EventTarget;
import platform.api.event.events.client.TickEvent;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.client.multiplayer.PlayerInfo;

@Command(a = "ccc")
public class CCCommand extends BaseCommand {
    private int c;

    @Override
    public void a(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.executes(context -> {
            if (ServerUtil.a.a() || ServerUtil.d.a()) {
                StringBuilder name = new StringBuilder();
                for (int i = 0; i < 3 + ((int) (Math.random() * 3.0d)); i++) {
                    name.append("абвгдежзийклмнопрстуфхцчшщъыьэюяАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".charAt((int) (Math.random() * ((double) "абвгдежзийклмнопрстуфхцчшщъыьэюяАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".length()))));
                }
                aM_.player.connection.sendChat("/clan create " + String.valueOf(name));
                this.c = aM_.player.tickCount + 2;
                return 1;
            }
            return 1;
        });
    }

    @EventTarget
    public void a(TickEvent eventTick) {
        if (this.c >= aM_.player.tickCount) {
            for (FriendConstructor constructor : Delta.h().d().e().e()) {
                PlayerInfo entry = (PlayerInfo) aM_.player.connection.getListedOnlinePlayers().stream().filter(listEntry -> {
                    return listEntry.getProfile().name().equalsIgnoreCase(constructor.a());
                }).findFirst().orElse(null);
                if (entry != null && !constructor.a().equals(aM_.getUser().getName())) {
                    aM_.player.connection.sendChat("/clan invite " + constructor.a());
                }
            }
            this.c = -3;
        }
    }
}



