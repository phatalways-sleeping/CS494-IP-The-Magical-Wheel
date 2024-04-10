package com.example.the_magic_wheel.controllers;

import com.example.the_magic_wheel.ClientApp;
import com.example.the_magic_wheel.protocols.response.Response;

/**
 * Controller
 */
public class Controller {
    protected ClientApp app;

    public Controller(ClientApp app) {
        this.app = app;
    }
    
    public void handleResponse(Response response) {};

}