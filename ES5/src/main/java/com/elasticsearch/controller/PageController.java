package com.elasticsearch.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping(value = "/page")
public class PageController {

    @RequestMapping(value = "/test")
    public String Page() {
        return "/estest/estest";
    }

}
