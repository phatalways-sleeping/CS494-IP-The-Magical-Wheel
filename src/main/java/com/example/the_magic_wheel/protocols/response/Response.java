package com.example.the_magic_wheel.protocols.response;

import com.example.the_magic_wheel.protocols.Event;
import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.util.Objects;

public abstract class Response implements Event {
    protected final String respondedAt;
    protected final String requestedAt;

    protected String source; // The server's address
    protected String destination; // Null indicates a broadcast

    public static Response fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInput in = new ObjectInputStream(bis)) {
            return (Response) in.readObject();
        }
    }

    protected Response(String requestedAt) {
        this.respondedAt = new java.util.Date().toString();
        this.requestedAt = requestedAt;
    }

    public String getRespondedAt() {
        return respondedAt;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public void setSource(String source) {
        this.source = Objects.requireNonNull(source);
    }

    public void setDestination(String destination) {
        this.destination = destination;
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
