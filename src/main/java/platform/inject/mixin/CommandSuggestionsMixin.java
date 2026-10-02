package platform.inject.mixin;

import platform.api.command.CommandProcessor;
import platform.client.Delta;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {

    @Shadow
    @Final
    EditBox input;

    @Shadow
    private ParseResults<ClientSuggestionProvider> currentParse;

    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;

    @Shadow
    private CommandSuggestions.SuggestionsList suggestions;

    @Shadow
    private boolean keepSuggestions;

    @Shadow
    private boolean currentParseIsCommand;

    @Shadow
    private boolean currentParseIsMessage;

    @Shadow
    @Final
    private List<FormattedCharSequence> commandUsage;

    @Shadow
    public abstract void showSuggestions(boolean immediateNarration);

    @Shadow
    private void updateUsageInfo(ParseResults<ClientSuggestionProvider> currentParse, Suggestions suggestions) {
        throw new AssertionError();
    }

    @Inject(method = {"updateCommandInfo"}, at = {@At("HEAD")}, cancellable = true)
    private void delta$onUpdateCommandInfo(CallbackInfo callbackInfo) {
        CommandProcessor commandProcessor = Delta.h().d().u();
        String prefix = commandProcessor.i();
        String text = this.input.getValue();
        if (!text.startsWith(prefix)) {
            return;
        }
        if (!this.keepSuggestions) {
            this.input.setSuggestion(null);
            this.suggestions = null;
        }
        this.commandUsage.clear();
        this.currentParseIsCommand = false;
        this.currentParseIsMessage = false;
        int cursorPosition = this.input.getCursorPosition();
        StringReader reading = new StringReader(text);
        reading.setCursor(prefix.length());
        CommandDispatcher dispatcher = commandProcessor.a();
        ParseResults<ClientSuggestionProvider> parse = dispatcher.parse(reading, commandProcessor.h());
        this.currentParse = parse;
        if (!(cursorPosition < prefix.length() || this.suggestions != null && this.keepSuggestions)) {
            this.pendingSuggestions = dispatcher.getCompletionSuggestions(parse, cursorPosition);
            this.pendingSuggestions.thenAccept(suggestionResult -> {
                if (this.pendingSuggestions.isDone()) {
                    this.updateUsageInfo(this.currentParse, (Suggestions) suggestionResult);
                    this.showSuggestions(false);
                }
            });
        }
        callbackInfo.cancel();
    }
}

