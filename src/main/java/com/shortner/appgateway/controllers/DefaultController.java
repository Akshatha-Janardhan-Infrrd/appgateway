package com.shortner.appgateway.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {

    @GetMapping("/defaultFallback")
    public String defaultMessage()
    {
        return "There were some error in connecting. Please try again later.";
    }
}
