package com.example.the_magic_wheel.protocols.request;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import com.example.the_magic_wheel.protocols.ToBytes;

import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.IOException;

public abstract class Request implements Serializable, ToBytes {
    protected final String requestedAt;
    protected final String content;

    public static Request fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
                ObjectInput objectStream = new ObjectInputStream(byteStream)) {
            return (Request) objectStream.readObject();
        }
    }

    protected Request(String content) {
        this.requestedAt = new Date().toString();
        this.content = content;
        // this.type = type;
    }

    // Extract the content of the request
    // ignore the RequestType
    public abstract Iterator<String> iterator();

    public String getRequestedAt() {
        return requestedAt;
    }

    public String getContent() {
        return content;
    }
}
