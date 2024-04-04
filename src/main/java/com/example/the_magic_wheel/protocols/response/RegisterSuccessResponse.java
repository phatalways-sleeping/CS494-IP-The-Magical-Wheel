package com.example.the_magic_wheel.protocols.response;

import java.util.Objects;

public class RegisterSuccessResponse extends Response {
    private final String username;
    private final int order;

    public RegisterSuccessResponse(String username, int order, String requestedAt) {
        super(requestedAt);
        this.username = Objects.requireNonNull(username);
        this.order = Objects.requireNonNull(order);
    }

    public String getUsername() {
        return username;
    }

    public int getOrder() {
        return order;
    }
}
