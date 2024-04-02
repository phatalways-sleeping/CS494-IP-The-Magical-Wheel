package com.example.the_magic_wheel.protocols.response;

import java.io.Serializable;
import java.util.Iterator;

import com.example.the_magic_wheel.protocols.ToBytes;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.IOException;

public abstract class Response implements Serializable, ToBytes {
    protected final String respondedAt;
    protected final String requestedAt;
    protected final String content;

    public static Response fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInput in = new ObjectInputStream(bis)) {
            return (Response) in.readObject();
        }
    }

    public abstract Iterator<String> iterator();

    protected Response(String content, String requestedAt) {
        this.respondedAt = new java.util.Date().toString();
        this.requestedAt = requestedAt;
        this.content = content;
    }

    public String getRespondedAt() {
        return respondedAt;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public String getContent() {
        return content;
    }
}
