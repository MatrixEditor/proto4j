package de.proto4j.network.objects.client; //@date 29.01.2022

import de.proto4j.internal.model.bean.BeanManager;
import de.proto4j.internal.model.bean.UnmodifiableBeanManager;
import de.proto4j.network.objects.TypeContext;
import de.proto4j.stream.SequenceStream;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientContext implements TypeContext {

    private final ObjectClient  client;
    private final BeanManager   manager;
    private final Set<Class<?>> classes;
    private final Class<?>      main;
    private final List<String> conf;

    ClientContext(ObjectClient client, BeanManager manager, SequenceStream<Class<?>> classes, Class<?> main,
                  List<String> conf) {
        this.client  = client;
        this.manager = UnmodifiableBeanManager.of(manager);
        this.classes = new HashSet<>();
        this.main    = main;
        this.conf    = conf;

        classes.forEach(this.classes::add);
    }

    public ObjectClient getClient() {
        return client;
    }

    @Override
    public BeanManager getBeanManager() {
        return manager;
    }

    @Override
    public Set<Class<?>> loadedClasses() {
        return classes;
    }

    @Override
    public Class<?> mainClass() {
        return main;
    }

    public List<String> getConfiguration() {
        return conf;
    }
}
