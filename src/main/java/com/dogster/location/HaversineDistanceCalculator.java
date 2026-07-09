package com.dogster.location;

import org.springframework.stereotype.Component;

@Component
public class HaversineDistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public double calculateKm(
            double sourceLatitude,
            double sourceLongitude,
            double targetLatitude,
            double targetLongitude
    ) {
        double latitudeDistance = Math.toRadians(targetLatitude - sourceLatitude);
        double longitudeDistance = Math.toRadians(targetLongitude - sourceLongitude);

        double sourceLatitudeRadians = Math.toRadians(sourceLatitude);
        double targetLatitudeRadians = Math.toRadians(targetLatitude);

        double haversine = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                + Math.cos(sourceLatitudeRadians) * Math.cos(targetLatitudeRadians)
                * Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2);

        double centralAngle = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));

        return EARTH_RADIUS_KM * centralAngle;
    }
}
