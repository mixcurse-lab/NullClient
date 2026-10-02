package platform.client.features.commands;

import platform.client.utils.other.BooleanUtils;
import platform.api.module.Interface;

import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.text.ChatUtil;

import platform.api.command.BaseCommand;
import platform.api.command.Command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.Generated;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.Vec3;

@Command(a = "gps")
public class GPSCommand extends BaseCommand {
    private Vec3 c;

    @Generated
    public Vec3 c() {
        return this.c;
    }

    @Override
    public void a(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(a(BooleanUtils.c).executes(context -> {
            if (this.c == null) {
                ChatUtil.a((Object) "GPS-метка сейчас отсутствует");
                return 1;
            }
            this.c = null;
            ChatUtil.a((Object) "GPS-метка больше не отображается");
            return 1;
        })).then(e("x").executes(context2 -> {
            ChatUtil.a((Object) "Использование: .gps <x> <z>, .gps <x> <y> <z> или .gps off");
            return 1;
        }).then(e("y или z").executes(context3 -> {
            a(new Vec3(b((CommandContext<CommandSourceStack>) context3, "x"), aM_.player.getY(), b((CommandContext<CommandSourceStack>) context3, "y или z")));
            return 1;
        }).then(e("z").executes(context4 -> {
            a(new Vec3(b((CommandContext<CommandSourceStack>) context4, "x"), b((CommandContext<CommandSourceStack>) context4, "y или z"), b((CommandContext<CommandSourceStack>) context4, "z")));
            return 1;
        })))).then(a("event").executes(context5 -> {
            Delta.h().d().u().c().a(WayCommand.a.GPS);
            aM_.player.connection.sendCommand("event delay");
            return 1;
        })).executes(context6 -> {
            ChatUtil.a((Object) "Использование: .gps <x> <z>, .gps <x> <y> <z> или .gps off");
            return 1;
        });
    }

    public void a(Vec3 pos) {
        this.c = pos;
        ChatUtil.a((Object) ("GPS-метка установлена: " + ((int) pos.x) + ", " + ((int) pos.y) + ", " + ((int) pos.z)));
    }
}



