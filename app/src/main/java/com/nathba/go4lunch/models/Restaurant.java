package com.nathba.go4lunch.models;

import org.osmdroid.util.GeoPoint;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a restaurant with details such as ID, name, address, photo URL, rating, and location.
 */
public class Restaurant {

    // Unique identifier for the restaurant
    private String restaurantId;

    // Name of the restaurant
    private String name;

    // Address of the restaurant
    private String address;

    // URL of the restaurant's photo
    private String photoUrl;

    // Rating of the restaurant (e.g., 4.5 out of 5)
    private double rating;

    // Geographical location of the restaurant
    private GeoPoint location;

    private String phoneNumber;

    private String yelpUrl;

    /**
     * Default constructor for Restaurant.
     * Initializes a new instance with default values.
     */
    public Restaurant() {}

    /**
     * Constructs a new Restaurant with the specified details.
     *
     * @param restaurantId Unique identifier for the restaurant
     * @param name Name of the restaurant
     * @param address Address of the restaurant
     * @param photoUrl URL of the restaurant's photo
     * @param rating Rating of the restaurant
     * @param location Geographical location of the restaurant
     */
    public Restaurant(String restaurantId, String name, String address, String photoUrl, double rating, GeoPoint location) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.address = address;
        this.photoUrl = photoUrl;
        this.rating = rating;
        this.location = location;
    }

    /**
     * Gets the unique identifier of the restaurant.
     *
     * @return the restaurant ID
     */
    public String getRestaurantId() {
        return restaurantId;
    }

    /**
     * Sets the unique identifier of the restaurant.
     *
     * @param restaurantId the restaurant ID to set
     */
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    /**
     * Gets the name of the restaurant.
     *
     * @return the name of the restaurant
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the restaurant.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the address of the restaurant.
     *
     * @return the address of the restaurant
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address of the restaurant.
     *
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the URL of the restaurant's photo.
     *
     * @return the photo URL
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Sets the URL of the restaurant's photo.
     *
     * @param photoUrl the photo URL to set
     */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /**
     * Gets the rating of the restaurant.
     *
     * @return the rating of the restaurant
     */
    public double getRating() {
        return rating;
    }

    /**
     * Sets the rating of the restaurant.
     *
     * @param rating the rating to set
     */
    public void setRating(double rating) {
        this.rating = rating;
    }

    /**
     * Gets the geographical location of the restaurant.
     *
     * @return the location of the restaurant
     */
    public GeoPoint getLocation() {
        return location;
    }

    /**
     * Sets the geographical location of the restaurant.
     *
     * @param location the location to set
     */
    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getYelpUrl() {
        return yelpUrl;
    }

    public void setYelpUrl(String yelpUrl) {
        this.yelpUrl = yelpUrl;
    }

    /**
     * Checks if this restaurant is equal to another object.
     * Two restaurants are considered equal if they have the same ID, name, address, photo URL, rating, and location.
     *
     * @param o the object to compare with
     * @return true if this restaurant is equal to the other object, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return Double.compare(that.rating, rating) == 0 &&
                Objects.equals(restaurantId, that.restaurantId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(address, that.address) &&
                Objects.equals(photoUrl, that.photoUrl) &&
                Objects.equals(location, that.location);
    }

    /**
     * Returns a hash code value for this restaurant.
     * The hash code is calculated based on the restaurant's ID, name, address, photo URL, rating, and location.
     *
     * @return the hash code value for this restaurant
     */
    @Override
    public int hashCode() {
        return Objects.hash(restaurantId, name, address, photoUrl, rating, location);
    }
}