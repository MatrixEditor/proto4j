package de.proto4j.annotation; //@date 12.02.2022


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

public class AnnotationLookup {

    public static final String CONF_BY_VALUE = "byValue";

    public static final String CONF_BY_CONNECTION = "byConnection";

    public static final String CONF_IGNORE_VALUES = "ignoreValues";

    public static final String CONF_THREAD_POOL = "threadPooling";

    public static class ConfigurationLookup {
        public static boolean areValuesIgnored(Class<?> c) {
            return hasConfiguration(c, CONF_IGNORE_VALUES);
        }

        public static boolean isByConnection(Class<?> c) {
            return hasConfiguration(c, CONF_BY_CONNECTION);
        }

        public static boolean isByValue(Class<?> c) {
            return hasConfiguration(c, CONF_BY_VALUE);
        }

        public static boolean hasConfiguration(Class<?> c, String conf) {
            if (c == null) throw new NullPointerException("main-class can not be null");
            if (Markup.isConfiguration(c)) {
                for (String s : Markup.getConfigurationMarkup(c).value()) {
                    if (s.equals(conf)) return true;
                }
            }
            return false;
        }
    }

    public static class HandlerLookup {
        public static boolean isServerRequestHandler(Method m) {
            Objects.requireNonNull(m);

            return !Modifier.isStatic(m.getModifiers()) && Markup.isRequestHandler(m);
        }
    }
}
