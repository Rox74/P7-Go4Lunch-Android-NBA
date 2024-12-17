package com.nathba.go4lunch.models;

/**
 * Represents a workmate with details such as ID, name, email, and photo URL.
 * <p>
 * This class is used to model a workmate entity within the application, storing
 * essential information about the user. It includes getters and setters for
 * accessing and updating the workmate data.
 */
public class Workmate {

    /** Unique identifier for the workmate. */
    private String workmateId;

    /** Name of the workmate. */
    private String name;

    /** Email address of the workmate. */
    private String email;

    /** URL of the workmate photo. */
    private String photoUrl;

    /**
     * Default constructor required for Firebase serialization.
     * <p>
     * This empty constructor is necessary for frameworks like Firebase Firestore
     * to deserialize objects automatically.
     */
    public Workmate() {
        // Required empty constructor for Firebase
    }

    /**
     * Constructs a new {@link Workmate} with the specified details.
     *
     * @param workmateId Unique identifier for the workmate.
     * @param name       Name of the workmate.
     * @param email      Email address of the workmate.
     * @param photoUrl   URL of the workmate photo.
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
     * @return The workmate ID.
     */
    public String getWorkmateId() {
        return workmateId;
    }

    /**
     * Sets the unique identifier of the workmate.
     *
     * @param workmateId The workmate ID to set.
     */
    public void setWorkmateId(String workmateId) {
        this.workmateId = workmateId;
    }

    /**
     * Gets the name of the workmate.
     *
     * @return The name of the workmate.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the workmate.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the email address of the workmate.
     *
     * @return The email address of the workmate.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the workmate.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the URL of the workmate photo.
     *
     * @return The photo URL.
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Sets the URL of the workmate photo.
     *
     * @param photoUrl The photo URL to set.
     */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}