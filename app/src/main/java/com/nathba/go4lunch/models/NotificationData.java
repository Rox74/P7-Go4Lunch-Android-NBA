package com.nathba.go4lunch.models;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Model class representing the data required for sending a notification.
 * <p>
 * This class includes information about the selected restaurant and the list of colleagues
 * joining for lunch. It is used to structure the data sent in notifications.
 */
public class NotificationData {

    /** The name of the selected restaurant. */
    private String restaurantName;

    /** The address of the selected restaurant. */
    private String restaurantAddress;

    /** A list of names of colleagues joining for lunch. */
    private List<String> colleaguesNames;

    /**
     * Default constructor required for serialization and deserialization.
     * <p>
     * This constructor is required when frameworks like Firebase deserialize the object.
     */
    public NotificationData() {}

    /**
     * Parameterized constructor to initialize a {@link NotificationData} object with specified values.
     *
     * @param restaurantName    The name of the selected restaurant.
     * @param restaurantAddress The address of the selected restaurant.
     * @param colleaguesNames   A list of names of colleagues joining for lunch.
     */
    public NotificationData(String restaurantName, String restaurantAddress, List<String> colleaguesNames) {
        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
        this.colleaguesNames = colleaguesNames;
    }

    /**
     * Returns the name of the restaurant.
     *
     * @return The name of the restaurant.
     */
    public String getRestaurantName() {
        return restaurantName;
    }

    /**
     * Sets the name of the restaurant.
     *
     * @param restaurantName The name of the restaurant to set.
     */
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    /**
     * Returns the address of the restaurant.
     *
     * @return The address of the restaurant.
     */
    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    /**
     * Sets the address of the restaurant.
     *
     * @param restaurantAddress The address of the restaurant to set.
     */
    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    /**
     * Returns the list of colleagues' names who are joining for lunch.
     *
     * @return A list of colleagues' names.
     */
    public List<String> getColleaguesNames() {
        return colleaguesNames;
    }

    /**
     * Sets the list of colleagues' names who are joining for lunch.
     *
     * @param colleaguesNames The list of colleagues' names to set.
     */
    public void setColleaguesNames(List<String> colleaguesNames) {
        this.colleaguesNames = colleaguesNames;
    }

    /**
     * Returns a string representation of the {@link NotificationData} object.
     * <p>
     * This method provides a readable representation of the object's state,
     * including restaurant name, address, and colleagues' names.
     *
     * @return A formatted string representing the {@link NotificationData} object.
     */
    @NonNull
    @Override
    public String toString() {
        return "NotificationData{" +
                "restaurantName='" + restaurantName + '\'' +
                ", restaurantAddress='" + restaurantAddress + '\'' +
                ", colleaguesNames=" + colleaguesNames +
                '}';
    }
}