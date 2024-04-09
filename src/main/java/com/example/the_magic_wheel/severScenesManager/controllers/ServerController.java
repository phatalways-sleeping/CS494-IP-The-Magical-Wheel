package com.example.the_magic_wheel.severScenesManager.controllers;
import com.example.the_magic_wheel.sockets.Server.ServerApp;
import com.example.the_magic_wheel.protocols.response.Response;

public class ServerController {
    protected ServerApp serverApp;

    public ServerController(ServerApp serverApp) {
        this.serverApp = serverApp;
    }
    
    public void handleResponse(Response response) {};
}