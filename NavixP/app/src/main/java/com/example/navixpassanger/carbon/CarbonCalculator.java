package com.example.navixpassanger.carbon;

public class CarbonCalculator {
    // Constants for carbon emissions (kg CO2 per km)
    private static final double BUS_EMISSION_PER_KM = 0.089;  // Electric Bus
    private static final double CAB_EMISSION_PER_KM = 0.171;  // Regular Car/Cab

    public static double calculateCarbonSaved(double distanceInKm) {
        double cabEmissions = distanceInKm * CAB_EMISSION_PER_KM;
        double busEmissions = distanceInKm * BUS_EMISSION_PER_KM;
        return cabEmissions - busEmissions;
    }

    public static String getEnvironmentalImpact(double carbonSaved) {
        double treesEquivalent = carbonSaved / 21.7;
        return String.format("Your impact is equivalent to planting %.1f trees!",
                treesEquivalent);
    }
}