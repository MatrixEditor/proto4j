package de.proto4j.internal; //@date 23.01.2022

import de.proto4j.internal.model.BoundConfiguration;
import de.proto4j.internal.model.Reflections;
import de.proto4j.internal.model.bean.BeanManager;
import de.proto4j.internal.model.bean.MapBeanManager;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

public class Proto4j {

    public static BoundConfiguration createEnvironment(Class<?> c) {
        if (c == null) throw new IllegalArgumentException();
        Set<Class<?>> allClasses = Reflections.getClassesFromMain(c);

        Proto4jConfiguration conf = new Proto4jConfiguration(c.getName());
        conf.setMainPackage(c.getPackage());

        for (Class<?> controller : Reflections.findRequestControllers(allClasses));
            //conf.getBeanManager().mapIfAbsent(controller, RequestController.class);

        return conf;
    }

    public static Object runServer(BoundConfiguration conf) {
        return null;
    }

    /*
    Class<?>[] classes = conf.loadedControllers().toArray(Class<?>[]::new);
        Method     m       = app.getRequestMethod(classes[0], input);

        if (m != null) {
            Object[] args_ = app.createArgs(m.getParameterAnnotations(), input);
            if (args_.length == 0) System.out.println(m.invoke(classes[0].getDeclaredConstructor().newInstance()));
            else System.out.println(m.invoke(classes[0].getDeclaredConstructor().newInstance(), args_));
        }
     */

    private static class Proto4jConfiguration implements BoundConfiguration {
        private final BeanManager beanManager = new MapBeanManager();

        private final String  name;
        private       Package mainPackage;

        public Proto4jConfiguration(String name) {
            this.name = name;
        }

        @Override
        public String getConfigurationName() {
            return name;
        }

        @Override
        public BeanManager getBeanManager() {
            return beanManager;
        }

        @Override
        public Package getPackage() {
            return mainPackage;
        }

        public void setMainPackage(Package mainPackage) {
            this.mainPackage = mainPackage;
        }
    }
}
