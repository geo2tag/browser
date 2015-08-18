package com.example.yana.map.model;


/**
 * Created by Yana on 21.07.2015.
 */
public class Coordinate {
    private double lon;
    private double lat;

    public Coordinate() {

    }

    public Coordinate(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {

        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
