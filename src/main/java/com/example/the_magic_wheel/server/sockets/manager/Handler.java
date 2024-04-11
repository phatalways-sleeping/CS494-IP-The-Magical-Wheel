package com.example.the_magic_wheel.server.sockets.manager;

public abstract interface Handler  extends Runnable {
    // Handle the request, may be blocked due to accessing shared resources
    // game controller
    public abstract void handle() throws Exception;

    public abstract String getSource();
}
