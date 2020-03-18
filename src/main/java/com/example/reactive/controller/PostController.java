package com.example.reactive.controller;

import com.example.reactive.service.PostService;
import com.example.reactive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class PostController {
    @Autowired
    private PostService postService;

    @Bean
    RouterFunction<ServerResponse> posts() {
        return route(GET("posts/users/{userId}"), postService::listPostsOfUsers)
                .andRoute(POST("posts/users/{userId}"), postService::createPost)
                .andRoute(DELETE("posts/{postId}"), postService::deletePost)
                .andRoute(PUT("posts/like/{postId}"), postService::likePost)
                .andRoute(PUT("posts/tag/{postId}"), postService::tagPost)
                .andRoute(PUT("posts/comment/{postId}"), postService::commentPost)
                ;
    }
}
