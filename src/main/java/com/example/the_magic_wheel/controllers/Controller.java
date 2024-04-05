package com.example.the_magic_wheel.controllers;

import com.example.the_magic_wheel.protocols.response.Response;

/**
 * Controller
 */
public interface Controller {

    void handleResponse(Response response);
}