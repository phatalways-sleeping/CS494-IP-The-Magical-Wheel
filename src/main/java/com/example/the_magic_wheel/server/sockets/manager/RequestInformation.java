package com.example.the_magic_wheel.server.sockets.manager;

import java.util.Date;


public class RequestInformation {
    private final String source;
    private Integer requests;
    private Date lastRequestTime;

    public RequestInformation(String source) {
        this.source = source;
        this.requests = 1;
        this.lastRequestTime = new Date(System.currentTimeMillis());
    }

    public String getSource() {
        return source;
    }

    public Date getLastRequestTime() {
        return lastRequestTime;
    }

    public Integer getRequests() {
        return requests;
    }

    public void addRequest() {
        this.requests++;
        this.lastRequestTime = new Date(System.currentTimeMillis());
    }
}