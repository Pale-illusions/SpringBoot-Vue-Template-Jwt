package com.iflove.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 苍镜月
 * @version 1.0
 * @implNote
 */
@RestController
@RequestMapping("api/test")
public class TestController {

    @GetMapping("hello")
    public String test() {
        return "Hello, World!";
    }
}
