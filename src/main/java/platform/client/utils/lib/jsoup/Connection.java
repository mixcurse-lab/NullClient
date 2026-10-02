package platform.client.utils.lib.jsoup;

import platform.client.utils.lib.javassist.Frame;
import java.io.IOException;

public interface Connection {
    boolean a();

    Frame b() throws IOException;

    void a(Frame frame) throws IOException;

    default void close() throws IOException {
    }
}



