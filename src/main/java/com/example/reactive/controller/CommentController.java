package com.example.reactive.controller;

import com.example.reactive.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Bean
    RouterFunction<ServerResponse> comments() {
        return route(POST("comments/{commentId}/reply"), commentService::replyComment)
                ;
    }
}
