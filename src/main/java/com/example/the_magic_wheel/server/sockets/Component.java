package com.example.the_magic_wheel.server.sockets;

public abstract class Component {
    protected GameMediator mediator;
    public void setMediator(GameMediator mediator) {
        this.mediator = mediator;
    }
}
