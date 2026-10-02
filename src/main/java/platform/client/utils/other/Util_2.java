package platform.client.utils.other;

public class Util_2 {
    private Util_2() {
    }

    public static String a(String key) {
        if (key == null) {
            throw new IllegalArgumentException("null input");
        }
        String result = null;
        try {
            result = System.getProperty(key);
        } catch (SecurityException e) {
        }
        return result;
    }

    public static boolean b(String key) {
        String value = a(key);
        if (value == null) {
            return false;
        }
        return value.equalsIgnoreCase(BooleanUtils.e);
    }

    public static Class<?> a() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String thisClassName = Util_2.class.getName();
        int i = 0;
        while (i < trace.length && !trace[i].getClassName().equals(thisClassName)) {
            i++;
        }
        if (i + 2 >= trace.length) {
            return null;
        }
        try {
            return Class.forName(trace[i + 2].getClassName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static final void a(String msg, Throwable t) {
        System.err.println(msg);
        System.err.println("Reported exception:");
        t.printStackTrace();
    }

    public static final void c(String msg) {
        System.err.println("SLF4J: " + msg);
    }
}



