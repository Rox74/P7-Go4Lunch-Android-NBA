package com.nathba.go4lunch.models;

import java.util.List;

/**
 * Represents the response from the Overpass API.
 * <p>
 * The response typically contains a list of geographical features (e.g., restaurants),
 * where each feature is represented as an {@link Element} with details such as coordinates,
 * an identifier, and additional tags.
 */
public class OverpassResponse {

    /** List of elements returned by the Overpass API, each representing a geographical feature. */
    public List<Element> elements;

    /**
     * Represents an individual element in the Overpass API response.
     * <p>
     * Each element includes the geographical coordinates, a unique identifier, and additional
     * information encapsulated in the {@link Tags} class.
     */
    public class Element {

        /** Latitude of the geographical feature. */
        public double lat;

        /** Longitude of the geographical feature. */
        public double lon;

        /** Unique identifier for the element. */
        public String id;

        /** Tags containing additional details about the element. */
        public Tags tags;

        /**
         * Represents the tags associated with an element in the Overpass API response.
         * <p>
         * Tags provide detailed information such as the name of the feature, its address,
         * a photo URL, and a rating.
         */
        public class Tags {

            /** The name of the geographical feature (e.g., the name of the restaurant). */
            public String name;

            /** The address of the geographical feature. */
            public String address;

            /** The URL of the photo associated with the geographical feature. */
            public String photoUrl;

            /** The rating of the geographical feature (optional, as a string). */
            public String rating;
        }
    }
}