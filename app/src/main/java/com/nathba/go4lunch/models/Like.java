package com.nathba.go4lunch.models;

public class Like {
    private String likeId;
    private String userId;
    private String restaurantId;

    public Like() {
        // Constructeur par défaut requis pour Firebase
    }

    public Like(String likeId, String userId, String restaurantId) {
        this.likeId = likeId;
        this.userId = userId;
        this.restaurantId = restaurantId;
    }

    public String getLikeId() { return likeId; }
    public String getUserId() { return userId; }
    public String getRestaurantId() { return restaurantId; }
}