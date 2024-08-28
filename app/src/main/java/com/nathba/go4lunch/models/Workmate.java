package com.nathba.go4lunch.models;

/**
 * Represents a workmate with details such as ID, name, email, and photo URL.
 */
public class Workmate {

    // Unique identifier for the workmate
    private String workmateId;

    // Name of the workmate
    private String name;

    // Email address of the workmate
    private String email;

    // URL of the workmate's photo
    private String photoUrl;

    /**
     * Default constructor required for Firebase serialization.
     * Initializes a new instance with default values.
     */
    public Workmate() {
        // Required empty constructor for Firebase
    }

    /**
     * Constructs a new Workmate with the specified details.
     *
     * @param workmateId Unique identifier for the workmate
     * @param name Name of the workmate
     * @param email Email address of the workmate
     * @param photoUrl URL of the workmate's photo
     */
    public Workmate(String workmateId, String name, String email, String photoUrl) {
        this.workmateId = workmateId;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    /**
     * Gets the unique identifier of the workmate.
     *
     * @return the workmate ID
     */
    public String getWorkmateId() {
        return workmateId;
    }

    /**
     * Sets the unique identifier of the workmate.
     *
     * @param workmateId the workmate ID to set
     */
    public void setWorkmateId(String workmateId) {
        this.workmateId = workmateId;
    }

    /**
     * Gets the name of the workmate.
     *
     * @return the name of the workmate
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the workmate.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the email address of the workmate.
     *
     * @return the email address of the workmate
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the workmate.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the URL of the workmate's photo.
     *
     * @return the photo URL
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Sets the URL of the workmate's photo.
     *
     * @param photoUrl the photo URL to set
     */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}