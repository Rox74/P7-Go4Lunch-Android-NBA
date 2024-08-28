package com.nathba.go4lunch.models;

import java.util.List;

/**
 * Represents the response from the Overpass API.
 * The response contains a list of elements, each representing a geographical feature
 * such as a restaurant with its attributes.
 */
public class OverpassResponse {

    // List of elements returned by the Overpass API
    public List<Element> elements;

    /**
     * Represents an individual element in the Overpass API response.
     * Each element contains geographical coordinates, an identifier, and tags with additional information.
     */
    public class Element {

        // Latitude of the geographical feature
        public double lat;

        // Longitude of the geographical feature
        public double lon;

        // Unique identifier of the element
        public String id;

        // Tags providing additional details about the element
        public Tags tags;

        /**
         * Represents the tags associated with an element in the Overpass API response.
         * Tags provide additional information such as the name, address, photo URL, and rating.
         */
        public class Tags {

            // Name of the geographical feature (e.g., restaurant name)
            public String name;

            // Address of the geographical feature
            public String address;

            // URL of the photo associated with the geographical feature
            public String photoUrl;

            // Rating of the geographical feature
            public String rating;
        }
    }
}