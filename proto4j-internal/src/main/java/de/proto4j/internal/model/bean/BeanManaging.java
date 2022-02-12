package de.proto4j.internal.model.bean; //@date 12.02.2022

import de.proto4j.annotation.http.requests.HttpRequestController;
import de.proto4j.annotation.server.requests.Controller;

import java.lang.annotation.Annotation;
import java.util.Objects;

public final class BeanManaging {

    private BeanManaging() {}

    public static void mapHttpController(BeanManager bm, Class<?> c) {
        Objects.requireNonNull(bm);
        bm.mapIfAbsent(c, HttpRequestController.class);
    }

    public static void mapController(BeanManager bm, Class<?> c) {
        Objects.requireNonNull(bm);
        bm.mapIfAbsent(c, Controller.class);
    }

    public static boolean filterController(Class<? extends Annotation> a) {
        return a == Controller.class;
    }
}
