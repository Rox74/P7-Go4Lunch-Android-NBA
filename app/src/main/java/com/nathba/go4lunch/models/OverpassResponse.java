package com.nathba.go4lunch.models;

import java.util.List;

public class OverpassResponse {
    public List<Element> elements;

    public class Element {
        public double lat;
        public double lon;
        public Tags tags;

        public class Tags {
            public String name;
        }
    }
}