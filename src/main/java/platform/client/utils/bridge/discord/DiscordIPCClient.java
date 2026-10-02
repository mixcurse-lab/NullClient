package platform.client.utils.bridge.discord;

import com.google.gson.JsonObject;
import platform.client.utils.lib.javassist.Frame;
import platform.client.utils.lib.javassist.OpCode;
import platform.client.utils.lib.log4j.LogManager;
import platform.client.utils.lib.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Discord IPC клиент — работает через Windows Named Pipe (discord-ipc-N).
 * Делает handshake, принимает READY и позволяет обновлять Rich Presence.
 */
public class DiscordIPCClient {

    private static final Logger LOG = LogManager.b(DiscordIPCClient.class);

    private final long clientId;
    private RandomAccessFile pipe;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /** Фоновый поток — читает входящие фреймы от Discord (READY, ERROR, CLOSE). */
    private Thread readerThread;

    /** Таймер переподключения при потере соединения. */
    private final ScheduledExecutorService reconnectScheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "DiscordRPC-Reconnect");
                t.setDaemon(true);
                return t;
            });

    public DiscordIPCClient(long clientId) {
        this.clientId = clientId;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Публичный API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Открыть соединение с Discord и выполнить handshake.
     * Возвращает CompletableFuture, которое завершится после READY.
     */
    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Thread connectThread = new Thread(() -> {
            try {
                openPipe();
                sendHandshake();
                startReaderThread(future);
            } catch (Exception e) {
                LOG.f("Failed to connect to Discord IPC: {}", e.getMessage());
                future.completeExceptionally(e);
                scheduleReconnect();
            }
        }, "DiscordRPC-Connect");
        connectThread.setDaemon(true);
        connectThread.start();
        return future;
    }

    /**
     * Обновить Rich Presence активность.
     */
    public void updateActivity(Activity activity) {
        if (!connected.get()) {
            LOG.a("updateActivity called but not connected, skipping");
            return;
        }
        try {
            JsonObject args = new JsonObject();
            args.add("activity", activity.j());
            args.addProperty("pid", ProcessHandle.current().pid());
            sendCommand("SET_ACTIVITY", args);
            LOG.a("Rich Presence updated: {}", activity.b().orElse("(no details)"));
        } catch (Exception e) {
            LOG.f("Failed to update activity: {}", e.getMessage());
            connected.set(false);
            scheduleReconnect();
        }
    }

    /**
     * Закрыть соединение и освободить ресурсы.
     */
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        connected.set(false);
        reconnectScheduler.shutdownNow();
        if (readerThread != null) {
            readerThread.interrupt();
        }
        closePipe();
        LOG.a("Discord IPC client closed");
    }

    public boolean isConnected() {
        return connected.get();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Внутренние методы
    // ──────────────────────────────────────────────────────────────────────────

    /** Перебирает discord-ipc-0..9 и открывает первый доступный пайп. */
    private void openPipe() throws IOException {
        for (int i = 0; i < PipeLocator.a; i++) {
            String path = PipeLocator.a(i);
            try {
                pipe = new RandomAccessFile(path, "rw");
                LOG.a("Opened Discord pipe: {}", path);
                return;
            } catch (IOException ignored) {
                // этот пайп занят или отсутствует, пробуем следующий
            }
        }
        throw new NoDiscordClientException();
    }

    /** Отправляет HANDSHAKE фрейм с clientId и версией протокола. */
    private void sendHandshake() throws IOException {
        JsonObject handshake = new JsonObject();
        handshake.addProperty("v", 1);
        handshake.addProperty("client_id", String.valueOf(clientId));
        writeFrame(OpCode.HANDSHAKE, handshake);
        LOG.a("Handshake sent for client_id={}", clientId);
    }

    /** Запускает фоновый поток чтения входящих фреймов. */
    private void startReaderThread(CompletableFuture<Void> readyFuture) {
        readerThread = new Thread(() -> {
            while (!closed.get() && pipe != null) {
                try {
                    Frame frame = readFrame();
                    if (frame == null) break;
                    handleFrame(frame, readyFuture);
                } catch (IOException e) {
                    if (!closed.get()) {
                        LOG.f("Discord IPC read error: {}", e.getMessage());
                        connected.set(false);
                        if (!readyFuture.isDone()) {
                            readyFuture.completeExceptionally(e);
                        }
                        scheduleReconnect();
                    }
                    break;
                }
            }
        }, "DiscordRPC-Reader");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    /** Разбирает входящий фрейм и обрабатывает READY / ERROR / CLOSE. */
    private void handleFrame(Frame frame, CompletableFuture<Void> readyFuture) {
        OpCode op = frame.b();
        JsonObject data = frame.c();

        switch (op) {
            case FRAME -> {
                if (data == null) return;
                String cmd = data.has("cmd") ? data.get("cmd").getAsString() : "";
                String evt = data.has("evt") && !data.get("evt").isJsonNull()
                        ? data.get("evt").getAsString() : "";

                if ("DISPATCH".equals(cmd) && "READY".equals(evt)) {
                    connected.set(true);
                    LOG.a("Discord RPC READY — Rich Presence is live");
                    readyFuture.complete(null);
                } else if ("ERROR".equals(evt)) {
                    String msg = data.has("data") ? data.getAsJsonObject("data")
                            .get("message").getAsString() : "unknown";
                    LOG.f("Discord RPC ERROR: {}", msg);
                    if (!readyFuture.isDone()) {
                        readyFuture.completeExceptionally(new RuntimeException("Discord error: " + msg));
                    }
                }
            }
            case CLOSE -> {
                LOG.a("Discord sent CLOSE frame");
                connected.set(false);
                closePipe();
                if (!closed.get()) scheduleReconnect();
            }
            case PING -> {
                // ответить PONG
                try {
                    writeFrame(OpCode.PONG, data);
                } catch (IOException e) {
                    LOG.f("Failed to send PONG: {}", e.getMessage());
                }
            }
            default -> { /* HANDSHAKE, PONG — игнорируем */ }
        }
    }

    /** Отправляет команду Discord IPC (SET_ACTIVITY и т.д.). */
    private void sendCommand(String cmd, JsonObject args) throws IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("cmd", cmd);
        payload.addProperty("nonce", UUID.randomUUID().toString());
        if (args != null) payload.add("args", args);
        writeFrame(OpCode.FRAME, payload);
    }

    /** Записывает фрейм в Named Pipe. */
    private synchronized void writeFrame(OpCode opCode, JsonObject data) throws IOException {
        if (pipe == null) throw new IOException("Pipe is not open");
        byte[] payload = data == null ? new byte[0]
                : data.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer buf = ByteBuffer.allocate(8 + payload.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(opCode.a());
        buf.putInt(payload.length);
        buf.put(payload);
        pipe.write(buf.array());
    }

    /** Читает один фрейм из Named Pipe (блокирующее чтение с поллингом). */
    private Frame readFrame() throws IOException {
        // Читаем 8-байтный заголовок
        byte[] header = new byte[8];
        int totalRead = 0;
        while (totalRead < 8) {
            if (closed.get()) return null;
            // Windows Named Pipe не поддерживает блокирующий available(),
            // поэтому используем поллинг с небольшой задержкой
            long available = pipe.length() - pipe.getFilePointer();
            if (available <= 0) {
                try { Thread.sleep(20); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                continue;
            }
            int n = pipe.read(header, totalRead, 8 - totalRead);
            if (n == -1) throw new IOException("Pipe closed");
            totalRead += n;
        }

        ByteBuffer headerBuf = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
        OpCode opCode = OpCode.a(headerBuf.getInt());
        int length = headerBuf.getInt();

        JsonObject data = null;
        if (length > 0) {
            byte[] payload = new byte[length];
            int payloadRead = 0;
            while (payloadRead < length) {
                int n = pipe.read(payload, payloadRead, length - payloadRead);
                if (n == -1) throw new IOException("Pipe closed during payload read");
                payloadRead += n;
            }
            String json = new String(payload, StandardCharsets.UTF_8);
            try {
                data = new com.google.gson.Gson().fromJson(json, JsonObject.class);
            } catch (Exception e) {
                LOG.f("Failed to parse Discord frame JSON: {}", e.getMessage());
            }
        }

        return new Frame(opCode != null ? opCode : OpCode.FRAME, data != null ? data : new JsonObject());
    }

    /** Планирует переподключение через 5 секунд если клиент не закрыт. */
    private void scheduleReconnect() {
        if (closed.get()) return;
        closePipe();
        LOG.a("Scheduling Discord RPC reconnect in 5s...");
        reconnectScheduler.schedule(() -> {
            if (!closed.get()) {
                connected.set(false);
                connect(); // readyFuture здесь нам не важен
            }
        }, 5, TimeUnit.SECONDS);
    }

    private void closePipe() {
        if (pipe != null) {
            try { pipe.close(); } catch (IOException ignored) {}
            pipe = null;
        }
    }
}
