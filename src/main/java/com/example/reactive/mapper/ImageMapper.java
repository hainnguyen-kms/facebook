package com.example.reactive.mapper;

import com.example.reactive.AppConstants;
import com.example.reactive.dto.ImageRequest;
import com.example.reactive.model.Image;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class ImageMapper {
    public static Map<String, AttributeValue> toMap(String postId, String userId, String imageUrl) {
        return Map.of(
                AppConstants.PARTITION_KEY, AttributeValue.builder().s(AppConstants.POST_PREFIX + postId).build(),
                AppConstants.SOFT_KEY, AttributeValue.builder().s(AppConstants.IMAGE_PREFIX + UUID.randomUUID().toString()).build(),
                "user_id", AttributeValue.builder().s(userId).build(),
                "url", AttributeValue.builder().s(imageUrl).build(),
                "time", AttributeValue.builder().s(new SimpleDateFormat(AppConstants.DATE_FORMAT).format(new Date())).build(),
                "entity_name", AttributeValue.builder().s("IMAGE").build()
        );
    }

    public static Image fromMap(Map<String, AttributeValue> valueMap) {
        Image image = new Image();
        image.setId(valueMap.get(AppConstants.SOFT_KEY).s().substring(AppConstants.IMAGE_PREFIX.length()));
        image.setUrl(valueMap.get("url").s());
        image.setTime(valueMap.get("time").s());
        return image;
    }

}