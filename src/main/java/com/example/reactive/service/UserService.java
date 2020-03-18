package com.example.reactive.service;


import com.example.reactive.dto.FriendRequestDTO;
import com.example.reactive.model.Friend;
import com.example.reactive.model.FriendRequest;
import com.example.reactive.model.User;
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
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @NotNull
    public Mono<ServerResponse> listUsers(ServerRequest serverRequest) {
        return userRepository.listUsers()
                .collect(Collectors.toList())
                .flatMap(users -> ServerResponse.ok().body(BodyInserters.fromValue(users)));
    }

    public Mono<ServerResponse> listFriendRequestByUser(ServerRequest serverRequest) {
        String userId = serverRequest.pathVariable("userId");
        return userRepository.listFriendRequestByUser(userId)
                .collect(Collectors.toList())
                .flatMap(users -> ServerResponse.ok().body(BodyInserters.fromValue(users)));
    }

    public Mono<ServerResponse> createUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(User.class)
                .flatMap(user -> userRepository.createUser(user))
                .flatMap(user -> ServerResponse.created(URI.create("/users/" + user.getId())).build());
    }

    public Mono<ServerResponse> requestFriend(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(FriendRequestDTO.class)
                .flatMap(requestBody -> userRepository.requestFriend(requestBody))
                .flatMap((a) -> ServerResponse.created(null).build());
    }

    public Mono<ServerResponse> getNewFeed(ServerRequest serverRequest) {
        String userId = serverRequest.pathVariable("userId");
        return userRepository.getNewFeedOfUser(userId)
                .collect(Collectors.toList())
                .flatMap(users -> ServerResponse.ok().body(BodyInserters.fromValue(users)));
    }

    public Mono<ServerResponse> actionRequestFriend(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(FriendRequest.class)
                .flatMap(requestBody -> userRepository.actionRequestFriend(requestBody))
                .flatMap(result -> ServerResponse.ok().body(BodyInserters.fromValue(result)));
    }
}