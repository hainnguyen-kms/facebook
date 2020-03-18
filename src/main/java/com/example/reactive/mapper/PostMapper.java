package com.example.reactive.mapper;

import com.example.reactive.AppConstants;
import com.example.reactive.model.Comment;
import com.example.reactive.model.Like;
import com.example.reactive.model.Post;
import com.example.reactive.model.Tag;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PostMapper {
    public static Boolean isTypeMap(String type, Map<String, AttributeValue> mapValue) {
        for (String key : mapValue.keySet()) {
            if (mapValue.get(key).s() != null && mapValue.get(key).s().startsWith(type)) {
                return true;
            }
        }
        return false;
    }

    public static Post fromMap(String postId, String text, String time, List<Map<String, AttributeValue>> attributeValueMaps) {
        Post post = new Post();
        post.setId(postId.substring(AppConstants.POST_PREFIX.length()));
        post.setText(text);
        post.setTime(time);
        List<Tag> tags = new ArrayList<>();
        List<Like> likes = new ArrayList<>();
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i < attributeValueMaps.size(); i++) {
            if (isTypeMap(AppConstants.TAG_PREFIX, attributeValueMaps.get(i))) {
                tags.add(TagMapper.fromMap(attributeValueMaps.get(i)));
            } else if (isTypeMap(AppConstants.LIKE_PREFIX, attributeValueMaps.get(i))) {
                likes.add(LikeMapper.fromMap(attributeValueMaps.get(i), AppConstants.LIKE_PREFIX.length()));
            } else if (isTypeMap(AppConstants.COMMENT_PREFIX, attributeValueMaps.get(i))) {
                Comment comment = CommentMapper.fromMap(attributeValueMaps.get(i), AppConstants.COMMENT_PREFIX.length());

                String replyPrefix = attributeValueMaps.get(i).get(AppConstants.SOFT_KEY).s() + AppConstants.REPLY_PREFIX;
                String likePrefix = attributeValueMaps.get(i).get(AppConstants.SOFT_KEY).s() + AppConstants.LIKE_PREFIX;

                int j = i + 1;
                for (; j < attributeValueMaps.size(); j++) {
                    if (isTypeMap(replyPrefix, attributeValueMaps.get(j))) {
                        Comment reply = CommentMapper.fromMap(attributeValueMaps.get(j), replyPrefix.length());
                        comment.getReplies().add(reply);
                    } else if (isTypeMap(likePrefix, attributeValueMaps.get(j))) {
                        Like like = LikeMapper.fromMap(attributeValueMaps.get(j), likePrefix.length());
                        comment.getLikes().add(like);
                    }
                }
                i = j + 1;

                comments.add(comment);
            }
        }

        post.setTags(tags);
        post.setLikes(likes);
        post.setComments(comments);
        return post;
    }

    public static Map<String, AttributeValue> toMap(String userId, Post post) {
        return Map.of(
                AppConstants.PARTITION_KEY, AttributeValue.builder().s(AppConstants.POST_PREFIX + post.getId()).build(),
                AppConstants.SOFT_KEY, AttributeValue.builder().s(AppConstants.USER_PREFIX + userId).build(),
                AppConstants.POPULARITY, AttributeValue.builder().n("0").build(),
                AppConstants.UPDATED_AT, AttributeValue.builder().s(new SimpleDateFormat(AppConstants.DATE_FORMAT).format(new Date())).build(),
                "text", AttributeValue.builder().s(post.getText() != null ? post.getText() : "text").build(),
                "entity_name", AttributeValue.builder().s("POST").build(),
                "time", AttributeValue.builder().s(new SimpleDateFormat(AppConstants.DATE_FORMAT).format(new Date())).build()
        );
    }

    public static Map<String, AttributeValue> toPopularityUpdate(String userId, Post post) {
        return Map.of(
                AppConstants.PARTITION_KEY, AttributeValue.builder().s(AppConstants.POST_PREFIX + post.getId()).build(),
                AppConstants.SOFT_KEY, AttributeValue.builder().s(AppConstants.USER_PREFIX + userId).build(),
                AppConstants.POPULARITY, AttributeValue.builder().n("0").build()
        );
    }
}