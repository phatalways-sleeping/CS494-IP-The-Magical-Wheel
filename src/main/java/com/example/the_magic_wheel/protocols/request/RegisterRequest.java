package com.example.the_magic_wheel.protocols.request;

import java.util.Iterator;
import java.util.List;

public class RegisterRequest extends Request {
    public RegisterRequest(String username) {
        super("REGISTER " + username);
    }

    @Override
    public Iterator<String> iterator() {
        List<String> list = List.of(content.split(" ")).subList(1, 2);
        return list.iterator();
    }
}
