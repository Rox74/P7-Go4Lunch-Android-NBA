package com.nathba.go4lunch.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class YelpBusinessResponse {

    @SerializedName("businesses")
    public List<YelpBusiness> businesses;

    public static class YelpBusiness {
        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("image_url")
        public String imageUrl;

        @SerializedName("rating")
        public double rating;

        @SerializedName("location")
        public YelpLocation location;

        @SerializedName("phone")
        public String phone;

        @SerializedName("url")
        public String url;
    }

    public static class YelpLocation {
        @SerializedName("address1")
        public String address;
    }
}