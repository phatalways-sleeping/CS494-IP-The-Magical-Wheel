package com.example.the_magic_wheel.sockets.Server.manager;

import com.example.the_magic_wheel.sockets.Server.defense.AttackException;

public abstract interface ExecutionManager {
    public abstract void execute(Handler task) throws AttackException;
    public abstract boolean isTerminated();
    public abstract void shutdown();
}
