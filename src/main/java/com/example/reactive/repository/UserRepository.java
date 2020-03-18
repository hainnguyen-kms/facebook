package com.example.reactive.repository;

import com.example.reactive.AppConstants;
import com.example.reactive.dto.FriendRequestDTO;
import com.example.reactive.mapper.FriendMapper;
import com.example.reactive.mapper.FriendRequestMapper;
import com.example.reactive.mapper.PostMapper;
import com.example.reactive.mapper.UserMapper;
import com.example.reactive.model.Friend;
import com.example.reactive.model.FriendRequest;
import com.example.reactive.model.Post;
import com.example.reactive.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

@Repository
public class UserRepository {
    @Autowired
    private DynamoDbAsyncClient dynamoDbAsyncClient;

    public Flux<User> listUsers() {
        //set up mapping of the partition name with the value
        HashMap<String, AttributeValue> attrValues = new HashMap<String,AttributeValue>();
        attrValues.put(":"+"pk", AttributeValue.builder().s(AppConstants.USER_PREFIX).build());

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .filterExpression("begins_with(partition_key, :pk)")
                .expressionAttributeValues(attrValues)
                .build();


        return Mono.fromCompletionStage(dynamoDbAsyncClient.scan(scanRequest))
                .map(ScanResponse::items)
                .map(UserMapper::fromList)
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<User> createUser(User user) {

        user.setId(UUID.randomUUID().toString());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .item(UserMapper.toMap(user))
                .build();

        return Mono.fromCompletionStage(dynamoDbAsyncClient.putItem(putItemRequest))
                .map(PutItemResponse::attributes)
                .map(attributeValueMap -> user);
    }

    public Mono<FriendRequest> requestFriend(FriendRequestDTO requestFriendBody) {
        FriendRequest newRequest = new FriendRequest();
        newRequest.setUser_id(requestFriendBody.getUser_id());
        newRequest.setFriend_id(requestFriendBody.getFriend_id());
        newRequest.setAccepted(false);


        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .item(FriendRequestMapper.toMap(newRequest))
                .build();

        return Mono.fromCompletionStage(dynamoDbAsyncClient.putItem(putItemRequest))
                .map(PutItemResponse::attributes)
                .map(attributeValueMap -> newRequest);
    }

    public Flux<FriendRequest> listFriendRequestByUser(String userId) {
        //set up mapping of the partition name with the value
        HashMap<String, AttributeValue> attrValues = new HashMap<String,AttributeValue>();
        attrValues.put(":pk", AttributeValue.builder().s(AppConstants.USER_PREFIX+userId).build());
        attrValues.put(":sk", AttributeValue.builder().s(AppConstants.FRIEND_PREFIX).build());

        QueryRequest queryReq = QueryRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .keyConditionExpression("partition_key = :pk and begins_with(soft_key, :sk)")
                .expressionAttributeValues(attrValues)
                .build();

        return Mono.fromCompletionStage(dynamoDbAsyncClient.query(queryReq))
                .map(QueryResponse::items)
                .map(FriendRequestMapper::fromList)
                .flatMapMany(Flux::fromIterable);
    }

    public Flux<Post> getNewFeedOfUser(String userId) {
        HashMap<String, AttributeValue> userAttrValues = new HashMap<>();
        userAttrValues.put(":pk", AttributeValue.builder().s(AppConstants.USER_PREFIX+userId).build());
        userAttrValues.put(":sk", AttributeValue.builder().s(AppConstants.FRIEND_PREFIX).build());

        QueryRequest queryUserReq = QueryRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .keyConditionExpression("partition_key = :pk and begins_with(soft_key, :sk)")
                .expressionAttributeValues(userAttrValues)
                .build();


        HashMap<String, AttributeValue> postAttrValues = new HashMap<>();
        postAttrValues.put(":sk", AttributeValue.builder().s("POST").build());
        QueryRequest queryPostRequest = QueryRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .indexName(AppConstants.GSI_BY_POPULARITY)
                .keyConditionExpression("entity_name = :sk")
                .expressionAttributeValues(postAttrValues)
                .scanIndexForward(false)
                .build();



        return Mono.fromCompletionStage(dynamoDbAsyncClient.query(queryUserReq))
                .map(QueryResponse::items)
                .map(FriendMapper::fromList)
                .flatMap(friendList -> Mono.fromCompletionStage(dynamoDbAsyncClient.query(queryPostRequest))
                        .map(QueryResponse::items)
                        .map(postMaps -> postMaps.stream().filter(
                                postMap -> {
                                    String postUserId = postMap.get("soft_key").s();
                                    return friendList.stream().anyMatch(friend -> friend.getUser_id().equals(postUserId) || friend.getFriend_id().equals(postUserId));
                                }
                        )
                        .collect(Collectors.toList()))
                )
                .map(posts -> posts.stream().map(post -> {
                        String postId = post.get(AppConstants.PARTITION_KEY).s();
                        String text = post.get("text").s();
                        String time = post.get("time").s();
                        QueryRequest queryRequest = QueryRequest.builder()
                                .tableName(AppConstants.TABLE_NAME)
                                .keyConditionExpression("partition_key = :post_id")
                                .expressionAttributeValues(Map.of(":post_id", AttributeValue.builder().s(postId).build()))
                                .build();

                        try {
                            return dynamoDbAsyncClient.query(queryRequest)
                                    .thenApply(QueryResponse::items)
                                    .thenApply((maps) -> PostMapper.fromMap(postId, text, time, maps)).get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).collect(Collectors.toList())
                )
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<User> findByName(String username) {
        Map<String, AttributeValue> findMap = new HashMap<>();
        findMap.put(":name", AttributeValue.builder().s(username).build());
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .indexName(AppConstants.GSI_BY_SOFTKEY)
                .keyConditionExpression("soft_key = :name")
                .expressionAttributeValues(findMap)
                .build();

        return Mono.fromCompletionStage(dynamoDbAsyncClient.query(queryRequest))
                .map(QueryResponse::items)
                .flatMap(userMaps -> {
                    if(userMaps.isEmpty()) return Mono.error(new RuntimeException("user not found"));
                    return Mono.just(UserMapper.fromMap(userMaps.get(0)));
                })
                ;
    }

    public Mono<Friend> actionRequestFriend(FriendRequest requestBody) {
        Map<String, AttributeValue> updateKeyMap = new HashMap<>();
        updateKeyMap.put(AppConstants.PARTITION_KEY, AttributeValue.builder()
                .s(AppConstants.USER_PREFIX + requestBody.getUser_id())
                .build());
        updateKeyMap.put(AppConstants.SOFT_KEY, AttributeValue.builder()
                .s(AppConstants.FRIEND_PREFIX + requestBody.getFriend_id())
                .build());

        Map<String, AttributeValueUpdate> updateMap = new HashMap<>();
        updateMap.put("is_accepted", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().bool(requestBody.getAccepted()).build())
                .action(AttributeAction.PUT)
                .build());

        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .tableName(AppConstants.TABLE_NAME)
                .key(updateKeyMap)
                .attributeUpdates(updateMap)
                .returnValues(ReturnValue.ALL_NEW)
                .build();

        return Mono.fromCompletionStage(dynamoDbAsyncClient.updateItem(updateItemRequest))
                .map(UpdateItemResponse::attributes)
                .map(FriendMapper::fromMap);
    }
}
