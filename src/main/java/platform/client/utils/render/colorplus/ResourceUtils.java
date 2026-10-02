package platform.client.utils.render.colorplus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.lwjgl.BufferUtils;

public final class ResourceUtils {
    private ResourceUtils() {}

    public static String resolve(String string) {
        ClassLoader classLoader = ResourceUtils.class.getClassLoader();
        String text = resolve3(string);
        try {
            String text2;
            try (InputStream inputStream = classLoader.getResourceAsStream(text)) {
                if (inputStream == null) {
                    throw new IllegalStateException("Resource not found: " + string);
                }
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String text3;
                    while ((text3 = bufferedReader.readLine()) != null) {
                        stringBuilder.append(text3).append('\n');
                    }
                    text2 = stringBuilder.toString();
                }
            }
            return text2;
        } catch (IOException ioException) {
            throw new RuntimeException("Failed to read resource: " + string, ioException);
        }
    }

    public static ByteBuffer resolve2(String string) {
        ClassLoader classLoader2 = ResourceUtils.class.getClassLoader();
        String text4 = resolve3(string);
        try {
            ByteBuffer byteBuffer;
            try (InputStream inputStream2 = classLoader2.getResourceAsStream(text4)) {
                if (inputStream2 == null) {
                    throw new IllegalStateException("Resource not found: " + string);
                }
                byte[] byteValues = inputStream2.readAllBytes();
                ByteBuffer byteBuffer2 = BufferUtils.createByteBuffer(byteValues.length);
                byteBuffer2.put(byteValues).flip();
                byteBuffer = byteBuffer2;
            }
            return byteBuffer;
        } catch (IOException ioException2) {
            throw new RuntimeException("Failed to read resource: " + string, ioException2);
        }
    }

    private static String resolve3(String string) {
        if (string == null) {
            throw new IllegalArgumentException("path");
        } else {
            return string.startsWith("/") ? string.substring(1) : string;
        }
    }
}
