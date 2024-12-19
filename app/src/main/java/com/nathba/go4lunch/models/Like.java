package com.nathba.go4lunch.models;

/**
 * Represents a "Like" entity linking a user to a restaurant they liked.
 * This class is used for Firebase data representation.
 */
public class Like {

    private String likeId;
    private String userId;
    private String restaurantId;

    /**
     * Default constructor required for Firebase deserialization.
     */
    public Like() {
        // Default constructor
    }

    /**
     * Creates a new Like object with specified identifiers.
     *
     * @param likeId       The unique identifier for the like.
     * @param userId       The unique identifier of the user who liked the restaurant.
     * @param restaurantId The unique identifier of the restaurant that was liked.
     */
    public Like(String likeId, String userId, String restaurantId) {
        this.likeId = likeId;
        this.userId = userId;
        this.restaurantId = restaurantId;
    }

    /**
     * Retrieves the unique identifier of the like.
     *
     * @return The like ID.
     */
    public String getLikeId() {
        return likeId;
    }

    /**
     * Retrieves the unique identifier of the user who liked the restaurant.
     *
     * @return The user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Retrieves the unique identifier of the liked restaurant.
     *
     * @return The restaurant ID.
     */
    public String getRestaurantId() {
        return restaurantId;
    }
}