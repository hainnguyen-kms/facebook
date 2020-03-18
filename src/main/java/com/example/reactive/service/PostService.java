package com.example.reactive.service;


import com.example.reactive.dto.CommentRequest;
import com.example.reactive.dto.FriendRequestDTO;
import com.example.reactive.dto.LikeRequest;
import com.example.reactive.dto.TagRequest;
import com.example.reactive.model.Post;
import com.example.reactive.model.User;
import com.example.reactive.repository.PostRepository;
import com.example.reactive.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    public Mono<ServerResponse> listPostsOfUsers(ServerRequest serverRequest) {
        String userId = serverRequest.pathVariable("userId");
        return postRepository.listPostsOfUser(userId)
                .collect(Collectors.toList())
                .flatMap(posts -> ServerResponse.ok().body(BodyInserters.fromValue(posts)));
    }

    public Mono<ServerResponse> createPost(ServerRequest serverRequest) {
        String userId = serverRequest.pathVariable("userId");
        return serverRequest.bodyToMono(Post.class)
                .flatMap(post -> postRepository.createPost(userId, post))
                .flatMap(posts -> ServerResponse.ok().body(BodyInserters.fromValue(posts)));
    }

    public Mono<ServerResponse> likePost(ServerRequest serverRequest) {
        String postId = serverRequest.pathVariable("postId");
        return serverRequest.bodyToMono(LikeRequest.class)
                .flatMap(likeRequest -> postRepository.likePost(postId, likeRequest))
                .flatMap(res -> ServerResponse.ok().body(BodyInserters.fromValue(res)));
    }

    public Mono<ServerResponse> tagPost(ServerRequest serverRequest) {
        String postId = serverRequest.pathVariable("postId");
        return serverRequest.bodyToMono(TagRequest.class)
                .flatMap(tagRequest -> postRepository.tagPost(postId, tagRequest))
                .flatMap(res -> ServerResponse.ok().body(BodyInserters.fromValue(res)));
    }

    public Mono<ServerResponse> commentPost(ServerRequest serverRequest) {
        String postId = serverRequest.pathVariable("postId");
        return serverRequest.bodyToMono(CommentRequest.class)
                .flatMap(tagRequest -> postRepository.commentPost(postId, tagRequest))
                .flatMap(res -> ServerResponse.ok().body(BodyInserters.fromValue(res)));
    }

    public Mono<ServerResponse> deletePost(ServerRequest serverRequest) {
        return Mono.just(serverRequest.pathVariable("postId"))
                .flatMap((postId) -> postRepository.deletePost(postId))
                .flatMap((res) -> ServerResponse.ok().build());
    }
}