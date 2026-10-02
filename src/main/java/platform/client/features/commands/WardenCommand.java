package platform.client.features.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import platform.api.command.BaseCommand;
import platform.api.command.Command;
import platform.client.Delta;
import platform.client.features.modules.misc.AutoWarden;
import platform.client.utils.player.ServerUtil;
import platform.client.utils.text.ChatUtil;

@Command(a = "warden")
public class WardenCommand extends BaseCommand {
    @Override
    public void a(LiteralArgumentBuilder<CommandSourceStack> builder) {
        AutoWarden module = Delta.h().d().t().autoWarden();
        builder.then(a("add").executes(context -> add(module, ServerUtil.a.d()))
                .then(e("анархия").executes(context -> add(module, b(context, "анархия")))))
                .then(a("remove").executes(context -> {
                    ChatUtil.a("Использование: .warden remove <анархия>");
                    return 1;
                }).then(e("анархия").suggests((context, suggestions) -> {
                    for (int anarchy : module.getAnarchyList()) {
                        suggestions.suggest(anarchy);
                    }
                    return suggestions.buildFuture();
                }).executes(context -> remove(module, b(context, "анархия")))))
                .then(a("list").executes(context -> {
                    if (module.getAnarchyList().isEmpty()) {
                        ChatUtil.a("Список анархий для зелий пуст.");
                        return 1;
                    }
                    ChatUtil.a("Анархии для поиска зелий (" + module.getAnarchyList().size() + "):");
                    for (int anarchy : module.getAnarchyList()) {
                        ChatUtil.a("— анархия-" + anarchy);
                    }
                    return 1;
                }))
                .then(a("clear").executes(context -> {
                    int removed = module.getAnarchyList().size();
                    module.getAnarchyList().clear();
                    ChatUtil.a("Удалено анархий из списка: " + removed);
                    return 1;
                }))
                .executes(context -> {
                    ChatUtil.a("Использование: .warden <add [анархия]|remove <анархия>|list|clear>");
                    return 1;
                });
    }

    private int add(AutoWarden module, int anarchy) {
        if (anarchy < 0 || anarchy > 999) {
            ChatUtil.a("Укажите анархию от 0 до 999 или выполните команду на анархии.");
            return 1;
        }
        if (!module.addAnarchy(anarchy)) {
            ChatUtil.a("Анархия-" + anarchy + " уже есть в списке.");
            return 1;
        }
        ChatUtil.a("Анархия-" + anarchy + " добавлена в список поиска зелий.");
        return 1;
    }

    private int remove(AutoWarden module, int anarchy) {
        if (!module.removeAnarchy(anarchy)) {
            ChatUtil.a("Анархия-" + anarchy + " не найдена в списке.");
            return 1;
        }
        ChatUtil.a("Анархия-" + anarchy + " удалена из списка.");
        return 1;
    }
}
