package platform.client.utils.lib.log4j;

public final class StatusLogger {
    public static final Logger x = new Logger(StatusLogger.class);

    private StatusLogger() {
    }

    public static Logger x() {
        return x;
    }
}



