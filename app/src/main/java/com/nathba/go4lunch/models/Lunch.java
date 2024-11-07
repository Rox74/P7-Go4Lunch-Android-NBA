package com.nathba.go4lunch.models;

import java.util.Date;

/**
 * Represents a Lunch entity where a workmate chooses a restaurant for lunch.
 * This class holds the information about the lunch, including the workmate who chose the restaurant,
 * the restaurant itself, and the date of the lunch.
 */
public class Lunch {

    // Unique identifier for the lunch entry
    private String lunchId;

    // Identifier for the workmate who chose the restaurant
    private String workmateId;

    // Identifier for the restaurant chosen for lunch
    private String restaurantId;

    // Name of the restaurant
    private String restaurantName;

    // Address of the restaurant
    private String restaurantAddress;

    // Date when the lunch is scheduled
    private Date date;

    /**
     * Default constructor required for serialization and deserialization.
     */
    public Lunch() {}

    /**
     * Parameterized constructor to create a new Lunch instance with specified values.
     *
     * @param lunchId           Unique identifier for the lunch entry.
     * @param workmateId        Identifier for the workmate who chose the restaurant.
     * @param restaurantId      Identifier for the restaurant chosen for lunch.
     * @param restaurantName    Name of the restaurant.
     * @param restaurantAddress Address of the restaurant.
     * @param date              Date when the lunch is scheduled.
     */
    public Lunch(String lunchId, String workmateId, String restaurantId, String restaurantName, String restaurantAddress, Date date) {
        this.lunchId = lunchId;
        this.workmateId = workmateId;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
        this.date = date;
    }


    // Getters et setters

    public String getLunchId() {
        return lunchId;
    }

    public void setLunchId(String lunchId) {
        this.lunchId = lunchId;
    }

    public String getWorkmateId() {
        return workmateId;
    }

    public void setWorkmateId(String workmateId) {
        this.workmateId = workmateId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}