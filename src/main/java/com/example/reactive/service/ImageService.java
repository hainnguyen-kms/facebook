package com.example.reactive.service;

import com.example.reactive.dto.ImageRequest;
import com.example.reactive.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    public Mono<ServerResponse> uploadImage(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ImageRequest.class)
                .flatMap(imageRequest -> imageRepository.uploadImage(imageRequest))
                .flatMap(image -> ServerResponse.ok().build());
    }

    public Mono<ServerResponse> deleteImage(ServerRequest serverRequest) {
        return Mono.just(serverRequest.pathVariable("imageId"))
                .flatMap(imageRepository::deleteImage)
                .flatMap(image -> ServerResponse.ok().build());
    }
}