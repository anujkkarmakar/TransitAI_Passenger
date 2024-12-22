package com.example.navixpassanger.bus;

import java.util.List;

public class BusRoute {
    private String id;
    private String from;
    private String route_no;
    private List<String> stops;
    private double distance;

    // Constructor and getters/setters
    public BusRoute() {}

    public BusRoute(String id, String from, String route_no, List<String> stops) {
        this.id = id;
        this.from = from;
        this.route_no = route_no;
        this.stops = stops;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getRoute_no() {
        return route_no;
    }

    public void setRoute_no(String route_no) {
        this.route_no = route_no;
    }

    public List<String> getStops() {
        return stops;
    }

    public void setStops(List<String> stops) {
        this.stops = stops;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}