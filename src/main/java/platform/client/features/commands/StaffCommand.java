package platform.client.features.commands;

import platform.api.utils.staff.StaffConstructor;
import platform.client.Delta;
import platform.client.utils.text.ChatUtil;

import platform.api.command.BaseCommand;
import platform.api.command.Command;
import platform.api.utils.staff.StaffProcessor;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;

@Command(a = "staff")
public class StaffCommand extends BaseCommand {
    @Override
    public void a(LiteralArgumentBuilder<CommandSourceStack> builder) {
        StaffProcessor processor = Delta.h().d().f();
        LiteralArgumentBuilder literalArgumentBuilderThen = builder.then(a("add").executes(context -> {
            ChatUtil.a((Object) "Использование: .staff add <ник>");
            return 1;
        }).then(b("ник").suggests(a()).executes(context2 -> {
            String name = a((CommandContext<CommandSourceStack>) context2, "ник");
            if (processor.d(name)) {
                ChatUtil.a((Object) ("Стафф " + name + " уже находится в списке стаффа."));
                return 1;
            }
            processor.b(name);
            processor.unSetup();
            ChatUtil.a((Object) ("Стафф " + name + " был успешно добавлен в список стаффа."));
            return 1;
        })));
        LiteralArgumentBuilder literalArgumentBuilderExecutes = a("remove").executes(context3 -> {
            ChatUtil.a((Object) "Использование: .staff remove <ник>");
            return 1;
        });
        RequiredArgumentBuilder<CommandSourceStack, String> requiredArgumentBuilderB = b("ник");
        Objects.requireNonNull(processor);
        literalArgumentBuilderThen.then(literalArgumentBuilderExecutes.then(requiredArgumentBuilderB.suggests(a(processor::e, (v0) -> {
            return v0.a();
        })).executes(context4 -> {
            String name = a((CommandContext<CommandSourceStack>) context4, "ник");
            if (!processor.d(name)) {
                ChatUtil.a((Object) ("Стафф " + name + " не найден в списке стаффа."));
                return 1;
            }
            processor.c(name);
            processor.unSetup();
            ChatUtil.a((Object) ("Стафф " + name + " был успешно удален из списка стаффа."));
            return 1;
        }))).then(a("list").executes(context5 -> {
            if (processor.e().isEmpty()) {
                ChatUtil.a((Object) "Список стаффа пуст.");
                return 1;
            }
            ChatUtil.a((Object) ("Список стаффа (" + processor.e().size() + "):"));
            for (StaffConstructor staffConstructor : processor.e()) {
                ChatUtil.a((Object) ("  - " + staffConstructor.a()));
            }
            return 1;
        })).then(a("clear").executes(context6 -> {
            ChatUtil.a((Object) ("Было успешно удалено стаффа из списка: " + processor.e().size()));
            processor.f();
            processor.unSetup();
            return 1;
        })).executes(context7 -> {
            ChatUtil.a((Object) "Использование: .staff <add|remove|list|clear>");
            return 1;
        });
    }
}



