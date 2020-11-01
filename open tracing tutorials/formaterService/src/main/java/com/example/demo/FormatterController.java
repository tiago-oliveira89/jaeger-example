package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FormatterController {

    @GetMapping("/{name}")
    public String sayHello(@PathVariable("name") String name) {
        String helloStr = String.format("Hello, %s!", name);
        return helloStr;
    }
}
