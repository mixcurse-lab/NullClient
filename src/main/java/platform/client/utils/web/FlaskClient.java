package platform.client.utils.web;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight HTTP helper for talking to the Flask backend.
 *
 * All methods are synchronous and should be called from a background thread.
 * The server base-URL is stored in a single constant — change it once here
 * and every feature picks it up automatically.
 *
 * Endpoints used:
 *   GET  /api/history                          → List<String> of last messages
 *   POST /api/send/<username>  {message:...}   → send a chat message
 *   GET  /api/configs/<username>               → List<String> config names
 *   GET  /api/config/<username>/<name>         → raw JSON string of the config
 *   POST /api/config/<username>/<name>         → upload raw JSON body
 *   DEL  /api/config/<username>/<name>         → delete a config
 */
public final class FlaskClient {

    /** Change this to your server's address (no trailing slash). */
    public static volatile String BASE_URL = "http://localhost:8000";

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final Gson GSON = new Gson();

    private FlaskClient() {}

    // ─── Chat ────────────────────────────────────────────────────────────────

    /**
     * Returns the last ≤50 messages from the server history,
     * or an empty list on any error.
     */
    public static List<String> getHistory() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/history"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return List.of();

            List<String> result = new ArrayList<>();
            JsonArray arr = JsonParser.parseString(resp.body()).getAsJsonArray();
            for (JsonElement el : arr) {
                result.add(el.getAsString());
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * POSTs a chat message to the server.
     *
     * @return true on HTTP 200, false on any error.
     */
    public static boolean sendMessage(String username, String message) {
        try {
            String body = GSON.toJson(new MessagePayload(message));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/send/" + encode(username)))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Configs ─────────────────────────────────────────────────────────────

    /**
     * Returns the list of config names stored on the server for this user,
     * or an empty list on error.
     */
    public static List<String> listConfigs(String username) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/configs/" + encode(username)))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return List.of();

            List<String> result = new ArrayList<>();
            JsonArray arr = JsonParser.parseString(resp.body()).getAsJsonArray();
            for (JsonElement el : arr) {
                result.add(el.getAsString());
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Downloads a config from the server.
     *
     * @return the raw JSON string, or null if not found / on error.
     */
    public static String downloadConfig(String username, String configName) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/config/" + encode(username) + "/" + encode(configName)))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) return resp.body();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Uploads a config to the server.
     *
     * @param jsonBody the raw JSON string produced by ModuleProcessor.
     * @return true on success.
     */
    public static boolean uploadConfig(String username, String configName, String jsonBody) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/config/" + encode(username) + "/" + encode(configName)))
                    .timeout(Duration.ofSeconds(8))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Deletes a config from the server.
     *
     * @return true on success.
     */
    public static boolean deleteConfig(String username, String configName) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/config/" + encode(username) + "/" + encode(configName)))
                    .timeout(Duration.ofSeconds(5))
                    .DELETE()
                    .build();

            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // ─── Utility ─────────────────────────────────────────────────────────────

    /** URL-encodes a path segment (spaces → %20, etc.). */
    private static String encode(String segment) {
        return URI.create("").getRawPath() + java.net.URLEncoder.encode(segment, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    // ─── Internal DTOs ────────────────────────────────────────────────────────

    private static final class MessagePayload {
        final String message;
        MessagePayload(String message) { this.message = message; }
    }
}
