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

        @SerializedName("display_phone")
        public String phone;

        @SerializedName("url")
        public String url;

        @SerializedName("hours")
        public List<YelpHours> hours; // Ajout des horaires d'ouverture
    }

    public static class YelpLocation {
        @SerializedName("address1")
        public String address;
    }

    // Nouvelle classe pour gérer les horaires
    public static class YelpHours {
        @SerializedName("open")
        public List<YelpOpen> open; // Liste des horaires d'ouverture
    }

    public static class YelpOpen {
        @SerializedName("day")
        public int day;

        @SerializedName("start")
        public String start;

        @SerializedName("end")
        public String end;
    }
}