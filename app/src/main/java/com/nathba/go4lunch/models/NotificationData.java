package com.nathba.go4lunch.models;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Model class representing the data required for sending a notification.
 * Includes restaurant name, address, and names of colleagues joining for lunch.
 */
public class NotificationData {
    private String restaurantName;
    private String restaurantAddress;
    private List<String> colleaguesNames;

    public NotificationData() {}

    public NotificationData(String restaurantName, String restaurantAddress, List<String> colleaguesNames) {
        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
        this.colleaguesNames = colleaguesNames;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public List<String> getColleaguesNames() {
        return colleaguesNames;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public void setColleaguesNames(List<String> colleaguesNames) {
        this.colleaguesNames = colleaguesNames;
    }

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