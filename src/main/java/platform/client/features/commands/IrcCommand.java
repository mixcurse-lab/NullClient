package platform.client.features.commands;

import platform.api.command.BaseCommand;
import platform.api.command.Command;
import platform.client.processors.FlaskProcessor;
import platform.client.utils.text.ChatUtil;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

/**
 * .irc <сообщение> — отправляет сообщение в IRC чат через Flask сервер.
 */
@Command(a = "irc")
public class IrcCommand extends BaseCommand {

    @Override
    public void a(LiteralArgumentBuilder<CommandSourceStack> builder) {
        // .irc <сообщение>
        builder.then(c("сообщение").executes(ctx -> {
            String msg = a(ctx, "сообщение");
            FlaskProcessor fp = FlaskProcessor.get();
            if (fp != null) {
                fp.sendChat(msg);
            } else {
                ChatUtil.a((Object) "IRC недоступен — FlaskProcessor не запущен.");
            }
            return 1;
        }));

        // .irc (без аргументов)
        builder.executes(ctx -> {
            ChatUtil.a((Object) "Использование: .irc <сообщение>");
            return 1;
        });
    }
}
