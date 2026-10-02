package platform.client.utils.bridge.discord;

import platform.api.system.interfaces.NativeMethodLookup;
import platform.api.system.configs.BaseProcessor;
import platform.api.annotation.Compile;
import platform.client.utils.lib.log4j.LogManager;
import platform.client.utils.lib.log4j.Logger;
import lombok.Generated;

public class DiscordProcessor extends BaseProcessor {

    private static final Logger LOG = LogManager.b(DiscordProcessor.class);

    /**
     * ──────────────────────────────────────────────────────────────────────
     *  DISCORD APPLICATION CLIENT ID
     *  1. Зайди на https://discord.com/developers/applications
     *  2. Нажми "New Application", назови "NullClient"
     *  3. На вкладке "Rich Presence → Art Assets" загрузи свою гифку
     *     и дай ей имя: nullclient_logo
     *  4. Скопируй Application ID и вставь сюда вместо 0L
     * ──────────────────────────────────────────────────────────────────────
     */
    private static final long CLIENT_ID = 1555192327633641522L;

    /**
     * Ключ изображения из Art Assets твоего приложения на Discord Developer Portal.
     * Должен совпадать с именем, которое ты дал гифке при загрузке.
     */
    private static final String LARGE_IMAGE_KEY = "nullclient_logo";

    private DiscordIPCClient ipcClient;

    // activity создаётся один раз при setup() и переиспользуется
    private Activity rpcActivity;

    @Override
    @Compile
    public void setup() {
        if (CLIENT_ID == 0L) {
            LOG.a("Discord RPC disabled: CLIENT_ID not set in DiscordProcessor");
            return;
        }

        // Составляем Rich Presence:
        //   large_image = твоя гифка (ключ из Art Assets)
        //   details     = название клиента
        //   state       = подзаголовок
        //   timestamp   = время запуска клиента (таймер "playing for X min")
        rpcActivity = new Activity.a()
                .a(ActivityType.PLAYING)                          // тип: Playing
                .b("NullClient")                                  // details (крупный текст)
                .a("nullclient.xyz")                              // state  (мелкий текст)
                .a(System.currentTimeMillis() / 1000L)            // timestamp start
                .a(LARGE_IMAGE_KEY, "NullClient")                 // large image + hover-текст
                .a();                                             // build

        ipcClient = new DiscordIPCClient(CLIENT_ID);
        ipcClient.connect().whenComplete(this::a);
    }

    @Override
    public void unSetup() {
        if (ipcClient != null) {
            ipcClient.close();
            ipcClient = null;
        }
    }

    /**
     * Callback после завершения connect().
     * При успехе — сразу отправляем активность.
     * При ошибке — просто логируем (клиент сам переподключится).
     */
    public void a(Void result, Throwable ex) {
        if (ex != null) {
            LOG.f("Discord RPC connect failed: {}", ex.getMessage());
            return;
        }
        if (ipcClient != null && rpcActivity != null) {
            ipcClient.updateActivity(rpcActivity);
        }
    }

    static {
        NativeMethodLookup.lookup(DiscordProcessor.class, 25);
    }

    @Generated
    public Object a() {
        return ipcClient;
    }
}



