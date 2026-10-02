package platform.client.processors;

import platform.api.event.EventManager;
import platform.api.event.events.client.BackendEvent;
import platform.api.event.interfaces.IEvent;
import platform.api.system.configs.BaseProcessor;
import platform.client.Delta;
import platform.client.utils.bridge.network.Packet;
import platform.client.utils.bridge.network.PacketSecurity;
import platform.client.utils.text.ChatUtil;
import platform.client.utils.web.ChatService;
import platform.client.utils.web.FlaskClient;

/**
 * FlaskProcessor — связывает ChatService с пайплайном BackendEvent / Communication.
 *
 * Как работает:
 *  1. setup() вызывает ChatService.connect() — тот грузит историю и запускает SSE-поток.
 *  2. Каждое новое сообщение из SSE передаётся в fireIrcEvent(), который
 *     оборачивает его в Packet("irc", ...) и стреляет BackendEvent.RECEIVE —
 *     Communication.java подхватывает и отображает в игровом чате без изменений.
 *  3. sendChat() отправляет сообщение через ChatService.sendMessage().
 *  4. setUrl() / setUsername() меняются в рантайме через .cloud команду.
 */
public class FlaskProcessor extends BaseProcessor {

    /** Shared PacketSecurity используется для сборки синтетических Packet'ов. */
    private final PacketSecurity security = new PacketSecurity();

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    public void setup() {
        ChatService chat = ChatService.getInstance();

        // Синхронизируем имя пользователя с тем что уже в ChatService
        // (можно переопределить через .cloud user <ник>)

        // Регистрируем коллбэк: каждое новое SSE-сообщение → BackendEvent
        chat.onNewMessage(this::fireIrcEvent);

        chat.connect();

        ChatUtil.a((Object) "FlaskProcessor запущен → " + FlaskClient.BASE_URL
                + " | пользователь: " + chat.getCurrentUser());
    }

    @Override
    public void unSetup() {
        ChatService.getInstance().disconnect();
    }

    // ─── Публичное API ────────────────────────────────────────────────────────

    /**
     * Отправляет сообщение в общий чат сервера.
     * Вызов неблокирующий.
     */
    public void sendChat(String message) {
        ChatService.getInstance().sendMessage(message);
    }

    /**
     * Отправляет приватное сообщение конкретному пользователю.
     */
    public void sendPrivate(String type, String data, String target) {
        ChatService.getInstance().sendPrivateMessage(type, data, target);
    }

    /**
     * Меняет адрес Flask-сервера в рантайме.
     * Пересоединяет ChatService с новым URL.
     */
    public void setUrl(String url) {
        FlaskClient.BASE_URL = url;
        // Перезапускаем соединение с новым URL
        ChatService chat = ChatService.getInstance();
        chat.disconnect();
        chat.onNewMessage(this::fireIrcEvent);
        chat.connect();
        ChatUtil.a((Object) "Flask URL обновлён: " + url);
    }

    /** Меняет имя пользователя для чат-сервера. */
    public void setUsername(String name) {
        ChatService.getInstance().setUsername(name);
        ChatUtil.a((Object) "Пользователь чата: " + ChatService.getInstance().getCurrentUser());
    }

    public String getUsername() {
        return ChatService.getInstance().getCurrentUser();
    }

    // ─── BackendEvent пайплайн ────────────────────────────────────────────────

    /**
     * Оборачивает сырую строку "user:text" в Packet на канале "irc"
     * и стреляет BackendEvent — Communication.java отображает в чате.
     *
     * JSON-структура payload совпадает с тем что ожидает Communication.a(BackendEvent):
     *   { "user": "...", "message": "...", "priority": "" }
     */
    private void fireIrcEvent(String raw) {
        try {
            int colon = raw.indexOf(':');
            String user    = colon > 0 ? raw.substring(0, colon) : "?";
            String message = colon > 0 ? raw.substring(colon + 1) : raw;

            String payload = security.a(
                    "user",     user,
                    "message",  message,
                    "priority", ""
            );

            Packet packet = new Packet("irc", payload, security);
            BackendEvent event = new BackendEvent(packet, BackendEvent.Phase.RECEIVE);
            EventManager.a((IEvent) event);

        } catch (Exception ignored) {}
    }

    // ─── Статический аксессор ─────────────────────────────────────────────────

    public static FlaskProcessor get() {
        try {
            return Delta.h().d().flaskProcessor();
        } catch (Exception e) {
            return null;
        }
    }
}
