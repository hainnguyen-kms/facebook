package com.example.reactive.service;


import com.example.reactive.dto.CommentRequest;
import com.example.reactive.dto.FriendRequestDTO;
import com.example.reactive.model.Comment;
import com.example.reactive.model.User;
import com.example.reactive.repository.CommentRepository;
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
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    public Mono<ServerResponse> replyComment(ServerRequest serverRequest) {
        String commentId = serverRequest.pathVariable("commentId");

        return serverRequest.bodyToMono(CommentRequest.class)
                .flatMap(replyComment -> commentRepository.replyComment(commentId, replyComment))
                .flatMap(customer -> ServerResponse.ok().body(BodyInserters.fromValue(customer)));

    }

}