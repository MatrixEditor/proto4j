package de.proto4j.annotation.server.threding;//@date 27.01.2022

import java.util.concurrent.ThreadFactory;

public interface ParallelExecutor {

    public void execute(ThreadFactory factory, Runnable command);
}
