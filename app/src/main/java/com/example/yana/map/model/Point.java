package com.example.yana.map.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Yana on 21.07.2015.
 */
public class Point {

    private String id;
    private Coordinate coordinates;
    private long date;
    private String dateText;
    private String image;
    private String description;
    private boolean isShown = true;

    public boolean isShown() {
        return isShown;
    }

    public void setShown(boolean shown) {
        this.isShown = shown;
    }

    public Point(JSONObject point){
        try {
            JSONObject idObject = point.getJSONObject("_id");
            id = idObject.getString("$oid");
            JSONObject location = point.getJSONObject("location");
            JSONArray coordinatesArray = location.getJSONArray("coordinates");
            coordinates = new Coordinate();
            coordinates.setLon(coordinatesArray.getDouble(0));
            coordinates.setLat(coordinatesArray.getDouble(1));
            JSONObject dateObject = point.getJSONObject("date");
            date = (long) dateObject.get("$date");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateText = sdf.format(new Date(date));
            JSONObject json = point.getJSONObject("json");
            image = json.getString("image_url");
            description = json.getString("description");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Coordinate getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double lat, double lon) {
        coordinates = new Coordinate();
        coordinates.setLat(lat);
        coordinates.setLon(lon);
    }

    public long getDate() {
        return date;
    }

    public String getImage() {
        return image;
    }

    public String getDateText() {
        return dateText;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}