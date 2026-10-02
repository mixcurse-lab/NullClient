package platform.api.command;

import static platform.api.module.Interface.aM_;
import platform.api.event.EventManager;

import platform.api.command.Command;
import platform.api.module.Interface;
import platform.api.system.interfaces.Supplier;
import platform.client.utils.input.KeyUtil;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;

public abstract class BaseCommand implements Interface {
    protected final String b = ((Command) getClass().getAnnotation(Command.class)).a();

    public abstract void a(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder);

    public final void a(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = CaseInsensitiveLiteral.a(this.b);
        a(builder);
        dispatcher.register(builder);
        EventManager.a(this);
    }

    protected LiteralArgumentBuilder<CommandSourceStack> a(String name) {
        return CaseInsensitiveLiteral.a(name);
    }

    protected RequiredArgumentBuilder<CommandSourceStack, String> b(String name) {
        return RequiredArgumentBuilder.argument(name, StringArgumentType.string());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, String> c(String name) {
        return RequiredArgumentBuilder.argument(name, StringArgumentType.greedyString());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, String> d(String name) {
        return RequiredArgumentBuilder.argument(name, reader -> {
            int start = reader.getCursor();
            while (reader.canRead() && reader.peek() != ' ') {
                reader.skip();
            }
            return reader.getString().substring(start, reader.getCursor());
        });
    }

    protected RequiredArgumentBuilder<CommandSourceStack, Integer> e(String name) {
        return RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer());
    }

    protected RequiredArgumentBuilder<CommandSourceStack, Float> f(String name) {
        return RequiredArgumentBuilder.argument(name, FloatArgumentType.floatArg());
    }

    protected String a(CommandContext<CommandSourceStack> context, String name) {
        return StringArgumentType.getString(context, name);
    }

    protected int b(CommandContext<CommandSourceStack> context, String name) {
        return IntegerArgumentType.getInteger(context, name);
    }

    protected float c(CommandContext<CommandSourceStack> context, String name) {
        return FloatArgumentType.getFloat(context, name);
    }

    protected SuggestionProvider<CommandSourceStack> a() {
        return (context, builder) -> {
            if (aM_.player.connection == null) {
                return builder.buildFuture();
            }
            Stream streamFilter = aM_.player.connection.getListedOnlinePlayers().stream().map(entry -> {
                return entry.getProfile().name();
            }).filter(name -> {
                if (name != null) {
                    if (name.toLowerCase().startsWith(builder.getRemainingLowerCase() == null ? "" : builder.getRemainingLowerCase())) {
                        return true;
                    }
                }
                return false;
            });
            Objects.requireNonNull(builder);
            streamFilter.forEach(s -> builder.suggest((String) s));
            return builder.buildFuture();
        };
    }

    protected SuggestionProvider<CommandSourceStack> b() {
        return (context, builder) -> {
            for (KeyUtil key : KeyUtil.values()) {
                if (key != KeyUtil.UNKNOWN) {
                    builder.suggest(key.name());
                }
            }
            return builder.buildFuture();
        };
    }

    protected <T> SuggestionProvider<CommandSourceStack> a(java.util.function.Supplier<Collection<T>> itemsSupplier, Function<T, String> mapper) {
        return (context, builder) -> {
            String remaining = builder.getRemainingLowerCase() == null ? "" : builder.getRemainingLowerCase();
            Iterator it = ((Collection) itemsSupplier.get()).iterator();
            while (it.hasNext()) {
                String name = mapper.apply((T) it.next());
                if (name != null && name.toLowerCase().startsWith(remaining)) {
                    builder.suggest(name);
                }
            }
            return builder.buildFuture();
        };
    }
}



