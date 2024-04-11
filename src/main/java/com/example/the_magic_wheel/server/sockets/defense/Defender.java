package com.example.the_magic_wheel.server.sockets.defense;

import com.example.the_magic_wheel.server.sockets.manager.RequestInformation;

public abstract class Defender {
    protected Defender nextDefender;

    public abstract boolean inspect(RequestInformation requestInformation);

    public void chain(Defender nextDefender) {
        this.nextDefender = nextDefender;
    }
}
