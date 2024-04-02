package com.example.the_magic_wheel.protocols.response;

import java.util.Iterator;
import java.util.List;

public class RegisterFailureResponse extends Response {

    public RegisterFailureResponse(String reason, String requestedAt) {
        super("REGISTER_FAILURE " + reason, requestedAt);
    }

    @Override
    public Iterator<String> iterator() {
        return List.of(content.split(" ")).subList(1, 2).iterator();
    }

}
