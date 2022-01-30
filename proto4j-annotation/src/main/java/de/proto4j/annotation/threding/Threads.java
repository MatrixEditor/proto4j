package de.proto4j.annotation.threding; //@date 30.01.2022

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class Threads {

    private Threads() {}

    public static void executeParallel(Runnable e) {
        if (e != null) {
            ParallelExecutor pe = newParallelThreadExecutor();
            pe.execute(e);
        }
    }

    public static <T> T supplyParallel(Supplier<T> s) {
        if (s != null) {
            ParallelSupplier ps = newSingleThreadSupplier();
            return ps.supplyAsync(s);
        }
        return null;
    }

    public static ParallelExecutor newParallelThreadExecutor() {
        return newParallelThreadExecutor(Thread::new);
    }

    public static ParallelExecutor newParallelThreadExecutor(ThreadFactory factory) {
        return new SingleThreadExecutor(factory);
    }

    public static ParallelSupplier newSingleThreadSupplier() {
        return newSingleThreadSupplier(Thread::new);
    }

    public static ParallelSupplier newSingleThreadSupplier(ThreadFactory factory) {
        return new SingleThreadSupplier(factory);
    }

    private static class SingleThreadSupplier implements ParallelSupplier {

        private final ThreadFactory factory;

        private volatile boolean finished = false;

        private SingleThreadSupplier(ThreadFactory factory) {this.factory = factory;}

        @Override
        public <T> T supplyAsync(Supplier<T> s) {
            if (s != null) {
                finished = false;
                AtomicReference<T> ref = new AtomicReference<>();

                Thread t = factory.newThread(() -> {
                    ref.set(s.get());
                    finished = true;
                });

                if (t != null) {
                    t.start();
                }
                while (!finished) {
                    Thread.onSpinWait();
                }
                return ref.get();
            }
            return null;
        }
    }


    private static class SingleThreadExecutor implements ParallelExecutor {

        private final ThreadFactory factory;

        private SingleThreadExecutor(ThreadFactory factory) {this.factory = factory;}

        @Override
        public void execute(Runnable command) {
            if (command != null && factory != null) {
                Thread t = factory.newThread(command);
                if (t != null) {
                    t.start();
                }
            }
        }
    }
}
