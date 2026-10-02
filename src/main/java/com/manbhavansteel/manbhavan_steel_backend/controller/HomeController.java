package com.manbhavansteel.manbhavan_steel_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Manbhavan Steel Backend is Running Successfully!";
    }
}