package com.example.the_magic_wheel.sockets.Server.manager;

public abstract interface Handler  extends Runnable {
    // Handle the request, may be blocked due to accessing shared resources
    // game controller
    public abstract void handle() throws Exception;
}
