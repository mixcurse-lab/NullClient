package platform.inject.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatComponent.class)
public abstract class ChatSpamMixin {
    @Shadow @Final private List<GuiMessage> allMessages;
    @Shadow private void refreshTrimmedMessages() {}

    @Inject(method = "addMessageToQueue", at = @At("TAIL"))
    private void delta$handleDuplicate(GuiMessage message, CallbackInfo ci) {
        if (this.allMessages.size() < 2) return;
        GuiMessage current = this.allMessages.get(0);
        GuiMessage previous = this.allMessages.get(1);
        String currentText = current.content().getString();
        String previousText = previous.content().getString();
        int counterIndex = previousText.lastIndexOf(" [x");
        if (counterIndex != -1 && previousText.endsWith("]")) {
            String originalText = previousText.substring(0, counterIndex);
            if (originalText.equals(currentText)) {
                String countStr = previousText.substring(counterIndex + 3, previousText.length() - 1);
                int count;
                try {
                    count = Integer.parseInt(countStr) + 1;
                } catch (NumberFormatException e) {
                    return;
                }
                delta$updateMessage(current, message.content(), count);
                this.allMessages.remove(1);
                refreshTrimmedMessages();
                return;
            }
            return;
        }
        if (previousText.equals(currentText)) {
            delta$updateMessage(current, message.content(), 2);
            this.allMessages.remove(1);
            refreshTrimmedMessages();
        }
    }

    @Unique
    private void delta$updateMessage(GuiMessage lastMessage, Component message, int count) {
        Component updatedContent = Component.empty().append(message.copy()).append(Component.literal(" [x" + count + "]").withStyle(ChatFormatting.GRAY));
        GuiMessage updated = new GuiMessage(lastMessage.addedTime(), updatedContent, lastMessage.signature(), lastMessage.source(), lastMessage.tag());
        this.allMessages.set(0, updated);
    }
}
