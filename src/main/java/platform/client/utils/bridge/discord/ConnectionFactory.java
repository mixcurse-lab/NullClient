package platform.client.utils.bridge.discord;


import platform.client.utils.lib.jsoup.Connection;
import java.io.IOException;

@FunctionalInterface
public interface ConnectionFactory {
    Connection create(String str) throws IOException;
}



