package com.example.the_magic_wheel.sockets.Server.defense;

import com.example.the_magic_wheel.sockets.Server.manager.RequestInformation;

public abstract interface Defender {
    public abstract boolean inspect(RequestInformation requestInformation);
    public abstract void chain(Defender nextDefender);
}
