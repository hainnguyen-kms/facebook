package com.example.reactive.mapper;

import com.example.reactive.AppConstants;
import com.example.reactive.dto.LikeRequest;
import com.example.reactive.model.Like;
import com.example.reactive.model.Post;
import com.example.reactive.model.Tag;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public class LikeMapper {
    public static Like fromMap(Map<String, AttributeValue> attributeValueMap, Integer offset) {
        Like like = new Like();
        like.setUser_id(attributeValueMap.get("soft_key").s().substring(offset));
//        like.setUser_name(attributeValueMap.get("name").s());
        like.setLike_type(attributeValueMap.get("type").s());
        return like;
    }


    public static Map<String, AttributeValue> toMap(String postId, LikeRequest likeRequest) {
        return Map.of(
                AppConstants.PARTITION_KEY, AttributeValue.builder().s(AppConstants.POST_PREFIX + postId).build(),
                AppConstants.SOFT_KEY, AttributeValue.builder().s(AppConstants.LIKE_PREFIX + likeRequest.getUser_id()).build(),
                "type", AttributeValue.builder().s(likeRequest.getLike_type()).build()
        );
    }
}