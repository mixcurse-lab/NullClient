package platform.client.utils.text;

import platform.client.ui.shader.GradientUtil;
import static platform.api.module.Interface.aM_;
import platform.client.Delta;
import platform.client.utils.render.ColorUtil;

import platform.api.system.configs.ThemeInfo;
import platform.api.module.Interface;

import lombok.Generated;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.MutableComponent;

public class ChatUtil implements Interface {
    @Generated
    private ChatUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void a(Object message) {
        a("[NullClient]", message);
    }

    public static void a(String prefix, Object message) {
        MutableComponent class_5250VarB;
        if (aM_.player != null) {
            if (prefix == null || prefix.isEmpty()) {
                class_5250VarB = b(message);
            } else {
                class_5250VarB = a(prefix).append(Component.literal("")).append(b(message));
            }
            aM_.player.sendSystemMessage(class_5250VarB);
        }
    }

    public static MutableComponent b(Object message) {
        if (message instanceof MutableComponent) {
            MutableComponent mutableText = (MutableComponent) message;
            return mutableText;
        }
        if (message instanceof Component) {
            Component text = (Component) message;
            return text.copy();
        }
        return Component.literal(("&7" + String.valueOf(message)).replace('&', (char) 167));
    }

    public static MutableComponent a(Object message, Component hover) {
        String strValueOf;
        if (message instanceof Component) {
            Component text = (Component) message;
            strValueOf = text.getString();
        } else {
            strValueOf = String.valueOf(message);
        }
        String rawMessage = strValueOf;
        return Component.literal(rawMessage.replace('&', (char) 167)).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowText(hover.copy())));
    }

    private static MutableComponent a(String prefix) {
        int primary = Delta.h().d().o().a(ThemeInfo.PRIMARY).a();
        return GradientUtil.a(prefix + " » ", primary, ColorUtil.b(primary, 0.5f), 1, 5.0f);
    }
}


