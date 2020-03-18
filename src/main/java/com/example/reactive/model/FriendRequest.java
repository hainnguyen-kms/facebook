package com.example.reactive.model;

public class FriendRequest {
    private String user_id;
    private String friend_id;
    private Boolean is_accepted;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getFriend_id() {
        return friend_id;
    }

    public void setFriend_id(String friend_id) {
        this.friend_id = friend_id;
    }

    public Boolean getAccepted() {
        return is_accepted;
    }

    public void setAccepted(Boolean accepted) {
        is_accepted = accepted;
    }
}
