package com.example.navixpassanger.models;

public class Users {
    String name, email, imageUri, UID, dob;
    private double totalCarbonSaved;  // in kg CO2
    private int totalBusRides;
    private double totalDistanceTraveled;  // in kilometers

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public double getTotalCarbonSaved() {
        return totalCarbonSaved;
    }

    public void setTotalCarbonSaved(double totalCarbonSaved) {
        this.totalCarbonSaved = totalCarbonSaved;
    }

    public int getTotalBusRides() {
        return totalBusRides;
    }

    public void setTotalBusRides(int totalBusRides) {
        this.totalBusRides = totalBusRides;
    }

    public double getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }

    public void setTotalDistanceTraveled(double totalDistanceTraveled) {
        this.totalDistanceTraveled = totalDistanceTraveled;
    }

    public Users() {}

    public Users(String name, String email, String dob, String imageUri, String UID) {
        this.name = name;
        this.email = email;
        this.dob = dob;
        this.imageUri = imageUri;
        this.UID = UID;
    }

    public String getDOB() {
        return dob;
    }

    public void setDOB(String dob) {
        this.dob = dob;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
