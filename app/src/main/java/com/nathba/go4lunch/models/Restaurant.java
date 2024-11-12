package com.nathba.go4lunch.models;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    private String openingHours;  // Horaires d'ouverture

    // Indicateur pour vérifier si les détails sont chargés
    private boolean detailsFetched;

    /**
     * Default constructor for Restaurant.
     * Initializes a new instance with default values.
     */
    public Restaurant() {}

    // Constructor
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
        this.detailsFetched = false;
    }


    // Getters et setters pour les autres champs

    public String getRestaurantId() { return restaurantId; }

    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }

    public void setAddress(String address) { this.address = address; }

    public String getPhotoUrl() { return photoUrl; }

    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public double getRating() { return rating; }

    public void setRating(double rating) { this.rating = rating; }

    public GeoPoint getLocation() { return location; }

    public void setLocation(GeoPoint location) { this.location = location; }

    public String getPhoneNumber() { return phoneNumber; }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getYelpUrl() { return yelpUrl; }

    public void setYelpUrl(String yelpUrl) { this.yelpUrl = yelpUrl; }

    public String getOpeningHours() { return openingHours; }

    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public boolean isDetailsFetched() {
        return detailsFetched;
    }

    public void setDetailsFetched(boolean detailsFetched) {
        this.detailsFetched = detailsFetched;
    }


    // Méthodes equals et hashCode
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

    @Override
    public int hashCode() {
        return Objects.hash(restaurantId, name, address, photoUrl, rating, location);
    }
}