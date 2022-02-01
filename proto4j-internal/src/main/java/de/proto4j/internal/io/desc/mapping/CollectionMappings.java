package de.proto4j.internal.io.desc.mapping; //@date 01.02.2022

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CollectionMappings {

    private static final List<Class<?>> collectionTypes =
            List.of(List.class, Set.class, LinkedList.class, Vector.class, HashSet.class,
                    ArrayList.class);

    public static Mapping<BiFunction<String, Class<?>, ?>> valueOf(Class<?> type) {
        if (!contains(type)) {
            if (type.getSimpleName().toLowerCase().contains("list")) type = LinkedList.class;
            else type = HashSet.class;
        }

        if (type == List.class || type == LinkedList.class) {
            return new CollectionMapping(LinkedList.class, LinkedList::new);
        } else {
            if (type == Set.class || type == HashSet.class) {
                return new CollectionMapping(HashSet.class, HashSet::new);
            } else {
                Class<?> finalType = type;
                return new CollectionMapping(type, () -> {
                    try {
                        return (Collection<?>) finalType.getDeclaredConstructor().newInstance();
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalArgumentException("could not initialize collection");
                    }
                });
            }
        }
    }

    public static boolean contains(Class<?> valueType) {
        return collectionTypes.contains(valueType);
    }

    private static class CollectionMapping implements Mapping<BiFunction<String, Class<?>, ?>> {
        private final BiFunction<String, Class<?>, ?> invoker;

        private final Class<?> type;

        public CollectionMapping(Class<?> c, Supplier<Collection<?>> supplier) {
            type    = c;
            invoker = (s, t) -> read(s, t, supplier);
        }

        protected <E> Collection<E> read(String s, Class<E> primitiveType, Supplier<Collection<?>> supplier) {
            StringTokenizer tokenizer = new StringTokenizer(s, "|");
            //noinspection unchecked
            Collection<E> coll = (Collection<E>) supplier.get();

            while (tokenizer.hasMoreElements()) {
                //noinspection unchecked
                coll.add((E) PrimitiveMappings.valueOf(primitiveType)
                                              .getInvoker().apply(tokenizer.nextToken()));
            }
            return coll;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public BiFunction<String, Class<?>, ?> getInvoker() {
            return invoker;
        }
    }
}
