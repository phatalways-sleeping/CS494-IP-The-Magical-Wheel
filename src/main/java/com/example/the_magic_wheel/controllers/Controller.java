package com.example.the_magic_wheel.controllers;

import com.example.the_magic_wheel.App;
import com.example.the_magic_wheel.protocols.response.Response;

/**
 * Controller
 */
public class Controller {
    protected App app;

    public Controller(App app) {
        this.app = app;
    }
    
    public void handleResponse(Response response) {};

}