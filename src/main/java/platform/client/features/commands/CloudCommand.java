package platform.client.features.commands;

import platform.api.command.BaseCommand;
import platform.api.command.Command;
import platform.api.system.configs.ModuleProcessor;
import platform.client.Delta;
import platform.client.processors.FlaskProcessor;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.web.FlaskClient;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * .cloud — управление облачными конфигами через Flask сервер.
 *
 * Подкоманды:
 *   .cloud upload <имя>      — загружает локальный конфиг на сервер
 *   .cloud download <имя>    — скачивает конфиг с сервера и применяет его
 *   .cloud list              — показывает список конфигов на сервере
 *   .cloud delete <имя>      — удаляет конфиг с сервера
 *   .cloud url <адрес>       — меняет адрес Flask сервера
 *   .cloud user <ник>        — меняет имя пользователя для сервера
 *   .cloud send <сообщение>  — отправляет сообщение в IRC чат сервера
 *   .cloud status            — показывает текущие настройки подключения
 */
@Command(a = "cloud")
public class CloudCommand extends BaseCommand {

    @Override
    public void a(LiteralArgumentBuilder<CommandSourceStack> builder) {
        // .cloud upload <имя>
        builder.then(a("upload")
                .executes(ctx -> {
                    ChatUtil.a((Object) "Использование: .cloud upload <имя>");
                    return 1;
                })
                .then(b("имя").executes(ctx -> {
                    String name = a(ctx, "имя");
                    runAsync(() -> cmdUpload(name));
                    return 1;
                }))
        );

        // .cloud download <имя>
        builder.then(a("download")
                .executes(ctx -> {
                    ChatUtil.a((Object) "Использование: .cloud download <имя>");
                    return 1;
                })
                .then(b("имя").executes(ctx -> {
                    String name = a(ctx, "имя");
                    runAsync(() -> cmdDownload(name));
                    return 1;
                }))
        );

        // .cloud list
        builder.then(a("list").executes(ctx -> {
            runAsync(this::cmdList);
            return 1;
        }));

        // .cloud delete <имя>
        builder.then(a("delete")
                .executes(ctx -> {
                    ChatUtil.a((Object) "Использование: .cloud delete <имя>");
                    return 1;
                })
                .then(b("имя").executes(ctx -> {
                    String name = a(ctx, "имя");
                    runAsync(() -> cmdDelete(name));
                    return 1;
                }))
        );

        // .cloud url <адрес>
        builder.then(a("url")
                .executes(ctx -> {
                    ChatUtil.a((Object) "Текущий URL: " + FlaskClient.BASE_URL);
                    return 1;
                })
                .then(c("адрес").executes(ctx -> {
                    String url = a(ctx, "адрес");
                    FlaskProcessor fp = FlaskProcessor.get();
                    if (fp != null) {
                        fp.setUrl(url.trim());
                    } else {
                        FlaskClient.BASE_URL = url.trim();
                        ChatUtil.a((Object) "Flask URL: " + url.trim());
                    }
                    return 1;
                }))
        );

        // .cloud user <ник>
        builder.then(a("user")
                .executes(ctx -> {
                    FlaskProcessor fp = FlaskProcessor.get();
                    String cur = fp != null ? fp.getUsername() : "?";
                    ChatUtil.a((Object) "Текущий пользователь: " + cur);
                    return 1;
                })
                .then(b("ник").executes(ctx -> {
                    String nick = a(ctx, "ник");
                    FlaskProcessor fp = FlaskProcessor.get();
                    if (fp != null) {
                        fp.setUsername(nick);
                        ChatUtil.a((Object) "Пользователь изменён на: " + nick);
                    } else {
                        ChatUtil.a((Object) "FlaskProcessor не запущен.");
                    }
                    return 1;
                }))
        );

        // .cloud send <сообщение>
        builder.then(a("send")
                .executes(ctx -> {
                    ChatUtil.a((Object) "Использование: .cloud send <сообщение>");
                    return 1;
                })
                .then(c("сообщение").executes(ctx -> {
                    String msg = a(ctx, "сообщение");
                    FlaskProcessor fp = FlaskProcessor.get();
                    if (fp != null) {
                        fp.sendChat(msg);
                    } else {
                        ChatUtil.a((Object) "FlaskProcessor не запущен.");
                    }
                    return 1;
                }))
        );

        // .cloud status
        builder.then(a("status").executes(ctx -> {
            FlaskProcessor fp = FlaskProcessor.get();
            ChatUtil.a((Object) "Flask URL:  " + FlaskClient.BASE_URL);
            ChatUtil.a((Object) "Пользователь: " + (fp != null ? fp.getUsername() : "?"));
            return 1;
        }));

        // .cloud (no subcommand)
        builder.executes(ctx -> {
            ChatUtil.a((Object) "Использование: .cloud <upload|download|list|delete|url|user|send|status>");
            return 1;
        });
    }

    // ─── Subcommand implementations ───────────────────────────────────────────

    private void cmdUpload(String configName) {
        ModuleProcessor mp = Delta.h().d().t();
        File configFile = new File(mp.b(), configName + ".json");

        if (!configFile.exists()) {
            ChatUtil.a((Object) "Локальный конфиг «" + configName + "» не найден. Сохраните его сначала (.cfg save).");
            return;
        }

        String json;
        try {
            json = Files.readString(configFile.toPath());
        } catch (Exception e) {
            ChatUtil.a((Object) "Ошибка чтения файла: " + e.getMessage());
            return;
        }

        FlaskProcessor fp = FlaskProcessor.get();
        String user = fp != null ? fp.getUsername() : "NullClient";

        ChatUtil.a((Object) "Загрузка конфига «" + configName + "» на сервер...");
        boolean ok = FlaskClient.uploadConfig(user, configName, json);
        if (ok) {
            ChatUtil.a((Object) "Конфиг «" + configName + "» успешно загружен на сервер.");
        } else {
            ChatUtil.a((Object) "Ошибка при загрузке конфига на сервер. Проверьте URL и соединение.");
        }
    }

    private void cmdDownload(String configName) {
        FlaskProcessor fp = FlaskProcessor.get();
        String user = fp != null ? fp.getUsername() : "NullClient";

        ChatUtil.a((Object) "Скачивание конфига «" + configName + "» с сервера...");
        String json = FlaskClient.downloadConfig(user, configName);

        if (json == null) {
            ChatUtil.a((Object) "Конфиг «" + configName + "» не найден на сервере.");
            return;
        }

        // Write to the local config directory then apply via ModuleProcessor
        ModuleProcessor mp = Delta.h().d().t();
        File configFile = new File(mp.b(), configName + ".json");
        try {
            Files.writeString(configFile.toPath(), json);
        } catch (Exception e) {
            ChatUtil.a((Object) "Ошибка записи файла: " + e.getMessage());
            return;
        }

        boolean loaded = mp.c(configName);
        if (loaded) {
            ChatUtil.a((Object) "Конфиг «" + configName + "» скачан и применён.");
        } else {
            ChatUtil.a((Object) "Конфиг «" + configName + "» скачан, но не удалось применить. Загрузите вручную: .cfg load " + configName);
        }
    }

    private void cmdList() {
        FlaskProcessor fp = FlaskProcessor.get();
        String user = fp != null ? fp.getUsername() : "NullClient";

        List<String> configs = FlaskClient.listConfigs(user);
        if (configs == null || configs.isEmpty()) {
            ChatUtil.a((Object) "На сервере нет конфигов для пользователя «" + user + "».");
            return;
        }
        ChatUtil.a((Object) ("Облачные конфиги (" + configs.size() + ") для «" + user + "»:"));
        for (String name : configs) {
            ChatUtil.a((Object) "  - " + name);
        }
    }

    private void cmdDelete(String configName) {
        FlaskProcessor fp = FlaskProcessor.get();
        String user = fp != null ? fp.getUsername() : "NullClient";

        ChatUtil.a((Object) "Удаление конфига «" + configName + "» с сервера...");
        boolean ok = FlaskClient.deleteConfig(user, configName);
        if (ok) {
            ChatUtil.a((Object) "Конфиг «" + configName + "» удалён с сервера.");
        } else {
            ChatUtil.a((Object) "Конфиг «" + configName + "» не найден на сервере или ошибка соединения.");
        }
    }

    // ─── Utility ─────────────────────────────────────────────────────────────

    /** Runs a task on a virtual/daemon thread to keep the game thread free. */
    private static void runAsync(Runnable task) {
        Thread t = new Thread(task, "CloudCommand-worker");
        t.setDaemon(true);
        t.start();
    }
}
