package com.example.the_magic_wheel.server.sockets.manager;

import com.example.the_magic_wheel.server.sockets.defense.Defender;
import com.example.the_magic_wheel.server.sockets.defense.DoSAttackException;
import com.example.the_magic_wheel.server.sockets.defense.DoSDefender;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public class ServerExecutor implements ExecutionManager {
    private final ExecutorService executor;

    // Prevents the server from being overwhelmed by requests from a single client
    // aka DoS attack
    private final Map<String, RequestInformation> requestsFromClients = new java.util.HashMap<>();

    private final Defender defenders;

    public ServerExecutor(int poolSize) {
        this.executor = Executors.newCachedThreadPool(
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        final Thread thread = new Thread(r);
                        thread.setName("ServerExecutor-" + thread.getId());
                        thread.setDaemon(true);
                        System.out.println("ServerExecutor: Created new thread " + thread.getName());
                        return thread;
                    }
                });
        final Defender doSDefender = new DoSDefender();
        this.defenders = doSDefender;
    }

    @Override
    public void execute(Handler task) throws DoSAttackException {
        if (Objects.isNull(task)) {
            return;
        }
        final String source = task.getSource();
        if (Objects.isNull(source)) {
            return;
        }
        if (this.requestsFromClients.containsKey(source)) {
            final RequestInformation requestInformation = this.requestsFromClients.get(source);
            // Chain of responsibility
            if (!this.defenders.inspect(requestInformation)) {
                throw new DoSAttackException(source);
            }
            requestInformation.addRequest();
        } else {
            this.requestsFromClients.put(source, new RequestInformation(source));
        }
        System.out.println("ServerExecutor: Executing task from " + source);
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
