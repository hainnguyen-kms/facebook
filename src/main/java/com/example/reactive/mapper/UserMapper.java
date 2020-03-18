package com.example.reactive.mapper;

import com.example.reactive.AppConstants;
import com.example.reactive.model.User;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserMapper {
    public static List<User> fromList(List<Map<String, AttributeValue>> items) {
        return items.stream()
                .filter(o -> o.get("password") != null)
                .map(UserMapper::fromMap)
                .collect(Collectors.toList());
    }

    public static User fromMap(Map<String, AttributeValue> attributeValueMap) {
        if(attributeValueMap == null) return null;
        User user = new User();
        user.setId(attributeValueMap.get(AppConstants.PARTITION_KEY).s().substring(AppConstants.USER_PREFIX.length()));
        user.setUsername(attributeValueMap.get(AppConstants.SOFT_KEY).s());
//        user.setEmail(attributeValueMap.get("email").s());
        user.setPassword(attributeValueMap.get("password").s());
        return user;
    }

    public static Map<String, AttributeValue> toMap(User user) {
        return Map.of(
                AppConstants.PARTITION_KEY, AttributeValue.builder().s(AppConstants.USER_PREFIX + user.getId()).build(),
                AppConstants.SOFT_KEY, AttributeValue.builder().s(user.getUsername()).build(),
//                "email", AttributeValue.builder().s(user.getEmail()).build(),
                "password", AttributeValue.builder().s(user.getPassword()).build()
        );
    }
}