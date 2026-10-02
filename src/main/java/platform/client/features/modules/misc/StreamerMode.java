package platform.client.features.modules.misc;

import static platform.api.module.Interface.aM_;

import platform.api.module.Category;
import platform.client.Delta;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.ScoreboardEvent;
import platform.api.event.events.client.TextVisitEvent;
import platform.api.utils.account.FriendConstructor;
import platform.api.module.setting.BooleanSetting;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.Generated;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

@ModuleRegister(a = "Streamer Mode", b = "Скрывает личные данные при стриминге и записи", c = Category.Misc)
public class StreamerMode extends Module {
    private final BooleanSetting b = new BooleanSetting("Скрывать скины игроков", true);
    private final BooleanSetting c = new BooleanSetting("Скрывать имена друзей", true);
    private final BooleanSetting d = new BooleanSetting("Скрывать номер анархии", true);

    @Generated
    public BooleanSetting q() {
        return this.b;
    }

    @Generated
    public BooleanSetting r() {
        return this.c;
    }

    @Generated
    public BooleanSetting s() {
        return this.d;
    }

    public StreamerMode() {
        a(this.b, this.c, this.d);
    }

    @EventTarget
    public void a(TextVisitEvent event) {
        event.a(a(event.b()));
    }

    @EventTarget
    public void a(ScoreboardEvent event) {
        if (this.d.c().booleanValue()) {
            MutableComponent text = Component.empty();
            event.b().visit((style, part) -> {
                text.append(Component.literal(part.replaceAll("Анархия-\\d+", "Анархия-???")).setStyle(style));
                return Optional.empty();
            }, Style.EMPTY);
            event.a((Component) text);
        }
    }

    public String a(String text) {
        String result = text.replaceAll("(?i)" + aM_.getUser().getName(), "Protected");
        if (this.c.c().booleanValue()) {
            for (FriendConstructor friend : Delta.h().d().e().a()) {
                result = Pattern.compile(friend.a(), 82).matcher(result).replaceAll("Protected");
            }
        }
        if (this.d.c().booleanValue()) {
            result = result.replaceAll("Анархия-\\d+", "Анархия-???");
        }
        return result;
    }
}







