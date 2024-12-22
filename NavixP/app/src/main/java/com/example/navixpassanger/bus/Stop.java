package com.example.navixpassanger.bus;

import java.util.Objects;

public class Stop {
    private String id;
    private String stop_id;
    private String stop_name;
    private double stop_lat;
    private double stop_lon;

    // Empty constructor required for Firestore
    public Stop() {}

    // Constructor with parameters
    public Stop(String stop_id, String stop_name, double stop_lat, double stop_lon) {
        this.stop_id = stop_id;
        this.stop_name = stop_name;
        this.stop_lat = stop_lat;
        this.stop_lon = stop_lon;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStop_id() {
        return stop_id;
    }

    public void setStop_id(String stop_id) {
        this.stop_id = stop_id;
    }

    public String getStop_name() {
        return stop_name;
    }

    public void setStop_name(String stop_name) {
        this.stop_name = stop_name;
    }

    public double getLat() {
        return stop_lat;
    }

    public void setLat(double stop_lat) {
        this.stop_lat = stop_lat;
    }

    public double getLon() {
        return stop_lon;
    }

    public void setLon(double stop_lon) {
        this.stop_lon = stop_lon;
    }

    @Override
    public String toString() {
        return "Stop{" +
                "id='" + id + '\'' +
                ", stop_id='" + stop_id + '\'' +
                ", stop_name='" + stop_name + '\'' +
                ", stop_lat=" + stop_lat +
                ", stop_lon=" + stop_lon +
                '}';
    }

    // Optional: Add equals and hashCode methods if needed
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stop stop = (Stop) o;
        return Objects.equals(stop_id, stop.stop_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stop_id);
    }
}