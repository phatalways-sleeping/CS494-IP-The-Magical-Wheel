package com.example.the_magic_wheel.server.sockets.manager;

import com.example.the_magic_wheel.server.sockets.defense.DoSAttackException;

public abstract interface ExecutionManager {
    public abstract void execute(Handler task) throws DoSAttackException;
    public abstract boolean isTerminated();
    public abstract void shutdown();
}
