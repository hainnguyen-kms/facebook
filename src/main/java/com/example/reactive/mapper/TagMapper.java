package com.example.reactive.mapper;

import com.example.reactive.AppConstants;
import com.example.reactive.dto.TagRequest;
import com.example.reactive.model.Tag;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TagMapper {
    public static Tag fromMap(Map<String, AttributeValue> attributeValueMap) {
        Tag tag = new Tag();
        tag.setUser_id(attributeValueMap.get("soft_key").s().substring(AppConstants.TAG_PREFIX.length()));
        if(attributeValueMap.get("name") != null) {
            tag.setUser_name(attributeValueMap.get("name").s());
        }
        return tag;
    }

    public static List<Map<String, AttributeValue>> toMap(String postId, TagRequest tagRequest) {
        return tagRequest.getUser_ids().stream().map(
                userId -> Map.of(
                        AppConstants.PARTITION_KEY, AttributeValue.builder().s(AppConstants.POST_PREFIX + postId).build(),
                        AppConstants.SOFT_KEY, AttributeValue.builder().s(AppConstants.TAG_PREFIX + userId).build()
                )
        ).collect(Collectors.toList());
    }
}