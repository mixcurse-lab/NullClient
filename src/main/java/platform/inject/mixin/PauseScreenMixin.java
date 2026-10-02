package platform.inject.mixin;

import static platform.api.module.Interface.aM_;

import platform.api.module.InterfaceC0020Opcode;
import platform.client.utils.player.ServerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import platform.inject.accessors.ButtonAccessor;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    @Shadow private Button disconnectButton;

    @Unique private boolean delta$reconnect;
    @Unique private boolean delta$disconnect;

    private PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void delta$init(CallbackInfo ci) {
        ServerData server = aM_.getCurrentServer();

        if (this.disconnectButton != null) {
            ButtonAccessor accessor = (ButtonAccessor) (Object) this.disconnectButton;
            Button.OnPress original = accessor.getOnPress();
            if (original != null) {
                accessor.setOnPress(btn -> {
                    if (ServerUtil.e() && !this.delta$disconnect) {
                        btn.setMessage(btn.getMessage().copy().withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                        this.delta$disconnect = true;
                    } else {
                        original.onPress(btn);
                    }
                });
            }
        } else {

            children().stream().filter(c -> c instanceof Button).map(c -> (Button) c).filter(b -> {
                Component msg = b.getMessage();
                if (msg.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents trans) {
                    return trans.getKey().equals("menu.disconnect");
                }
                return false;
            }).findFirst().ifPresent(btn -> {
                ButtonAccessor acc = (ButtonAccessor) (Object) btn;
                Button.OnPress orig = acc.getOnPress();
                if (orig != null) {
                    acc.setOnPress(w -> {
                        if (ServerUtil.e() && !this.delta$disconnect) {
                            w.setMessage(w.getMessage().copy().withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                            this.delta$disconnect = true;
                        } else {
                            orig.onPress(w);
                        }
                    });
                }
            });
        }

        if (server != null) {
            int maxY = children().stream().filter(c -> c instanceof Button).map(c -> ((Button) c).getY()).max(Integer::compareTo).orElse(this.height / 2);
            addRenderableWidget(Button.builder(Component.literal("Переподключиться"), btn -> {
                if (ServerUtil.e() && !this.delta$reconnect) {
                    btn.setMessage(Component.literal("Переподключиться").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                    this.delta$reconnect = true;
                } else {
                    try {
                        if (aM_.level != null) {
                            aM_.level.disconnect(Component.literal(""));
                        }
                        ServerAddress address = ServerAddress.parseString(server.ip);
                        ConnectScreen.startConnecting(new TitleScreen(), aM_, address, server, false, null);
                    } catch (Exception e) {
                    }
                }
            }).bounds(this.width / 2 - 100, maxY + 24, InterfaceC0020Opcode.aN, 20).build());
        }
    }
}
