package com.example.the_magic_wheel.sockets.Server.manager;

public abstract interface ExecutionManager {
    public abstract void execute(Handler task);
    public abstract boolean isTerminated();
    public abstract void shutdown();
}
