package com.example.reactive.repository;

import com.example.reactive.AppConstants;
import com.example.reactive.dto.CommentRequest;
import com.example.reactive.dto.LikeRequest;
import com.example.reactive.dto.TagRequest;
import com.example.reactive.mapper.CommentMapper;
import com.example.reactive.mapper.LikeMapper;
import com.example.reactive.mapper.PostMapper;
import com.example.reactive.mapper.TagMapper;
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
public class PostRepository {
    @Autowired
    private DynamoDbAsyncClient dynamoDbAsyncClient;

    public Mono<Post> createPost(String userId, Post post) {
        post.setId(UUID.randomUUID().toString());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .item(PostMapper.toMap(userId, post))
                .build();

        return Mono.fromCompletionStage(dynamoDbAsyncClient.putItem(putItemRequest))
                .map(PutItemResponse::attributes)
                .map(attributeValueMap -> post);
    }

    public Flux<Post> listPostsOfUser(String userId) {
        //set up mapping of the partition name with the value
        HashMap<String, AttributeValue> attrValues = new HashMap<String, AttributeValue>();
        attrValues.put(":post_prefix", AttributeValue.builder().s(AppConstants.POST_PREFIX).build());
        attrValues.put(":user_id", AttributeValue.builder().s(AppConstants.USER_PREFIX + userId).build());

        QueryRequest queryPostIdsRequest = QueryRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .indexName(AppConstants.GSI_BY_SOFTKEY)
                .keyConditionExpression("soft_key = :user_id and begins_with(partition_key, :post_prefix)")
                .expressionAttributeValues(attrValues)
                .build();


        return Mono.fromCompletionStage(dynamoDbAsyncClient.query(queryPostIdsRequest))
                .map(QueryResponse::items)
                .flatMapIterable(this::queryPosts)
                .flatMap(Mono::fromCompletionStage)
                ;
    }

    private List<CompletableFuture<Post>> queryPosts(List<Map<String, AttributeValue>> posts) {
        return posts.stream().map(post -> {
            String postId = post.get(AppConstants.PARTITION_KEY).s();
            String text = post.get("text").s();
            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName(AppConstants.TABLE_NAME)
                    .keyConditionExpression("partition_key = :post_id")
                    .expressionAttributeValues(Map.of(":post_id", AttributeValue.builder().s(postId).build()))
                    .build();

            return dynamoDbAsyncClient.query(queryRequest)
                    .thenApply(QueryResponse::items)
                    .thenApply((maps) -> PostMapper.fromMap(postId, text, "time", maps));

        }).collect(Collectors.toList());
    }

    public Mono<Void> increasePopularityForPost(String postId) {
        Map<String, AttributeValue> findMap = new HashMap<>();
        findMap.put(":pk", AttributeValue.builder().s(AppConstants.POST_PREFIX + postId).build());
        findMap.put(":sk", AttributeValue.builder().s(AppConstants.USER_PREFIX).build());
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .keyConditionExpression("partition_key = :pk and begins_with(soft_key, :sk)")
                .expressionAttributeValues(findMap)
                .build();

        return Mono.fromCompletionStage(dynamoDbAsyncClient.query(queryRequest))
                .map(QueryResponse::items)
                .filter(List::isEmpty)
                .map(posts ->  posts.get(0))
                .map(this::updatePopularityForPost)
                .thenEmpty(Mono.empty())
                ;
    }

    private CompletableFuture<UpdateItemResponse> updatePopularityForPost(Map<String, AttributeValue> postMap) {
        Map<String, AttributeValue> updateKeyMap = new HashMap<>();
        updateKeyMap.put(AppConstants.PARTITION_KEY, postMap.get(AppConstants.PARTITION_KEY));
        updateKeyMap.put(AppConstants.SOFT_KEY, postMap.get(AppConstants.SOFT_KEY));

        Map<String, AttributeValueUpdate> updateValueMap = new HashMap<>();
        updateValueMap.put("popularity", AttributeValueUpdate.builder()
                .action(AttributeAction.ADD)
                .value(AttributeValue.builder()
                        .n("1")
                        .build())
                .build());
        updateValueMap.put("updated_at", AttributeValueUpdate.builder()
                .action(AttributeAction.PUT)
                .value(AttributeValue.builder()
                        .s(new SimpleDateFormat(AppConstants.DATE_FORMAT).format(new Date()))
                        .build())
                .build());


        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .key(updateKeyMap)
                .tableName(AppConstants.TABLE_NAME)
                .attributeUpdates(updateValueMap)
                .build();

        return dynamoDbAsyncClient.updateItem(updateItemRequest);
    }

    public Mono<LikeRequest> likePost(String postId, LikeRequest likeRequest) {
        PutRequest likePostRequest = PutRequest.builder()
                .item(LikeMapper.toMap(postId, likeRequest))
                .build();
        List<WriteRequest> requestItems = new ArrayList<>();
        requestItems.add(WriteRequest.builder().putRequest(likePostRequest).build());
        BatchWriteItemRequest batchWriteItemRequest = BatchWriteItemRequest.builder()
                .requestItems(Map.of(AppConstants.TABLE_NAME, requestItems))
                .build();

        increasePopularityForPost(postId).subscribe();

        return Mono.fromCompletionStage(dynamoDbAsyncClient.batchWriteItem(batchWriteItemRequest))
                .map(BatchWriteItemResponse::itemCollectionMetrics)
                .map(attributeValueMap -> likeRequest);
    }

    public Mono<TagRequest> tagPost(String postId, TagRequest tagRequest) {
        List<WriteRequest> requestItems = TagMapper.toMap(postId, tagRequest)
                .stream()
                .map(tagMap -> WriteRequest.builder()
                                            .putRequest(PutRequest.builder().item(tagMap).build())
                                            .build()
                )
                .collect(Collectors.toList());

        BatchWriteItemRequest batchWriteItemRequest = BatchWriteItemRequest.builder()
                .requestItems(Map.of(AppConstants.TABLE_NAME, requestItems))
                .build();
        increasePopularityForPost(postId).subscribe();

        return Mono.fromCompletionStage(dynamoDbAsyncClient.batchWriteItem(batchWriteItemRequest))
                .map(BatchWriteItemResponse::itemCollectionMetrics)
                .map(attributeValueMap -> tagRequest);
    }

    public Mono<CommentRequest> commentPost(String postId, CommentRequest commentRequest) {
        commentRequest.setId(UUID.randomUUID().toString());
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .item(CommentMapper.toMap(postId, commentRequest))
                .build();

        increasePopularityForPost(postId).subscribe();
        return Mono.fromCompletionStage(dynamoDbAsyncClient.putItem(putItemRequest))
                .map(PutItemResponse::attributes)
                .map(attributeValueMap -> commentRequest);
    }

    public Mono<Void> deletePost(String postId) {
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .keyConditionExpression("partition_key = :pk")
                .expressionAttributeValues(Map.of(":pk", AttributeValue.builder()
                        .s(AppConstants.POST_PREFIX + postId)
                        .build()))
                .build();

        return Mono.fromCompletionStage(dynamoDbAsyncClient.query(queryRequest))
                .map(QueryResponse::items)
                .map(this::buildDeletePostItemRequest)
                .flatMap(Mono::fromCompletionStage)
                .thenEmpty(Mono.empty())
                ;
    }
    private CompletableFuture<BatchWriteItemResponse> buildDeletePostItemRequest(List<Map<String, AttributeValue>> itemMaps) {
        List<WriteRequest> writeRequests = itemMaps.stream().map(map -> {
            Map<String, AttributeValue> itemKey = new HashMap<>();
            itemKey.put(AppConstants.PARTITION_KEY, map.get(AppConstants.PARTITION_KEY));
            itemKey.put(AppConstants.SOFT_KEY, map.get(AppConstants.SOFT_KEY));

            DeleteRequest deleteRequest = DeleteRequest.builder()
                    .key(itemKey)
                    .build();

            return WriteRequest.builder()
                    .deleteRequest(deleteRequest)
                    .build();

        }).collect(Collectors.toList());

        return dynamoDbAsyncClient.batchWriteItem(BatchWriteItemRequest.builder()
                .requestItems(Map.of(AppConstants.TABLE_NAME, writeRequests))
                .build());
    }
}
