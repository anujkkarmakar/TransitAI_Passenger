package com.example.navixpassanger.location;

// LocationData.java
public class LocationData {
    public double latitude;
    public double longitude;
    public long timestamp;

    public LocationData() {
        // Required for Firebase
    }

    public LocationData(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}