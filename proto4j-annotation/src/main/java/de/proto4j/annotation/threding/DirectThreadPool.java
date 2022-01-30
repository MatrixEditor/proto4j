package de.proto4j.annotation.threding; //@date 30.01.2022

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DirectThreadPool implements ExecutorService, ThreadFactory {

    private static final String prefix = "thread-worker-[";

    private final Thread[] threads;

    private final int parallelism;

    private boolean terminated = false;
    private boolean isShutdown = false;

    private int counter = 0;
    private int pos = 0;

    public DirectThreadPool() {
        this(5);
    }

    public DirectThreadPool(final int p) {
        if (p < 1 || p > 10) throw new IllegalArgumentException(
                "parallelism can not be greater than 10 or lower that 0");

        parallelism = p;
        threads     = new Thread[parallelism];

    }

    @Override
    public void execute(Runnable command) {
        if (command == null) throw new NullPointerException("command should not be null");
        if (terminated || isShutdown) throw new IllegalStateException("pool is terminating or stopped");

        Thread t = newThread(command);
        if (pos == threads.length) {
            collectAndRemove();
        }
        threads[pos] = t;
        synchronized (threads) {
            threads[pos].start();
        }
    }

    @Override
    public Thread newThread(Runnable r) {
        if (r == null) throw new NullPointerException("command should not be null");
        if (terminated || isShutdown) throw new IllegalStateException("pool is terminating or stopped");

        return new Thread(null, r, makeName(), 0);
    }

    @Override
    public List<Runnable> shutdownNow() {
        isShutdown = true;
        synchronized (threads) {
            for (Thread t : threads) {
                t.interrupt();
            }
        }
        terminated = true;
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public void shutdown() {
        shutdownNow();
    }

    @Override
    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        unit.sleep(timeout);
        shutdown();
        return true;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        if (task == null) throw new NullPointerException("task is null");
        return new FutureTask<>(task);
    }

    @Override
    public Future<Boolean> submit(Runnable task) {
        if (task == null) throw new NullPointerException("task is null");
        return submit(task, true);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        if (task == null) throw new NullPointerException("task is null");
        return new FutureTask<>(() -> {
            task.run();
            return result;
        });
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return tasks.stream().map(FutureTask::new).collect(Collectors.toList());
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return invokeAll(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return invokeAll(tasks).get(0).get();
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return invokeAny(tasks);
    }

    private String makeName() {
        return prefix + (++counter) + "]";
    }

    public int getParallelism() {
        return parallelism;
    }

    private void collectAndRemove() {
        for (int j = 0; j < threads.length; j++) {
            Thread t = threads[j];
            if (t == null) {
                pos = j;
                return;
            }
            if (!t.isAlive()) {
                threads[j] = null;
                pos = j;
                return;
            }
        }
    }
}
