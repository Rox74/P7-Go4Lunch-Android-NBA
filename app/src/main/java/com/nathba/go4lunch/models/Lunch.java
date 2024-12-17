package com.nathba.go4lunch.models;

import java.util.Date;

/**
 * Represents a Lunch entity where a workmate selects a restaurant for lunch.
 * <p>
 * This class encapsulates information about the lunch, including:
 * the workmate who selected the restaurant, the restaurant details, and the date of the lunch.
 * It is used to model lunch data in the application.
 */
public class Lunch {

    /** Unique identifier for the lunch entry. */
    private String lunchId;

    /** Unique identifier of the workmate who selected the restaurant. */
    private String workmateId;

    /** Unique identifier of the restaurant chosen for lunch. */
    private String restaurantId;

    /** Name of the restaurant selected for lunch. */
    private String restaurantName;

    /** Address of the restaurant selected for lunch. */
    private String restaurantAddress;

    /** Date on which the lunch is scheduled. */
    private Date date;

    /**
     * Default constructor required for serialization and deserialization.
     * <p>
     * This constructor is necessary for frameworks such as Firebase Firestore,
     * which require an empty constructor for data deserialization.
     */
    public Lunch() {}

    /**
     * Parameterized constructor to create a new Lunch instance with specified values.
     *
     * @param lunchId           Unique identifier for the lunch entry.
     * @param workmateId        Unique identifier of the workmate who selected the restaurant.
     * @param restaurantId      Unique identifier of the restaurant chosen for lunch.
     * @param restaurantName    Name of the restaurant.
     * @param restaurantAddress Address of the restaurant.
     * @param date              Date on which the lunch is scheduled.
     */
    public Lunch(String lunchId, String workmateId, String restaurantId, String restaurantName, String restaurantAddress, Date date) {
        this.lunchId = lunchId;
        this.workmateId = workmateId;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
        this.date = date;
    }

    /**
     * Returns the unique identifier for the lunch entry.
     *
     * @return The lunch ID.
     */
    public String getLunchId() {
        return lunchId;
    }

    /**
     * Sets the unique identifier for the lunch entry.
     *
     * @param lunchId The lunch ID to set.
     */
    public void setLunchId(String lunchId) {
        this.lunchId = lunchId;
    }

    /**
     * Returns the unique identifier of the workmate who selected the restaurant.
     *
     * @return The workmate ID.
     */
    public String getWorkmateId() {
        return workmateId;
    }

    /**
     * Sets the unique identifier of the workmate who selected the restaurant.
     *
     * @param workmateId The workmate ID to set.
     */
    public void setWorkmateId(String workmateId) {
        this.workmateId = workmateId;
    }

    /**
     * Returns the unique identifier of the restaurant chosen for lunch.
     *
     * @return The restaurant ID.
     */
    public String getRestaurantId() {
        return restaurantId;
    }

    /**
     * Sets the unique identifier of the restaurant chosen for lunch.
     *
     * @param restaurantId The restaurant ID to set.
     */
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    /**
     * Returns the name of the restaurant selected for lunch.
     *
     * @return The restaurant name.
     */
    public String getRestaurantName() {
        return restaurantName;
    }

    /**
     * Sets the name of the restaurant selected for lunch.
     *
     * @param restaurantName The restaurant name to set.
     */
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    /**
     * Returns the address of the restaurant selected for lunch.
     *
     * @return The restaurant address.
     */
    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    /**
     * Sets the address of the restaurant selected for lunch.
     *
     * @param restaurantAddress The restaurant address to set.
     */
    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    /**
     * Returns the date on which the lunch is scheduled.
     *
     * @return The lunch date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date on which the lunch is scheduled.
     *
     * @param date The lunch date to set.
     */
    public void setDate(Date date) {
        this.date = date;
    }
}