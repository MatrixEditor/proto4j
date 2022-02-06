package de.proto4j.internal.logger;//@date 24.11.2021

public class PrintService {

    private static boolean DO_LOG = true;

    public static void setDoLog(boolean doLog) {
        DO_LOG = doLog;
    }

    public static Logger createLogger(Class<?> aClass) {
        if (aClass != null) {
            return (l, c, m) -> doLog(l, c, m, aClass);
        }
        return null;
    }

    private static void doLog(Logger.Level l, PrintColor pc, LogMessage m, Class<?> aClass) {
        if (!DO_LOG) return;
        System.out.printf("\033%s[%s]\033[0m {%s} (%s): %s\n", pc.getColorCode(), aClass.getSimpleName(),
                          Thread.currentThread().getName(), l.name().toUpperCase(), m.getMessage());
    }

}
