package com.gateway.service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping
public class fallBackController {
@RequestMapping("/categoryFallBackUri")
    public Mono<String> categoryFallBack(){
    return Mono.just("Category Service Failing");
}

    @RequestMapping("/courseFallBack")
    public Mono<String> courseFallBack(){
        return Mono.just("Course Service Failing");
    }
}