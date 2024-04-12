package com.example.the_magic_wheel.protocols.response;

import com.example.the_magic_wheel.protocols.interfaces.Event;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

public abstract class Response extends Event {
    protected final String respondedAt;

    public static Response fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInput in = new ObjectInputStream(bis)) {
            return (Response) in.readObject();
        }
    }

    protected Response(String requestedAt) {
        super(requestedAt);
        this.respondedAt = new java.util.Date().toString();
    }

    public String getRespondedAt() {
        return respondedAt;
    }

    @Override
    public String toString() {
        return "Response{" +
                "respondedAt='" + respondedAt + '\'' +
                ", requestedAt='" + requestedAt + '\'' +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                '}';
    }
}
