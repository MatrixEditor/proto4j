package de.proto4j.annotation.threding; //@date 27.01.2022

import java.util.concurrent.ThreadFactory;

public class DefaultParallelExecutor implements ParallelExecutor {

    @Override
    public void execute(ThreadFactory factory, Runnable command) {
        if (factory != null && command != null) {
            Thread t = factory.newThread(command);
            if (t != null) {
                t.start();
            }
        }
    }

}
