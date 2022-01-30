package de.proto4j.annotation.threding; //@date 27.01.2022

public class DefaultParallelExecutor implements ParallelExecutor {

    @Override
    public void execute(Runnable command) {
        if (command != null) {
            Thread t = new Thread(command);
            t.start();
        }
    }

}
