package com.example.reactive.repository;

import com.example.reactive.AppConstants;
import com.example.reactive.dto.CommentRequest;
import com.example.reactive.mapper.CommentMapper;
import com.example.reactive.mapper.ReplyCommentMapper;
import com.example.reactive.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Repository
public class CommentRepository {
    @Autowired
    private DynamoDbAsyncClient dynamoDbAsyncClient;

    public Mono<Comment> replyComment(String commentId, CommentRequest commentRequest) {
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .indexName(AppConstants.GSI_BY_SOFTKEY)
                .keyConditionExpression("soft_key = :sk")
                .expressionAttributeValues(Map.of(":sk", AttributeValue.builder().s(AppConstants.COMMENT_PREFIX + commentId).build()))
                .build();

        return Mono.fromCompletionStage(dynamoDbAsyncClient.query(queryRequest))
                .map(QueryResponse::items)
                .map(comments -> {
                    if(comments.isEmpty()) {
                        throw new RuntimeException("Base comment is not exists");
                    }
                    commentRequest.setId(UUID.randomUUID().toString());
                    PutItemRequest putItemRequest = PutItemRequest.builder()
                            .tableName(AppConstants.TABLE_NAME)
                            .item(ReplyCommentMapper.toMap(
                                    comments.get(0).get(AppConstants.PARTITION_KEY).s(), comments.get(0).get(AppConstants.SOFT_KEY).s(), commentRequest
                            ))
                            .returnValues(ReturnValue.ALL_OLD)
                            .build();

                    try {
                        return dynamoDbAsyncClient.putItem(putItemRequest)
                                .thenApply(PutItemResponse::attributes)
                                .thenApply(newComment -> CommentMapper.fromMap(newComment, newComment.get(AppConstants.SOFT_KEY).s().length()))
                                .get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }
}
