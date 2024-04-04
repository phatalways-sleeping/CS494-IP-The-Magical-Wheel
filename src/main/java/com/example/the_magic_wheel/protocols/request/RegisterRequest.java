package com.example.the_magic_wheel.protocols.request;
import java.util.Objects;

public class RegisterRequest extends Request {
    private final String username;

    public RegisterRequest(String username) {
        super();
        this.username = Objects.requireNonNull(username);
    }

    public String getUsername() {
        return username;
    }
}
