package com.example.the_magic_wheel.protocols.response;

import java.util.Iterator;
import java.util.List;

public class RegisterSuccessResponse extends Response {
    public RegisterSuccessResponse(String username, int order, String requestedAt) {
        super("REGISTER_SUCCESS " + username + " " + order, requestedAt);
    }

    @Override
    public Iterator<String> iterator() {
        return List.of(content.split(" ")).subList(1, 3).iterator();
    }
}
