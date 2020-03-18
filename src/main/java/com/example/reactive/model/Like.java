package com.example.reactive.model;

public class Like {
    private String user_id;
    private String user_name;
    private LikeType like_type;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public LikeType getLike_type() {
        return like_type;
    }

    public void setLike_type(String like_type) {
        this.like_type = LikeType.valueOf(like_type);
    }
}
