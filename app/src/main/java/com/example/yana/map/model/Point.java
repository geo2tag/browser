package com.example.yana.map.model;

import android.util.Log;

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

    private JSONObject idObject;
    private String id;
    private JSONObject location;
    private JSONArray coordinatesArray;
    private Coordinate coordinates;
    private JSONObject dateObject;
    private long date;
    private String dateText;
    private JSONObject json;
    private String image;
    private String description;

    public Point(JSONObject point){
        try {
            idObject = point.getJSONObject("_id");
            id = idObject.getString("$oid");
            location = point.getJSONObject("location");
            coordinatesArray = location.getJSONArray("coordinates");
            coordinates = new Coordinate();
            coordinates.setLon(coordinatesArray.getDouble(0));
            coordinates.setLat(coordinatesArray.getDouble(1));
            dateObject = point.getJSONObject("date");
            date = (long) dateObject.get("$date");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateText = sdf.format(new Date(date));
            json = point.getJSONObject("json");
            image = json.getString("image_url");
            description = json.getString("description");
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
}
