package com.lanternsoftware.util;

public class MapUtils {
    private static final double RADIUS_EARTH = 6371000;

    /**
     * @return Distance between the two points in meters
     */
    public static double distance(double _latitude1, double _longitude1, double _latitude2, double _longitude2)
    {
        double latitude = Math.toRadians(_latitude2 - _latitude1);
        double longitude = Math.toRadians(_longitude2 - _longitude1);
        double a = Math.sin(latitude / 2) * Math.sin(latitude / 2) + Math.cos(Math.toRadians(_latitude1)) * Math.cos(Math.toRadians(_latitude2)) * Math.sin(longitude / 2) * Math.sin(longitude / 2);
        return RADIUS_EARTH * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /**
     * @return Time in ms to travel from one point to another as the crow flies at the given speed
     */
    public static long travelTime(double _latitude1, double _longitude1, double _latitude2, double _longitude2, double _speedMetersPerSecond) {
        return (long)(1000*distance(_latitude1, _longitude1, _latitude2, _longitude2)/_speedMetersPerSecond);
    }
}
