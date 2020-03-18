package com.example.reactive.controller;

import com.example.reactive.service.AuthService;
import com.example.reactive.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AuthController {
    @Autowired
    private AuthService authService;

    @Bean
    RouterFunction<ServerResponse> auths() {
        return route(POST("auth/signin"), authService::signin)
                .andRoute(POST("auth/signup"), authService::signup)
                ;
    }
}
