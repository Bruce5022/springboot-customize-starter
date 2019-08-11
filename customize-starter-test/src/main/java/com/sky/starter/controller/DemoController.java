package com.sky.starter.controller;

import com.sky.starter.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
    @Autowired
    private HelloService helloService;

    @RequestMapping("/demo/{name}")
    public Object demo(@PathVariable("name") String name){

        return helloService.sayHello(name);
    }
}
