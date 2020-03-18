package com.example.reactive.mapper;

import com.example.reactive.AppConstants;
import com.example.reactive.model.FriendRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FriendRequestMapper {
    public static List<FriendRequest> fromList(List<Map<String, AttributeValue>> items) {
        return items.stream()
                .map(FriendRequestMapper::fromMap)
                .collect(Collectors.toList());
    }

    public static FriendRequest fromMap(Map<String, AttributeValue> attributeValueMap) {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setUser_id(attributeValueMap.get(AppConstants.PARTITION_KEY).s().substring(AppConstants.USER_PREFIX.length()));
        friendRequest.setFriend_id(attributeValueMap.get(AppConstants.SOFT_KEY).s().substring(AppConstants.FRIEND_PREFIX.length()));
        friendRequest.setAccepted(attributeValueMap.get("is_accepted").bool());
        return friendRequest;
    }

    public static Map<String, AttributeValue> toMap(FriendRequest friendRequest) {
        return Map.of(
                AppConstants.PARTITION_KEY, AttributeValue.builder().s(AppConstants.USER_PREFIX + friendRequest.getUser_id()).build(),
                AppConstants.SOFT_KEY, AttributeValue.builder().s(AppConstants.FRIEND_PREFIX + friendRequest.getFriend_id()).build(),
                "is_accepted", AttributeValue.builder().s(friendRequest.getAccepted().toString()).build()
        );
    }
}