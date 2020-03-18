package com.example.reactive.controller;

import com.example.reactive.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ImageController {
    @Autowired
    private ImageService imageService;

    @Bean
    RouterFunction<ServerResponse> images() {
        return route(POST("images/upload"), imageService::uploadImage)
                .andRoute(DELETE("images/{imageId}"), imageService::deleteImage)
                ;

    }
}
