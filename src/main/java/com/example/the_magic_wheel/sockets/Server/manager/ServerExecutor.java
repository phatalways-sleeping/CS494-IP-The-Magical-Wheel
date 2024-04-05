package com.example.the_magic_wheel.sockets.Server.manager;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerExecutor implements ExecutionManager {
    private final ExecutorService executor;

    public ServerExecutor(int poolSize) {
        this.executor = Executors.newFixedThreadPool(Objects.requireNonNullElse(poolSize, 5));
    }

    @Override
    public void execute(Handler task) {
        this.executor.execute(task);
    }

    @Override
    public void shutdown() {
        this.executor.shutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.executor.isTerminated();
    }
}
