package com.example.the_magic_wheel.protocols.request;

import java.util.Iterator;
import java.util.List;

public class CloseConnectionRequest extends Request {
    public CloseConnectionRequest(String username) {
        super("CLOSE_CONNECTION " + username);
    }

    @Override
    public Iterator<String> iterator() {
        return List.of(content.split(" ")).subList(1, 2).iterator();
    }
}
