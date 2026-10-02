package platform.client.features.modules.render;

import platform.api.module.Category;
import platform.api.event.interfaces.EventTarget;
import platform.api.module.Module;
import platform.api.module.ModuleRegister;
import platform.api.event.events.render.ScoreboardEvent;
import platform.api.module.setting.BooleanSetting;
import platform.api.module.setting.ModeSetting;
import platform.api.module.setting.MultiModeSetting;
import platform.api.module.setting.StringSetting;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

@ModuleRegister(a = "Board Spoofer", b = "Подменяет значения доната, монет и токенов в Scoreboard", c = Category.Render)
public class BoardSpoofer extends Module {
    private final MultiModeSetting b = new MultiModeSetting("Элементы настройки", new BooleanSetting("Ранг", true), new BooleanSetting("Монеты", true), new BooleanSetting("Токены", true));
    private final ModeSetting c = (ModeSetting) new ModeSetting("Выберите привилегию", "Игрок", "Игрок", "Барон", "Страж", "Герой", "Аспид", "Сквид", "Глава", "Элита", "Титан", "Принц", "Князь", "Герцог").a(() -> {
        return this.b.a("Ранг").c();
    });
    private final StringSetting d = (StringSetting) new StringSetting("Число монет", "", true).a(() -> {
        return this.b.a("Монеты").c();
    });
    private final StringSetting e = (StringSetting) new StringSetting("Число токенов", "", true).a(() -> {
        return this.b.a("Токены").c();
    });

    public BoardSpoofer() {
        a(this.b, this.c, this.d, this.e);
    }

    @EventTarget
    public void a(ScoreboardEvent event) {
        Component title = event.b();
        if (this.b.a("Ранг").c().booleanValue()) {
            title = a(title, "Ранг: ", this.c.c());
        }
        if (this.b.a("Монеты").c().booleanValue()) {
            Component class_2561Var = title;
            Locale locale = Locale.US;
            Object[] objArr = new Object[1];
            objArr[0] = Long.valueOf(Long.parseLong(this.d.c().isEmpty() ? "0" : this.d.c()));
            title = a(class_2561Var, "Монет: ", String.format(locale, "%,d", objArr));
        }
        if (this.b.a("Токены").c().booleanValue()) {
            title = a(title, "Токенов: ", this.e.c().isEmpty() ? "0" : this.e.c());
        }
        event.a(title);
    }

    private Component a(Component text, String label, String newValue) {
        MutableComponent rebuilt = Component.empty();
        StringBuilder seen = new StringBuilder();
        text.visit((style, part) -> {
            int previousLength = seen.length();
            seen.append(part);
            int labelIndex = seen.indexOf(label);
            if (labelIndex == -1 || labelIndex + label.length() <= previousLength) {
                rebuilt.append(Component.literal(part).setStyle(style));
                return Optional.empty();
            }
            rebuilt.append(Component.literal(part.substring(0, (labelIndex + label.length()) - previousLength)).setStyle(style));
            rebuilt.append(Component.literal(newValue).setStyle("Ранг: ".equals(label) ? a(newValue) : style));
            return Optional.of(true);
        }, Style.EMPTY);
        return rebuilt.getSiblings().isEmpty() ? text : rebuilt;
    }

    private Style a(String rank) {
        switch (rank) {
            case "Страж":
                return Style.EMPTY.withColor(ChatFormatting.YELLOW);
            case "Барон":
            case "Сквид":
                return Style.EMPTY.withColor(ChatFormatting.AQUA);
            case "Герой":
                return Style.EMPTY.withColor(ChatFormatting.GREEN);
            case "Аспид":
                return Style.EMPTY.withColor(ChatFormatting.DARK_AQUA);
            case "Глава":
            case "Титан":
                return Style.EMPTY.withColor(ChatFormatting.GOLD);
            case "Элита":
                return Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE);
            case "Принц":
            case "Князь":
                return Style.EMPTY.withColor(ChatFormatting.RED);
            case "Герцог":
                return Style.EMPTY.withColor(ChatFormatting.DARK_RED);
            default:
                return Style.EMPTY.withColor(ChatFormatting.WHITE);
        }
    }
}


