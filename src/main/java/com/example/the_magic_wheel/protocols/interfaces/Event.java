package com.example.the_magic_wheel.protocols.interfaces;

import java.io.Serializable;
import java.util.Objects;

public abstract class Event implements ToBytes, Serializable {
    protected final String requestedAt;
    protected String source;
    protected String destination;

    protected Event(String requestedAt) {
        this.requestedAt = Objects.requireNonNull(requestedAt);
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
}
