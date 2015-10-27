package com.example.yana.map.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Yana on 12.07.2015.
 */
public class Util {

    private static final String RADIUS_SHARED_PREF = "mySharedPreferences";
    private static final String RADIUS = "radius";
    private static final String JSON_SHARED_PREF = "resultJsonSharedPreferences";
    private static final String JSON = "resultJson";
    private static final String DATE_FROM_SHARED_PREF = "dateFromSharedPreferences";
    private static final String DATE_FROM = "dateFrom";
    private static final String DATE_TO_SHARED_PREF = "dateToSharedPreferences";
    private static final String DATE_TO = "dateTo";
    private static final String TIME_FROM_SHARED_PREF = "timeFromSharedPreferences";
    private static final String TIME_FROM = "timeFrom";
    private static final String TIME_TO_SHARED_PREF = "timeToSharedPreferences";
    private static final String TIME_TO = "timeTo";
    private static final String DATE_TIME_FROM_SHARED_PREF = "dateTimeFromSharedPreferences";
    private static final String DATE_TIME_FROM = "dateTimeFrom";
    private static final String DATE_TIME_TO_SHARED_PREF = "dateTimeToSharedPreferences";
    private static final String DATE_TIME_TO = "dateTimeTo";
    private static final String EQUATOR_PIXELS_SHARED_PREF = "equatorPixelsSharedPref";
    private static final String EQUATOR_PIXELS = "equatorPixels";
    private static final String CODE_SHARED_PREF = "codeSharedPreferences";
    private static final String CODE = "code" ;
    private static final String REFRESH_TOKEN_SHARED_PREF = "refreshTokenSharedPreferences";
    private static final String REFRESH_TOKEN = "refreshToken" ;
    private static final String COOKIE_SHARED_PREF = "cookieSharedPreferences";
    private static final String COOKIE = "cookie" ;

    public static int minD = 0;
    public static int minRadius = 1;
    public static long defaultDateFrom = 0;
    public static long defaultDateTo;
    public static String defaultJson = "";
    public static String defaultCode = "";
    public static String defaultStringDateFrom = "01-01-1970";
    public static String defaultStringTimeFrom = "0:0";

    public static void saveCookie(Context ctx, String cookie) {
        SharedPreferences preferences = ctx.getSharedPreferences(COOKIE_SHARED_PREF, Context.MODE_APPEND);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(COOKIE, cookie);
        editor.apply();
    }

    public static String getCookie(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(COOKIE_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getString(COOKIE, defaultCode);
    }

    public static void saveRefreshToken(Context ctx, String token) {
        SharedPreferences preferences = ctx.getSharedPreferences(REFRESH_TOKEN_SHARED_PREF, Context.MODE_APPEND);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(REFRESH_TOKEN, token);
        editor.apply();
    }

    public static String getRefreshToken(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(REFRESH_TOKEN_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getString(REFRESH_TOKEN, defaultCode);
    }

    public static void saveCode(Context ctx, String token) {
        SharedPreferences preferences = ctx.getSharedPreferences(CODE_SHARED_PREF, Context.MODE_APPEND);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CODE, token);
        editor.apply();
    }

    public static String getCode(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(CODE_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getString(CODE, defaultCode);
    }

    public static void saveD(Context ctx, int d) {
        SharedPreferences preferences = ctx.getSharedPreferences(EQUATOR_PIXELS_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(EQUATOR_PIXELS, d);
        editor.apply();
    }

    public static int getD(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(EQUATOR_PIXELS_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getInt(EQUATOR_PIXELS, minD);
    }

    public static void saveRadius(Context ctx, int radius) {
        SharedPreferences preferences = ctx.getSharedPreferences(RADIUS_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(RADIUS, radius);
        editor.apply();
    }

    public static int getRadius(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(RADIUS_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getInt(RADIUS, minRadius);
    }

    public static void saveJson(Context ctx, String resultJson) {
        SharedPreferences preferences = ctx.getSharedPreferences(JSON_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(JSON, resultJson);
        editor.apply();
    }

    public static String getJson(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(JSON_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getString(JSON, defaultJson);
    }

    public static void saveDateFrom(Context ctx, String dateFrom) {
        SharedPreferences preferences = ctx.getSharedPreferences(DATE_FROM_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(DATE_FROM, dateFrom);
        editor.apply();
    }

    public static String getDateFrom(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(DATE_FROM_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getString(DATE_FROM, defaultStringDateFrom);
    }

    public static void saveDateTo(Context ctx, String dateTo) {
        SharedPreferences preferences = ctx.getSharedPreferences(DATE_TO_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(DATE_TO, dateTo);
        editor.apply();
    }

    public static String getDateTo(Context ctx) {
        Calendar Current_Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Date Current_Date = Current_Calendar.getTime();
        String defaultStringDateTo = "" + DateFormat.format("dd-MM-yyyy", Current_Date);

        SharedPreferences preferences = ctx.getSharedPreferences(DATE_TO_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getString(DATE_TO, defaultStringDateTo);
    }

    public static void saveTimeFrom(Context ctx, String timeFrom) {
        SharedPreferences preferences = ctx.getSharedPreferences(TIME_FROM_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TIME_FROM, timeFrom);
        editor.apply();
    }

    public static String getTimeFrom(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(TIME_FROM_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getString(TIME_FROM, defaultStringTimeFrom);
    }

    public static void saveTimeTo(Context ctx, String timeTo) {
        SharedPreferences preferences = ctx.getSharedPreferences(TIME_TO_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TIME_TO, timeTo);
        editor.apply();
    }

    public static String getTimeTo(Context ctx) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String defaultStringTimeTo = "" + sdf.format(new Date());
        SharedPreferences preferences = ctx.getSharedPreferences(TIME_TO_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getString(TIME_TO, defaultStringTimeTo);
    }

    public static void saveDateTimeFrom(Context ctx, String dateFrom, String timeFrom) {
        SharedPreferences preferences = ctx.getSharedPreferences(DATE_TIME_FROM_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        long res = 0;
        try {
            long date = simpleDateFormat.parse(dateFrom).getTime();
            long time = timeFormat.parse(timeFrom).getTime();
            res = date + time;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        editor.putLong(DATE_TIME_FROM, res);
        editor.apply();
    }

    public static long getDateTimeFrom(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(DATE_TIME_FROM_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getLong(DATE_TIME_FROM, defaultDateFrom);
    }

    public static void saveDateTimeTo(Context ctx, String dateTo, String timeTo) {
        SharedPreferences preferences = ctx.getSharedPreferences(DATE_TIME_TO_SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        long res = 0;
        try {
            long date = simpleDateFormat.parse(dateTo).getTime();
            long time = timeFormat.parse(timeTo).getTime();
            res = date + time;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        editor.putLong(DATE_TIME_TO, res);
        editor.apply();
    }

    public static long getDateTimeTo(Context ctx) {
        Calendar Current_Calendar = Calendar.getInstance();
        Date Current_Date = Current_Calendar.getTime();
        defaultDateTo = Current_Date.getTime();
        SharedPreferences preferences = ctx.getSharedPreferences(DATE_TIME_TO_SHARED_PREF, Context.MODE_PRIVATE);
        return preferences.getLong(DATE_TIME_TO, defaultDateTo);
    }
}