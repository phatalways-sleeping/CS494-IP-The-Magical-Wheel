package com.example.the_magic_wheel.sockets.Server.defense;

public abstract class AttackException  extends Exception {
    private String source;

    public AttackException(String source) {
        super("Attack detected from " + source);
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
