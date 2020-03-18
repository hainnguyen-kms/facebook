package com.example.reactive.dto;

import com.example.reactive.model.LikeType;

public class LikeRequest {
    private String user_id;
    private LikeType like_type;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getLike_type() {
        return like_type.name();
    }

    public void setLike_type(LikeType like_type) {
        this.like_type = like_type;
    }

}
