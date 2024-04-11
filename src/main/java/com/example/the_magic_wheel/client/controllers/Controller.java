package com.example.the_magic_wheel.client.controllers;

import com.example.the_magic_wheel.client.views.App;
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