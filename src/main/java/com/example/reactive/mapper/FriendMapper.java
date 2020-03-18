package com.example.reactive.mapper;

import com.example.reactive.AppConstants;
import com.example.reactive.model.Friend;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FriendMapper {
    public static List<Friend> fromList(List<Map<String, AttributeValue>> items) {
        return items.stream()
                .map(FriendMapper::fromMap)
                .collect(Collectors.toList());
    }

    public static Friend fromMap(Map<String, AttributeValue> attributeValueMap) {
        Friend friend = new Friend();
        friend.setUser_id(attributeValueMap.get(AppConstants.PARTITION_KEY).s());
        friend.setFriend_id(attributeValueMap.get(AppConstants.SOFT_KEY).s());
        friend.setIs_accepted(attributeValueMap.get("is_accepted").bool());
        return friend;
    }
}
