package platform.client.utils.player;

import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import net.minecraft.client.Minecraft;
import platform.client.Delta;
import platform.client.utils.render.ColorUtil;
import platform.api.utils.notification.Notification;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class CrashGuard {
    private static final String[] PACK_KEYWORDS = {
            "shader", "glsl", "compil", "model", "bakedmodel", "texture", "atlas", "sprite",
            "resourcepack", "resource pack", "ресурспак", "json", "mcmeta", "post chain",
            "postchain", "missing texture", "shaders/", "models/", "textures/", "blockstates/",
            "font", "sounds.json", "zip", "deflate", "lang"
    };
    private static final int MAX_SAME_SIGNATURE = 30;

    private static final Map<String, int[]> SEEN = new ConcurrentHashMap<>();

    private CrashGuard() {}

    public static boolean shouldIntercept(Throwable t) {
        if (t == null) return false;
        if (t instanceof OutOfMemoryError) return false;
        String message = String.valueOf(t.getMessage()).toLowerCase();
        StringBuilder stackText = new StringBuilder(message).append('\n');
        StackTraceElement[] stack = t.getStackTrace();
        for (int i = 0; i < Math.min(stack.length, 25); i++) {
            stackText.append(stack[i]).append('\n');
        }
        for (Throwable cause = t.getCause(); cause != null && cause != t; cause = cause.getCause()) {
            stackText.append(String.valueOf(cause.getMessage()).toLowerCase()).append('\n');
            for (StackTraceElement e : cause.getStackTrace()) {
                stackText.append(e).append('\n');
                if (stackText.length() > 8000) break;
            }
            if (stackText.length() > 8000) break;
        }
        String full = stackText.toString();
        for (String keyword : PACK_KEYWORDS) {
            if (full.contains(keyword)) return true;
        }
        return false;
    }


    /**
     * Универсальный перехват краша: гасит любое исключение игрового цикла,
     * сохраняет лог в delta/crash-guard и показывает уведомление сбоку.
     * Возвращает true, если игра может продолжить работу; false — если краш
     * должен пройти честно (OOM или ошибка повторилась слишком много раз).
     */
    public static boolean guard(String phase, Throwable t) {
        return intercept(phase, t, null);
    }

    /**
     * То же самое, но для уже собранного ванильного отчёта — в лог дописывается
     * полный отчёт Minecraft (системные данные, мир и т.д.).
     */
    public static boolean guard(String phase, CrashReport report) {
        if (report == null || report.getException() == null) return false;
        return intercept(phase, report.getException(), report);
    }

    private static boolean intercept(String phase, Throwable t, CrashReport vanillaReport) {
        if (t == null) return false;
        if (t instanceof OutOfMemoryError) return false;

        String signature = t.getClass().getName() + "|" + topFrame(t);
        int[] stats = SEEN.computeIfAbsent(signature, k -> new int[]{0});
        stats[0]++;
        if (stats[0] > MAX_SAME_SIGNATURE) {

            if (stats[0] == MAX_SAME_SIGNATURE + 1) {
                System.err.println("[CrashGuard] ошибка '" + signature + "' повторилась " + MAX_SAME_SIGNATURE
                        + "+ раз, прекращаю перехват (даю игре крашнуться честно)");
            }
            return false;
        }

        System.err.println("[CrashGuard] перехвачена ошибка в фазе '" + phase + "': " + t);

        boolean packError = shouldIntercept(t);
        String reportText = buildReport(phase, t);
        if (vanillaReport != null) {
            try {
                reportText += "\n\n=== Отчёт Minecraft ===\n"
                        + vanillaReport.getFriendlyReport(ReportType.CRASH);
            } catch (Throwable ignored) {
            }
        }

        File file = saveToFile(reportText);
        if (stats[0] == 1) copyToClipboard(reportText);
        notifyUser(file, packError);
        return true;
    }

    private static String buildReport(String phase, Throwable t) {
        StringWriter sw = new StringWriter();
        sw.append("=== Delta CrashGuard ===\n");
        sw.append("Фаза: ").append(phase).append('\n');
        sw.append("Время: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append('\n');
        sw.append("Игра НЕ крашнулась, ошибка перехвачена.\n\n");
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static String topFrame(Throwable t) {
        StackTraceElement[] stack = t.getStackTrace();
        return stack.length > 0 ? stack[0].toString() : "unknown";
    }

    private static File saveToFile(String report) {
        try {
            Minecraft mc = Minecraft.getInstance();
            File dir = new File(mc.gameDirectory, "delta/crash-guard");
            if (!dir.exists()) dir.mkdirs();
            String name = "crash_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".txt";
            File file = new File(dir, name);
            java.nio.file.Files.writeString(file.toPath(), report, StandardCharsets.UTF_8);
            return file;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void copyToClipboard(String report) {
        try {
            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(report);
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        } catch (Throwable ignored) {
        }
    }

    private static void notifyUser(File file, boolean packError) {
        try {
            if (Delta.h() == null || Delta.h().d() == null) return;
            String text = packError
                    ? "[Guard] Ошибка ресурспака — краш предотвращён"
                    : "[Guard] Игра могла крашнуться — краш предотвращён";
            if (packError) {
                if (file != null) text += " (лог скопирован)";
            } else {
                if (file != null) text += " (лог в delta/crash-guard)";
            }
            Delta.h().d().m().a(new Notification("!", ColorUtil.a(255, 60, 60, 255), text, 6000));
        } catch (Throwable ignored) {
        }
    }
}
