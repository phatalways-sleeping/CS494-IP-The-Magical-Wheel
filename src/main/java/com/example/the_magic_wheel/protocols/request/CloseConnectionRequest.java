package com.example.the_magic_wheel.protocols.request;


public class CloseConnectionRequest extends Request {
    private final String username;

    public CloseConnectionRequest(String username) {
        super();
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
