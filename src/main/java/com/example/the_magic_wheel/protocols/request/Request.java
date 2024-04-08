package com.example.the_magic_wheel.protocols.request;

import java.util.Date;
import com.example.the_magic_wheel.protocols.Event;
import java.io.ByteArrayInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.IOException;

public abstract class Request extends Event {
    public static Request fromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
                ObjectInput objectStream = new ObjectInputStream(byteStream)) {
            return (Request) objectStream.readObject();
        }
    }

    protected Request() {
        super(new Date().toString());
    }

    @Override
    public String toString() {
        return "Request{" + "requestedAt='" + requestedAt + '\'' + ", source='" + source + '\'' + ", destination='"
                + destination + '\'' + '}';
    }
}
