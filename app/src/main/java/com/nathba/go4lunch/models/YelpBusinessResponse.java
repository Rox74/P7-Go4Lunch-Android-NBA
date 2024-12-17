package com.nathba.go4lunch.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents the response from the Yelp API for businesses.
 * <p>
 * This response contains a list of businesses, each represented by a {@link YelpBusiness}.
 * Each business includes details such as the name, rating, image URL, address, phone number,
 * and hours of operation.
 */
public class YelpBusinessResponse {

    /** List of businesses returned by the Yelp API. */
    @SerializedName("businesses")
    public List<YelpBusiness> businesses;

    /**
     * Represents a single business entity in the Yelp API response.
     * <p>
     * Includes key details such as ID, name, image URL, rating, location, phone number,
     * business URL, and hours of operation.
     */
    public static class YelpBusiness {

        /** Unique identifier for the business. */
        @SerializedName("id")
        public String id;

        /** Name of the business. */
        @SerializedName("name")
        public String name;

        /** URL of the business's image. */
        @SerializedName("image_url")
        public String imageUrl;

        /** Rating of the business (e.g., 4.5 out of 5). */
        @SerializedName("rating")
        public double rating;

        /** Location details of the business, including its address. */
        @SerializedName("location")
        public YelpLocation location;

        /** Phone number of the business (formatted for display). */
        @SerializedName("display_phone")
        public String phone;

        /** URL to the business's Yelp page. */
        @SerializedName("url")
        public String url;

        /** List of hours of operation for the business. */
        @SerializedName("hours")
        public List<YelpHours> hours;
    }

    /**
     * Represents the location details of a business.
     * <p>
     * Contains the primary address of the business as provided by the Yelp API.
     */
    public static class YelpLocation {

        /** The primary address of the business. */
        @SerializedName("address1")
        public String address;
    }

    /**
     * Represents the hours of operation for a business.
     * <p>
     * Contains a list of {@link YelpOpen} objects, where each entry corresponds to
     * a specific day's open hours.
     */
    public static class YelpHours {

        /** List of open hours for the business. */
        @SerializedName("open")
        public List<YelpOpen> open;
    }

    /**
     * Represents a specific open time for a business.
     * <p>
     * Contains the day of the week, start time, and end time of operation.
     */
    public static class YelpOpen {

        /** The day of the week (0 = Monday, 6 = Sunday). */
        @SerializedName("day")
        public int day;

        /** Start time of the business's hours (format: HHMM, e.g., "0900" for 9 AM). */
        @SerializedName("start")
        public String start;

        /** End time of the business's hours (format: HHMM, e.g., "1800" for 6 PM). */
        @SerializedName("end")
        public String end;
    }
}