package com.example.the_magic_wheel.server.controllers;
import com.example.the_magic_wheel.protocols.response.Response;
import com.example.the_magic_wheel.server.views.App;

public class ServerController {
    protected App serverApp;

    public ServerController(App serverApp) {
        this.serverApp = serverApp;
    }

    public void handleResponse(Response response) {};
}