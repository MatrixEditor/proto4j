package de.proto4j.internal.logger;//@date 24.11.2021

public class PrintService {

    private static boolean DO_LOG = true;

    public static void setDoLog(boolean doLog) {
        DO_LOG = doLog;
    }

    public static Logger createLogger(Class<?> aClass) {
        if (aClass != null) {
            return (l, c, m) -> {
                if (DO_LOG)
                    System.out.printf("\033%s[%s]\033[0m {%s} (%s): %s\n",
                                      c.getColorCode(), aClass.getSimpleName(),
                                      Thread.currentThread().getName(),
                                      l.name().toUpperCase(), m.getMessage());
            };
        }
        return null;
    }

}
