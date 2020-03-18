package com.example.reactive.repository;

import com.example.reactive.AppConstants;
import com.example.reactive.dto.CommentRequest;
import com.example.reactive.dto.ImageRequest;
import com.example.reactive.dto.LikeRequest;
import com.example.reactive.dto.TagRequest;
import com.example.reactive.mapper.*;
import com.example.reactive.model.Image;
import com.example.reactive.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class ImageRepository {
    @Autowired
    private DynamoDbAsyncClient dynamoDbAsyncClient;
    @Autowired
    private PostRepository postRepository;

    public Mono<Void> uploadImage(ImageRequest imageRequest) {
        return postRepository
                .createPost(imageRequest.getUser_id(), new Post())
                .map(newPost -> buildBatchWriteNewImage(imageRequest, newPost))
                .flatMap(Mono::fromCompletionStage)
                .map(BatchWriteItemResponse::itemCollectionMetrics)
                .thenEmpty(Mono.empty());
    }

    private CompletableFuture<BatchWriteItemResponse> buildBatchWriteNewImage(
            ImageRequest imageRequest, Post newPost
    ) {
        List<WriteRequest> writeRequests = imageRequest
                .getImage_urls()
                .stream()
                .map(imageUrl -> WriteRequest.builder()
                        .putRequest(PutRequest.builder()
                                .item(ImageMapper.toMap(newPost.getId(), imageRequest.getUser_id(), imageUrl))
                                .build())
                        .build())
                .collect(Collectors.toList());


        return dynamoDbAsyncClient.batchWriteItem(BatchWriteItemRequest.builder()
                .requestItems(Map.of(AppConstants.TABLE_NAME, writeRequests))
                .build());
    }

    public Mono<Void> deleteImage(String imageId) {
        QueryRequest queryImageRequest = QueryRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .indexName(AppConstants.GSI_BY_ENTITY_NAME)
                .keyConditionExpression("entity_name = :en and begins_with(soft_key, :sk)")
                .expressionAttributeValues(Map.of(
                        ":en", AttributeValue.builder().s("IMAGE").build(),
                        ":sk", AttributeValue.builder().s(AppConstants.IMAGE_PREFIX + imageId).build()
                ))
                .build();

        return Mono.fromCompletionStage(dynamoDbAsyncClient.query(queryImageRequest))
                .map(QueryResponse::items)
                .map(this::buildBatchDeleteImage)
                .flatMap(Mono::fromCompletionStage)
                .thenEmpty(Mono.empty());
    }

    private CompletableFuture<BatchWriteItemResponse> buildBatchDeleteImage(
            List<Map<String ,AttributeValue>> deleteMaps
    ) {
        List<WriteRequest> writeRequests = deleteMaps
                .stream()
                .map(deleteMap -> WriteRequest.builder()
                        .deleteRequest(DeleteRequest.builder()
                                .key(Map.of(
                                        AppConstants.PARTITION_KEY, deleteMap.get(AppConstants.PARTITION_KEY),
                                        AppConstants.SOFT_KEY, deleteMap.get(AppConstants.SOFT_KEY)
                                ))
                                .build())
                        .build())
                .collect(Collectors.toList());


        return dynamoDbAsyncClient.batchWriteItem(BatchWriteItemRequest.builder()
                .requestItems(Map.of(AppConstants.TABLE_NAME, writeRequests))
                .build());
    }
}
