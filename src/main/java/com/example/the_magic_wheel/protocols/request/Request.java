package com.example.the_magic_wheel.protocols.request;

import java.util.Date;
import java.util.Objects;

import com.example.the_magic_wheel.protocols.Event;
import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.IOException;

public abstract class Request implements Event {
    protected final String requestedAt;
    protected String source;
    protected String destination;

    public static Request fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
                ObjectInput objectStream = new ObjectInputStream(byteStream)) {
            return (Request) objectStream.readObject();
        }
    }

    protected Request() {
        this.requestedAt = new Date().toString();
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
        this.destination = Objects.requireNonNull(destination);
    }

    @Override
    public String toString() {
        return "Request{" + "requestedAt='" + requestedAt + '\'' + ", source='" + source + '\'' + ", destination='"
                + destination + '\'' + '}';
    }
}
