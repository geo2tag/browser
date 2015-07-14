package com.example.yana.map.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.http.conn.ConnectionPoolTimeoutException;

/**
 * Created by Yana on 12.07.2015.
 */
public class Util {

    public static int minRadius = 1;

    public static void saveRadius(Context ctx, int radius) {
        SharedPreferences preferences = ctx.getSharedPreferences("mySharedPreferences", ctx.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("radius", radius);
        editor.commit();
    };

    public static int getRadius(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences("mySharedPreferences", ctx.MODE_PRIVATE);
        return preferences.getInt("radius", minRadius);
    };
}