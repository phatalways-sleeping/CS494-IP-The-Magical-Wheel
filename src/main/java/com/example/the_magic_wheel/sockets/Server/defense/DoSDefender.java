package com.example.the_magic_wheel.sockets.Server.defense;

import java.util.Objects;

import com.example.the_magic_wheel.sockets.Server.manager.RequestInformation;

public class DoSDefender extends Defender {
    private final int MAX_REQUESTS = 10;
    private final int MAX_REQUESTS_INTERVAL = 1000; // milliseconds
    public DoSDefender() {
    }

    @Override
    public boolean inspect(RequestInformation requestInformation) {
        if (Objects.isNull(requestInformation)) {
            return true;
        }
        final boolean isDoS = requestInformation.getLastRequestTime().getTime() + MAX_REQUESTS_INTERVAL > System
                .currentTimeMillis()
                && requestInformation.getRequests() > MAX_REQUESTS;
        if (isDoS) {
            System.out.println("DoSDefender: Detected DoS attack from " + requestInformation.getSource());
            return false;
        }
        return Objects.isNull(nextDefender) || nextDefender.inspect(requestInformation);
    }
}
