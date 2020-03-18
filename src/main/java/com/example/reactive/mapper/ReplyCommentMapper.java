package com.example.reactive.mapper;

import com.example.reactive.AppConstants;
import com.example.reactive.dto.CommentRequest;
import com.example.reactive.model.Comment;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public class ReplyCommentMapper {
    public static Map<String, AttributeValue> toMap(String fullPostId, String fullCommentId, CommentRequest commentRequest) {
        return Map.of(
                        AppConstants.PARTITION_KEY, AttributeValue.builder().s(fullPostId).build(),
                        AppConstants.SOFT_KEY, AttributeValue.builder().s(fullCommentId + AppConstants.REPLY_PREFIX + commentRequest.getId()).build(),
                        "text", AttributeValue.builder().s(commentRequest.getText()).build(),
                        "user_id", AttributeValue.builder().s(commentRequest.getUser_id()).build()
                );
    }
}