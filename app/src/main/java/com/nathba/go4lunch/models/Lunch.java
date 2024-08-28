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

    // Date when the lunch is scheduled
    private Date date;

    /**
     * Default constructor required for serialization and deserialization.
     */
    public Lunch() {}

    /**
     * Parameterized constructor to create a new Lunch instance with specified values.
     *
     * @param lunchId       Unique identifier for the lunch entry.
     * @param workmateId    Identifier for the workmate who chose the restaurant.
     * @param restaurantId  Identifier for the restaurant chosen for lunch.
     * @param date          Date when the lunch is scheduled.
     */
    public Lunch(String lunchId, String workmateId, String restaurantId, Date date) {
        this.lunchId = lunchId;
        this.workmateId = workmateId;
        this.restaurantId = restaurantId;
        this.date = date;
    }

    /**
     * Gets the unique identifier for the lunch entry.
     *
     * @return String - Unique identifier for the lunch entry.
     */
    public String getLunchId() {
        return lunchId;
    }

    /**
     * Sets the unique identifier for the lunch entry.
     *
     * @param lunchId - Unique identifier for the lunch entry.
     */
    public void setLunchId(String lunchId) {
        this.lunchId = lunchId;
    }

    /**
     * Gets the identifier for the workmate who chose the restaurant.
     *
     * @return String - Identifier for the workmate.
     */
    public String getWorkmateId() {
        return workmateId;
    }

    /**
     * Sets the identifier for the workmate who chose the restaurant.
     *
     * @param workmateId - Identifier for the workmate.
     */
    public void setWorkmateId(String workmateId) {
        this.workmateId = workmateId;
    }

    /**
     * Gets the identifier for the restaurant chosen for lunch.
     *
     * @return String - Identifier for the restaurant.
     */
    public String getRestaurantId() {
        return restaurantId;
    }

    /**
     * Sets the identifier for the restaurant chosen for lunch.
     *
     * @param restaurantId - Identifier for the restaurant.
     */
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    /**
     * Gets the date when the lunch is scheduled.
     *
     * @return Date - Date of the lunch.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date when the lunch is scheduled.
     *
     * @param date - Date of the lunch.
     */
    public void setDate(Date date) {
        this.date = date;
    }
}