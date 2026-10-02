package platform.client.utils.lib.jsoup;

import platform.client.utils.lib.jsoup.Jsoup;
import platform.client.utils.lib.jsoup.Connection_2;

public final class Jsoup {
    private Jsoup() {
    }

    public static Document a(String html) {
        return new Document(org.jsoup.Jsoup.parse(html));
    }

    public static Connection_2 b(String url) {
        return Connection_2.b(url);
    }
}



