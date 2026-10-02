package platform.client.utils.web;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * ChatService — клиент для Flask чат-сервера.
 *
 * Возможности:
 *  - Загрузка истории сообщений при подключении
 *  - SSE-стриминг новых сообщений в реальном времени с авто-реконнектом
 *  - Отправка публичных сообщений в общий чат
 *  - Отправка приватных сообщений конкретному пользователю (FRIEND_REQUEST и др.)
 *
 * Использование:
 *   ChatService.getInstance().connect();
 *   ChatService.getInstance().onNewMessage(msg -> /* показать в чате *\/);
 *   ChatService.getInstance().sendMessage("привет");
 *   ChatService.getInstance().disconnect();
 */
public class ChatService {

    // ─── Singleton ────────────────────────────────────────────────────────────

    private static final ChatService INSTANCE = new ChatService();

    public static ChatService getInstance() {
        return INSTANCE;
    }

    private ChatService() {}

    // ─── Состояние ────────────────────────────────────────────────────────────

    /** Список последних сообщений (синхронизированный). */
    private final List<String> history = Collections.synchronizedList(new ArrayList<>());

    /**
     * Имя пользователя: берётся из системного свойства user.name,
     * все нелатинские/нецифровые символы заменяются на _.
     * Можно переопределить через setUsername().
     */
    private volatile String username =
            System.getProperty("user.name", "NullClient").replaceAll("[^a-zA-Z0-9]", "_");

    private volatile boolean running = false;
    private Thread sseThread;

    /** Коллбэк, вызываемый при каждом новом сообщении из SSE-потока. */
    private volatile Consumer<String> messageCallback = null;

    private final Gson gson = new Gson();

    // ─── Подключение / отключение ─────────────────────────────────────────────

    /**
     * Загружает историю и запускает SSE-слушатель.
     * Повторный вызов игнорируется.
     */
    public synchronized void connect() {
        if (running) return;
        running = true;
        fetchHistory();
        startSseListener();
    }

    /** Останавливает SSE-поток и освобождает ресурсы. */
    public synchronized void disconnect() {
        running = false;
        if (sseThread != null) {
            sseThread.interrupt();
            sseThread = null;
        }
    }

    // ─── Публичное API ────────────────────────────────────────────────────────

    /** Устанавливает коллбэк, который будет вызван при каждом новом входящем сообщении. */
    public void onNewMessage(Consumer<String> callback) {
        this.messageCallback = callback;
    }

    /** Возвращает копию истории сообщений (потокобезопасно). */
    public List<String> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(history));
    }

    /** Текущее имя пользователя, под которым идут запросы. */
    public String getCurrentUser() {
        return username;
    }

    /** Меняет имя пользователя. Пересоединение не нужно. */
    public void setUsername(String name) {
        this.username = name.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * Отправляет сообщение в общий чат от имени текущего пользователя.
     * Вызов неблокирующий — выполняется в daemon-потоке.
     */
    public void sendMessage(String text) {
        if (text == null || (text = text.trim()).isEmpty()) return;
        final String finalText = text;
        Thread t = new Thread(() -> doSendMessage(username, finalText), "ChatSend");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Отправляет приватное сообщение конкретному пользователю.
     *
     * @param type   тип сообщения, например "FRIEND_REQUEST" или "MARK"
     * @param data   данные сообщения
     * @param target имя целевого пользователя на Flask-сервере
     */
    public void sendPrivateMessage(String type, String data, String target) {
        if (target == null || target.trim().isEmpty()) {
            System.err.println("[ChatService] sendPrivateMessage: цель не указана");
            return;
        }
        final String payload;
        if ("FRIEND_REQUEST".equals(type)) {
            payload = "[FRIEND_REQUEST]" + username + ":" + data;
        } else {
            payload = "[" + type + "]" + username + ": " + data;
        }
        Thread t = new Thread(() -> doSendRaw(target.trim(), payload), "ChatPrivateSend");
        t.setDaemon(true);
        t.start();
    }

    // ─── Загрузка истории ─────────────────────────────────────────────────────

    private void fetchHistory() {
        try {
            URL url = new URL(FlaskClient.BASE_URL + "/api/history");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(6000);
            conn.setReadTimeout(6000);

            if (conn.getResponseCode() == 200) {
                String json = readStream(conn.getInputStream());
                JsonArray arr = gson.fromJson(json, JsonArray.class);
                history.clear();
                for (JsonElement el : arr) {
                    history.add(el.getAsString());
                }
                System.out.println("[ChatService] История загружена: " + history.size() + " сообщений");
            } else {
                System.err.println("[ChatService] Ошибка загрузки истории: HTTP " + conn.getResponseCode());
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("[ChatService] fetchHistory exception: " + e.getMessage());
        }
    }

    // ─── SSE-слушатель ────────────────────────────────────────────────────────

    private void startSseListener() {
        sseThread = new Thread(() -> {
            while (running) {
                HttpURLConnection conn = null;
                try {
                    String encoded = URLEncoder.encode(username, "UTF-8");
                    URL url = new URL(FlaskClient.BASE_URL + "/api/stream/" + encoded);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(0); // бесконечный таймаут для SSE

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                        String line;
                        while (running && (line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                if (!data.isEmpty() && !history.contains(data)) {
                                    history.add(data);
                                    System.out.println("[ChatService SSE] Новое: " + data);
                                    // Уведомляем подписчика (FlaskProcessor → BackendEvent)
                                    Consumer<String> cb = messageCallback;
                                    if (cb != null) {
                                        try { cb.accept(data); } catch (Exception ignored) {}
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    if (running) {
                        System.err.println("[ChatService SSE] Отключение, реконнект через 3с: " + e.getMessage());
                        try { Thread.sleep(3000); } catch (InterruptedException ignored) { break; }
                    }
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }
        }, "ChatSSE");
        sseThread.setDaemon(true);
        sseThread.start();
    }

    // ─── Отправка (внутренние методы) ─────────────────────────────────────────

    /** Отправляет JSON {"message": text} на /api/send/<user>. */
    private void doSendMessage(String user, String text) {
        try {
            String encoded = URLEncoder.encode(user, "UTF-8");
            URL url = new URL(FlaskClient.BASE_URL + "/api/send/" + encoded);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            JsonObject body = new JsonObject();
            body.addProperty("message", text);
            byte[] bytes = gson.toJson(body).getBytes("UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                String err = conn.getErrorStream() != null ? readStream(conn.getErrorStream()) : "";
                System.err.println("[ChatService] Ошибка отправки: HTTP " + code + " → " + err);
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("[ChatService] doSendMessage exception: " + e.getMessage());
        }
    }

    /**
     * Отправляет сырой текст (не JSON) на /api/send/<target>.
     * Используется для приватных сообщений, у которых свой формат.
     */
    private void doSendRaw(String target, String rawText) {
        try {
            String encoded = URLEncoder.encode(target, "UTF-8");
            URL url = new URL(FlaskClient.BASE_URL + "/api/send/" + encoded);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // Приватные сообщения Flask-сервер принимает как plain text
            byte[] bytes = rawText.getBytes("UTF-8");
            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));

            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }

            int code = conn.getResponseCode();
            System.out.println("[ChatService] Приватное → " + target + ": HTTP " + code);
            if (code >= 400 && conn.getErrorStream() != null) {
                System.err.println("[ChatService] Ошибка: " + readStream(conn.getErrorStream()));
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("[ChatService] doSendRaw exception: " + e.getMessage());
        }
    }

    // ─── Утилиты ─────────────────────────────────────────────────────────────

    private static String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString().trim();
    }
}
