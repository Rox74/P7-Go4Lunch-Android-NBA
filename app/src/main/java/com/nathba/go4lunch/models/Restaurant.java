package com.nathba.go4lunch.models;

import org.osmdroid.util.GeoPoint;

import java.util.Objects;

/**
 * Represents a restaurant entity with attributes such as name, address, rating, and location.
 * <p>
 * This class is used to model restaurant data, including additional details like phone number,
 * photo URL, opening hours, and a flag to check if restaurant details have been fetched.
 * It also includes utility methods like {@link #equals(Object)} and {@link #hashCode()}.
 */
public class Restaurant {

    /** Unique identifier for the restaurant. */
    private String restaurantId;

    /** Name of the restaurant. */
    private String name;

    /** Address of the restaurant. */
    private String address;

    /** URL of the restaurant's photo. */
    private String photoUrl;

    /** Rating of the restaurant, e.g., 4.5 out of 5. */
    private double rating;

    /** Geographical location of the restaurant (latitude and longitude). */
    private GeoPoint location;

    /** Phone number of the restaurant. */
    private String phoneNumber;

    /** URL to the restaurant's Yelp page. */
    private String yelpUrl;

    /** Opening hours of the restaurant. */
    private String openingHours;

    /** Indicator to check if detailed information has been fetched. */
    private boolean detailsFetched;

    /**
     * Default constructor for {@link Restaurant}.
     * <p>
     * Required for serialization and deserialization frameworks such as Firebase.
     */
    public Restaurant() {}

    /**
     * Parameterized constructor to initialize a {@link Restaurant} instance with specific values.
     *
     * @param restaurantId   Unique identifier for the restaurant.
     * @param name           Name of the restaurant.
     * @param address        Address of the restaurant.
     * @param photoUrl       URL of the restaurant's photo.
     * @param rating         Rating of the restaurant.
     * @param location       Geographical location (latitude and longitude).
     * @param phoneNumber    Phone number of the restaurant.
     * @param yelpUrl        URL to the restaurant's Yelp page.
     * @param openingHours   Opening hours of the restaurant.
     * @param detailsFetched Boolean flag indicating if details have been fetched.
     */
    public Restaurant(String restaurantId, String name, String address, String photoUrl, double rating,
                      GeoPoint location, String phoneNumber, String yelpUrl, String openingHours, boolean detailsFetched) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.address = address;
        this.photoUrl = photoUrl;
        this.rating = rating;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.yelpUrl = yelpUrl;
        this.openingHours = openingHours;
        this.detailsFetched = false; // Default to false
    }

    /** @return The unique identifier of the restaurant. */
    public String getRestaurantId() {
        return restaurantId;
    }

    /** Sets the unique identifier of the restaurant. */
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    /** @return The name of the restaurant. */
    public String getName() {
        return name;
    }

    /** Sets the name of the restaurant. */
    public void setName(String name) {
        this.name = name;
    }

    /** @return The address of the restaurant. */
    public String getAddress() {
        return address;
    }

    /** Sets the address of the restaurant. */
    public void setAddress(String address) {
        this.address = address;
    }

    /** @return The URL of the restaurant's photo. */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /** Sets the URL of the restaurant's photo. */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /** @return The rating of the restaurant. */
    public double getRating() {
        return rating;
    }

    /** Sets the rating of the restaurant. */
    public void setRating(double rating) {
        this.rating = rating;
    }

    /** @return The geographical location (latitude and longitude) of the restaurant. */
    public GeoPoint getLocation() {
        return location;
    }

    /** Sets the geographical location of the restaurant. */
    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    /** @return The phone number of the restaurant. */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /** Sets the phone number of the restaurant. */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /** @return The URL to the restaurant's Yelp page. */
    public String getYelpUrl() {
        return yelpUrl;
    }

    /** Sets the Yelp URL of the restaurant. */
    public void setYelpUrl(String yelpUrl) {
        this.yelpUrl = yelpUrl;
    }

    /** @return The opening hours of the restaurant. */
    public String getOpeningHours() {
        return openingHours;
    }

    /** Sets the opening hours of the restaurant. */
    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    /** @return {@code true} if the restaurant details have been fetched; {@code false} otherwise. */
    public boolean isDetailsFetched() {
        return detailsFetched;
    }

    /** Sets whether the restaurant details have been fetched. */
    public void setDetailsFetched(boolean detailsFetched) {
        this.detailsFetched = detailsFetched;
    }

    /**
     * Checks if two {@link Restaurant} objects are equal based on their ID, name, address, photo URL,
     * rating, and location.
     *
     * @param o The object to compare with the current instance.
     * @return {@code true} if the objects are equal; {@code false} otherwise.
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
     * Computes the hash code for a {@link Restaurant} object based on its ID, name, address,
     * photo URL, rating, and location.
     *
     * @return The hash code for the current instance.
     */
    @Override
    public int hashCode() {
        return Objects.hash(restaurantId, name, address, photoUrl, rating, location);
    }
}