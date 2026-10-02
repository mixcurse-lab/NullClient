package platform.api.command;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.client.utils.lib.log4j.LoggerFactory;

import platform.client.features.commands.AHCommand;
import platform.client.features.commands.BindCommand;
import platform.client.features.commands.BlockESPCommand;
import platform.client.features.commands.CCCommand;
import platform.client.features.commands.ConfigCommand;
import platform.client.features.commands.FriendCommand;
import platform.client.features.commands.GPSCommand;
import platform.client.features.commands.HClipCommand;
import platform.client.features.commands.LayoutCommand;
import platform.client.features.commands.MacrosCommand;
import platform.client.features.commands.RCTCommand;
import platform.client.features.commands.StaffCommand;
import platform.client.features.commands.VClipCommand;
import platform.client.features.commands.WayCommand;
import platform.client.features.commands.WardenCommand;
import platform.client.features.commands.CloudCommand;
import platform.client.features.commands.IrcCommand;
import platform.api.system.configs.BaseProcessor;

import platform.client.utils.lib.log4j.Logger_2;
import platform.api.annotation.Compile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.server.permissions.PermissionSet;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class CommandProcessor extends BaseProcessor {

    @Generated
    private static final Logger_2 b;
    private final List<BaseCommand> d = new ArrayList();
    private final WayCommand e = new WayCommand();
    private final GPSCommand f = new GPSCommand();
    private final LayoutCommand g = new LayoutCommand();
    private final RCTCommand h = new RCTCommand();
    private final BlockESPCommand i = new BlockESPCommand();
    private final String k = ".";
    private final CommandDispatcher<CommandSourceStack> c = new CommandDispatcher<>(new CaseInsensitiveLiteral.a());
    private final ClientSuggestionProvider j = new ClientSuggestionProvider((ClientPacketListener) null, Minecraft.getInstance(), PermissionSet.NO_PERMISSIONS);

    @Override
    @Compile
    public void setup() {
        a(this.e, this.f, this.g, this.h, this.i, new AHCommand(), new MacrosCommand(), new FriendCommand(), new StaffCommand(), new ConfigCommand(), new BindCommand(), new VClipCommand(), new HClipCommand(), new CCCommand(), new WardenCommand(), new CloudCommand(), new IrcCommand());
    }

    static {
        NativeMethodLookup.lookup(CommandProcessor.class, 22);
        b = LoggerFactory.a((Class<?>) CommandProcessor.class);
    }

    @Generated
    public CommandDispatcher<CommandSourceStack> a() {
        return this.c;
    }

    @Generated
    public List<BaseCommand> b() {
        return this.d;
    }

    @Generated
    public WayCommand c() {
        return this.e;
    }

    @Generated
    public GPSCommand d() {
        return this.f;
    }

    @Generated
    public LayoutCommand e() {
        return this.g;
    }

    @Generated
    public RCTCommand f() {
        return this.h;
    }

    @Generated
    public BlockESPCommand g() {
        return this.i;
    }

    @Generated
    public ClientSuggestionProvider h() {
        return this.j;
    }

    @Generated
    public String i() {
        Objects.requireNonNull(this);
        return ".";
    }

    @Override
    public void unSetup() {
    }

    public void a(BaseCommand... commands) {
        for (BaseCommand command : commands) {
            this.d.add(command);
            command.a(this.c);
        }
    }

    public void a(String message, CallbackInfo ci) {
        if (message == null || message.isEmpty() || !message.startsWith(i())) {
            return;
        }
        ci.cancel();
        String command = message.substring(i().length()).trim();
        if (command.isEmpty()) {
            return;
        }
        try {
            ParseResults<CommandSourceStack> results = ((CommandDispatcher) this.c).parse(command, this.j);
            for (ParsedCommandNode<CommandSourceStack> parsed : results.getContext().getNodes()) {
                CommandNode<CommandSourceStack> node = parsed.getNode();
                if (node instanceof LiteralCommandNode) {
                    LiteralCommandNode<CommandSourceStack> literal = (LiteralCommandNode<CommandSourceStack>) node;
                    int typedLength = parsed.getRange().getLength();
                    if (typedLength != literal.getLiteral().length()) {
                        return;
                    }
                }
            }
            this.c.execute(results);
        } catch (CommandSyntaxException e) {
            platform.client.utils.text.ChatUtil.a((Object) ("Неверная команда: " + e.getRawMessage().getString()));
        }
    }

    public static <T> RequiredArgumentBuilder<CommandSourceStack, T> a(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}

