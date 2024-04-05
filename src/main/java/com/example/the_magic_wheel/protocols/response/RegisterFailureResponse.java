package com.example.the_magic_wheel.protocols.response;

public class RegisterFailureResponse extends Response {

    private final String reason;

    public RegisterFailureResponse(String reason, String requestedAt) {
        super(requestedAt);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
