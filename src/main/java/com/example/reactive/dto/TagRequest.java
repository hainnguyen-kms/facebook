package com.example.reactive.dto;

import java.util.List;

public class TagRequest {
    private List<String> user_ids;

    public List<String> getUser_ids() {
        return user_ids;
    }

    public void setUser_ids(List<String> user_ids) {
        this.user_ids = user_ids;
    }
}
