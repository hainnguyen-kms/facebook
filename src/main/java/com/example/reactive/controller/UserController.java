package com.example.reactive.controller;

import com.example.reactive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserController {
    @Autowired
    private UserService userService;

    @Bean
    RouterFunction<ServerResponse> users() {
        return route(GET("/users"), userService::listUsers)
                .andRoute(POST("/users"), userService::createUser)
                .andRoute(GET("/users/friend-request/{userId}"), userService::listFriendRequestByUser)
                .andRoute(GET("/users/{userId}/newfeed"), userService::getNewFeed)
                .andRoute(POST("/users/friend-request"), userService::requestFriend)
                .andRoute(POST("/users/friend-request/action"), userService::actionRequestFriend)
                ;
    }
}
