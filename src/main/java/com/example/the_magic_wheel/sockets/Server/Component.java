package com.example.the_magic_wheel.sockets.Server;

public abstract class Component {
    protected GameMediator mediator;
    public void setMediator(GameMediator mediator) {
        this.mediator = mediator;
    }
}
