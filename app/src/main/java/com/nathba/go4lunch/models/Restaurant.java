package com.nathba.go4lunch.models;

import org.osmdroid.util.GeoPoint;

public class Restaurant {
    private String name;
    private GeoPoint location;

    public Restaurant(String name, GeoPoint location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public GeoPoint getLocation() {
        return location;
    }
}