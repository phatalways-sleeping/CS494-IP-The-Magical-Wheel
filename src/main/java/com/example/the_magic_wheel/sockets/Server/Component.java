package com.example.the_magic_wheel.sockets.Server;

import com.example.the_magic_wheel.protocols.Event;

public interface Component {
    public void setMediator(GameMediator mediator);
    public void notify(Event event);
}
