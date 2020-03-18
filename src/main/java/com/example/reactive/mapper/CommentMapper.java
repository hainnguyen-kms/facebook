package com.example.reactive.mapper;

import com.example.reactive.AppConstants;
import com.example.reactive.dto.CommentRequest;
import com.example.reactive.dto.TagRequest;
import com.example.reactive.model.Comment;
import com.example.reactive.model.Tag;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommentMapper {
    public static Comment fromMap(Map<String, AttributeValue> attributeValueMap, Integer offset) {
        Comment comment = new Comment();
        comment.setUser_id(attributeValueMap.get("user_id").s());
        comment.setText(attributeValueMap.get("text").s());
        comment.setId(attributeValueMap.get(AppConstants.SOFT_KEY).s().substring(offset));
        return comment;
    }

    public static Map<String, AttributeValue> toMap(String postId, CommentRequest commentRequest) {
        return Map.of(
                        AppConstants.PARTITION_KEY, AttributeValue.builder().s(AppConstants.POST_PREFIX + postId).build(),
                        AppConstants.SOFT_KEY, AttributeValue.builder().s(AppConstants.COMMENT_PREFIX + commentRequest.getId()).build(),
                        "text", AttributeValue.builder().s(commentRequest.getText()).build(),
                        "user_id", AttributeValue.builder().s(commentRequest.getUser_id()).build()
                );
    }
}