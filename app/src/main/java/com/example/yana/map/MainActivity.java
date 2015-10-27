package com.example.yana.map;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.widget.Toast;

import com.example.yana.map.model.Coordinate;
import com.example.yana.map.model.Point;
import com.example.yana.map.util.MarkerInfoWindowAdapter;
import com.example.yana.map.util.Util;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static java.lang.Math.*;

public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, LocationListener {

    private static final long MIN_TIME = 10;
    private static final float MIN_DISTANCE = 100;
    static final int METRES_IN_KILOMETRES = 1000;
    private static final String LOG_TAG = "myLogs";
    static final int NUMBER = 50;
    static final float RESERVE = 1.5f;
    static String CLIENT_ID = "560161143796-0dh7u04hdqmustsv4hu4usvk8a6eqnmc.apps.googleusercontent.com";
    static String CLIENT_SECRET = "vKycLSmXidvxlg-HslNR8awW";
    static String OATH_URL = "http://demo.geo2tag.org/instance/login";

    private static GoogleMap map;
    static Context ctx;
    static Location myLocation;
    private static LatLng latLng;
    private static int offset = 0;
    private LocationManager locationManager;
    private LatLng savedLoc;
    private static List<Point> points;
    private static List<Point> pointsOnMap;
    private static Map<String, Marker> visibleMarkers;
    public static Map<String, String> photoHashMap;
    private static String dateTo;
    private static String dateFrom;
    private static List<Point> newPoints;
    private static AsyncTask myTask;
    private static Menu myMenu;
    private SensorManager sensorManager;
    private Sensor sensorAccel;
    private Sensor sensorMagnet;
    static int rotation;
    private static Triangle triangle;
    private static LatLng[] coords;
    static int myD = 0;
    static String cookie = "";
    static String authCode;

    private static final int T_CONST = METRES_IN_KILOMETRES;
    static Boolean success = false;
    static Dialog auth_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ctx = this;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        auth_dialog = new Dialog(MainActivity.this);

        Object saved = getLastCustomNonConfigurationInstance();
        if (saved != null) {
            success = (Boolean) saved;
        }

        if(!success) {
            Authorization.execute(auth_dialog);
        }

        visibleMarkers = new HashMap<>();
        photoHashMap = new HashMap<>();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.setRetainInstance(true);
        mapFragment.getMapAsync(this);
    }

    public Object onRetainCustomNonConfigurationInstance() {
        return success;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        auth_dialog.dismiss();
    }

    public static URL makeURL(int number, int offset, int radius, float reserve, String date_from, String date_to, double lon, double lat) {
        try {

            //url for local service
//            return new URL("http://geomongo//instance/service/testservice/point?number=" + number + "&offset=" + offset + "&channel_ids=556721a52a2e7febd2744202&channel_ids=556721a52a2e7febd2744201" +
//                    "&radius=" + (radius * reserve) + "&geometry={\"type\":\"Point\",\"coordinates\":[" + lon + "," + lat + "]}&date_from=" + date_from + "&date_to=" + date_to);

            //url for test service demo.geo2tag.org
            return new URL("http://demo.geo2tag.org//instance/service/testservice/point?" +
                    "bc_from=false&bc_to=false&number=" + number + "&offset=" + offset +
                    "&channel_ids=556721a52a2e7febd2744202&channel_ids=556721a52a2e7febd2744201" +
                    "&radius=" + (radius * reserve) + "&geometry={\"type\":\"Point\",\"coordinates\":[" +
                    lon + "," + lat + "]}&date_from=" + date_from + ":00.00&date_to=" + date_to + ":00.00");
        } catch (MalformedURLException e) {
            Toast.makeText(ctx, "Bad URL", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "bad url");
            e.printStackTrace();
        }
        return null;
    }

    private static void addPointsToMap(List<Point> points) {
        if ((map != null) && (points != null)) {
//            pointsOnMap = new ArrayList<>();
            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            Location location = map.getMyLocation();
            if (location != null) {
                for (final Point point : points) {
//                    Log.d("marker", "bounds " + bounds.contains(new LatLng(point.getCoordinates().getLat(), point.getCoordinates().getLon())));
//                    if (bounds.contains(new LatLng(point.getCoordinates().getLat(), point.getCoordinates().getLon()))
                    if ((CalculationDistance(point.getCoordinates(), new LatLng(location.getLatitude(), location.getLongitude())) <= Util.getRadius(ctx))
                            && ((point.getDate() >= Util.getDateTimeFrom(ctx)) && (point.getDate() <= Util.getDateTimeTo(ctx)))) {
                        if (!visibleMarkers.containsKey(point.getId())) {
                            MarkerOptions markerOptions = getMarkerForItem(point);
                            Marker myMarker = map.addMarker(markerOptions);
                            visibleMarkers.put(point.getId(), myMarker);
                            photoHashMap.put(myMarker.getId(), point.getImage());
                            map.setInfoWindowAdapter(new MarkerInfoWindowAdapter(ctx));
                        }
                    } else {
                        if (visibleMarkers.containsKey(point.getId())) {
                            String key = visibleMarkers.get(point.getId()).getId();
                            photoHashMap.remove(key);
                            visibleMarkers.get(point.getId()).remove();
                            visibleMarkers.remove(point.getId());
                        }
                    }
                }
            }
        }
    }

    public static void clearMap() {
        photoHashMap.clear();
        visibleMarkers.clear();
        map.clear();
        pointsOnMap = null;
        addMyMarkers();
        coords = null;
    }

    public static void setDates() {
        dateFrom = "" + DateFormat.format("yyyy-MM-dd", new Date(Util.getDateTimeFrom(ctx))) +
                "T" + DateFormat.format("HH:mm", new Date(Util.getDateTimeFrom(ctx)));
        dateTo = "" + DateFormat.format("yyyy-MM-dd", new Date(Util.getDateTimeTo(ctx))) +
                "T" + DateFormat.format("HH:mm", new Date(Util.getDateTimeTo(ctx)));
    }

    public static double CalculationDistance(Coordinate coordinates, LatLng latLng) {
        return CalculationDistanceByCoord(coordinates.getLat(), coordinates.getLon(), latLng.latitude, latLng.longitude);
    }

    public static double CalculationDistanceByCoord(double myLat, double myLon, double pointLat, double pointLon) {
        float[] results = new float[1];
        Location.distanceBetween(myLat, myLon, pointLat, pointLon, results);
        return (results[0] / METRES_IN_KILOMETRES);
    }

    private static MarkerOptions getMarkerForItem(Point point) {
        return new MarkerOptions().position(new LatLng(point.getCoordinates().getLat(),
                point.getCoordinates().getLon())).title(point.getDescription()).snippet(point.getDateText()).
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
    }

    @Override
    protected void onResume() {
        super.onResume();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

        sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorMagnet, SensorManager.SENSOR_DELAY_NORMAL);

        if (map != null) {
            CameraUpdate z = CameraUpdateFactory.zoomTo(getZoomLevel(Util.getRadius(this) * METRES_IN_KILOMETRES));
            map.moveCamera(z);

            if (myLocation != null && map.isMyLocationEnabled()) {
                LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                Location loc = map.getMyLocation();
                if (loc != null) {
                    savedLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
                }
                CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
                map.animateCamera(center);
                map.moveCamera(center);
                clearMap();
                triangle = new Triangle(map, new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), METRES_IN_KILOMETRES);
            }
            addPointsToMap(points);
        }

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        rotation = display.getRotation();
    }

    private float getZoomLevel(int radius) {
        double scale = radius / 500;
        float zoomLevel = (float) (16 - log(scale) / log(2));
        return --zoomLevel;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (visibleMarkers != null) {
            visibleMarkers.clear();
            photoHashMap.clear();
        }
        sensorManager.unregisterListener(listener);
        locationManager.removeUpdates(this);
        if (myTask != null) {
            myTask.cancel(false);
            Log.d(LOG_TAG, "cancel myTask");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        myMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (myLocation != null) {
            switch (item.getItemId()) {
                case R.id.action_settings:
                    startActivityForResult(new Intent(this, SettingsActivity.class), 1);
                    break;
                case R.id.menuRefresh:
                    getPoints();
                    if (myLocation != null && map.isMyLocationEnabled()) {
                        CameraUpdate z = CameraUpdateFactory.zoomTo(getZoomLevel(Util.getRadius(this) * METRES_IN_KILOMETRES));
                        map.moveCamera(z);
                        LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                        CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
                        map.animateCamera(center);
                        map.moveCamera(center);
                    }
                    break;
            }
        }

        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public static void setRefreshActionButtonState(final boolean refreshing) {
        if (myMenu != null) {
            final MenuItem refreshItem = myMenu.findItem(R.id.menuRefresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.progressbar);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getPoints();
        clearMap();
        triangle = new Triangle(map, new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), T_CONST);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (map == null) {
            finish();
            return;
        }
        CameraUpdate z = CameraUpdateFactory.zoomTo(getZoomLevel(Util.getRadius(this) * METRES_IN_KILOMETRES));
        map.moveCamera(z);
        addMyMarkers();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location lastKnownLocation = locationManager.getLastKnownLocation(provider);

        if (lastKnownLocation != null) {
            new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            locationManager.removeUpdates(this);
        }

        map.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                marker.showInfoWindow();
                final Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (marker.isInfoWindowShown())
                            handler.removeCallbacks(this);
                        marker.hideInfoWindow();
                        marker.showInfoWindow();
                    }
                });
                return true;
            }
        });

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                myLocation = location;

                if(success) {

                if (savedLoc == null) {
                    savedLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
                    map.animateCamera(center);
                    map.moveCamera(center);
                    getPoints();
                    myD = Triangle.metersToEquatorPixels(latLng, METRES_IN_KILOMETRES * 2, map);
                    if (myD != 0)
                        Util.saveD(ctx, myD);
                    triangle = new Triangle(map, latLng, T_CONST);
                } else {
                    if (Triangle.groundOverlay != null)
                        Triangle.groundOverlay.setPosition(latLng);
                    Coordinate coordinate = new Coordinate(location.getLongitude(), location.getLatitude());
                    double distance = CalculationDistance(coordinate, savedLoc);
                    if (distance >= (Util.getRadius(ctx) * 0.5)) {
                        getPoints();
                    }
                    savedLoc = new LatLng(location.getLatitude(), location.getLongitude());
                }
            }
                geomagneticField = new GeomagneticField(
                        (float) location.getLatitude(),
                        (float) location.getLongitude(),
                        (float) location.getAltitude(),
                        System.currentTimeMillis());
            }
        });

        map.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(LOG_TAG, "Status changed: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(LOG_TAG, "enable " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(LOG_TAG, "disable " + provider);
    }

    public static void getPoints() {
        if (success) {
            setRefreshActionButtonState(true);
            if (myTask != null) {
                myTask.cancel(false);
                Log.d(LOG_TAG, "cancel myTask");
            }
            setDates();
            offset = 0;
            newPoints = new ArrayList<>();
            myTask = (new ParseTask()).execute();
        } else {
            Authorization.execute(auth_dialog);
        }
    }

    public static class ParseTask extends AsyncTask<Void, Void, String> {
        private HttpURLConnection urlConnection = null;
        private BufferedReader reader = null;
        private Boolean available = false;

        String returnJson = "";

        @Override
        protected String doInBackground(Void... params) {

            if (isCancelled()) return null;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                System.setProperty("http.keepAlive", "false");
            }

            String resultJson;
            do {

                URL url = makeURL(NUMBER, offset, Util.getRadius(ctx), RESERVE,
                        dateFrom, dateTo, myLocation.getLongitude(), myLocation.getLatitude());
                Log.d("", "get points URL " + url);
                resultJson = null;

                if (url != null) {
                    try {
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setConnectTimeout(5000);
                        urlConnection.setRequestProperty("Cookie", Util.getCookie(ctx));
                        urlConnection.connect();

                        int responseCode = urlConnection.getResponseCode();
                        Log.d("", "responseCode " + responseCode);
                        if (responseCode == HttpURLConnection.HTTP_OK) {

                            if(!cookie.equals("")) {
                                System.out.println("Set cookie: " + CookieManager.getInstance().getCookie(String.valueOf(url)));
                            }

                            available = true;
                            InputStream inputStream = urlConnection.getInputStream();

                            StringBuilder buffer = new StringBuilder();

                            reader = new BufferedReader(new InputStreamReader(inputStream));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                buffer.append(line);
                            }

                            resultJson = buffer.toString();
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        Log.i(LOG_TAG, "malformed url");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.i(LOG_TAG, "ioexception");
                        available = false;
                    } finally {
                        if (urlConnection != null) {
                            Log.i(LOG_TAG, "deleting connection");
                            urlConnection.disconnect();
                        }
                    }
                }

                if (available) {
                    Log.i(LOG_TAG, "connection iteration, offset = " + offset);
                    offset += NUMBER;
                    returnJson += resultJson;
                }
            } while (null != resultJson && !resultJson.equals("") &&
                    !resultJson.equals("[]"));

            JSONArray jsonPoints;
            try {
                Log.i(LOG_TAG, "parsing " + returnJson);
                jsonPoints = new JSONArray(returnJson);
                for (int i = 0; i < jsonPoints.length(); i++) {
                    JSONObject point = jsonPoints.getJSONObject(i);
                    Point p = new Point(point);
                    newPoints.add(p);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i(LOG_TAG, "background work is finished");
            return returnJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            if (!available) {
                Toast.makeText(ctx, "Server unavailable", Toast.LENGTH_SHORT).show();
            }
            if (newPoints.size() != 0) {
                clearMap();
                triangle = new Triangle(map, new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), T_CONST);
                points = newPoints;
                addPointsToMap(points);
            }
            setRefreshActionButtonState(false);
        }

        @Override
        protected void onCancelled(String result) {
            Log.d(LOG_TAG, "onCancelled start");
            super.onCancelled(result);
            Log.d(LOG_TAG, "onCancelled finish");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d(LOG_TAG, "Cancel");
        }
    }

    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    System.arraycopy(event.values, 0, valuesAccel, 0, 3);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    System.arraycopy(event.values, 0, valuesMagnet, 0, 3);
                    break;
            }
            getActualDeviceOrientation();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    float[] inR = new float[9];
    float[] iM = new float[9];
    float[] outR = new float[9];
    float[] valuesAccel = new float[3];
    float[] valuesMagnet = new float[3];
    float[] valuesResult = new float[3];
    private long lastUpdate = 0;
    float angle;
    float temp = 0;
    float floatAzimuth = 0;
    float s = 0;
    private GeomagneticField geomagneticField;

    void getActualDeviceOrientation() {
        SensorManager.getRotationMatrix(inR, null, valuesAccel, valuesMagnet);
        int x_axis = SensorManager.AXIS_X;
        int y_axis = SensorManager.AXIS_Y;
        switch (rotation) {
            case (Surface.ROTATION_0):
                break;
            case (Surface.ROTATION_90):
                x_axis = SensorManager.AXIS_Y;
                y_axis = SensorManager.AXIS_MINUS_X;
                break;
            case (Surface.ROTATION_180):
                y_axis = SensorManager.AXIS_MINUS_Y;
                break;
            case (Surface.ROTATION_270):
                x_axis = SensorManager.AXIS_MINUS_Y;
                y_axis = SensorManager.AXIS_X;
                break;
            default:
                break;
        }
        SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
        SensorManager.remapCoordinateSystem(inR, x_axis, y_axis, outR);
        SensorManager.getOrientation(outR, valuesResult);
        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 100) {
            lastUpdate = curTime;
            if (triangle != null) {
//                Log.d(LOG_TAG, "ACCEL " + toDegrees(valuesAccel[0])
//                        + " [1] " + toDegrees(valuesAccel[1])
//                        + " [2] " + toDegrees(valuesAccel[2]));
//                Log.d(LOG_TAG, "MAG " + toDegrees(valuesMagnet[0])
//                        + " [1] " + toDegrees(valuesMagnet[1])
//                        + " [2] " + toDegrees(valuesMagnet[2]));
//                Log.d(LOG_TAG, " RADIANS [0] " + (valuesResult[0])
//                        + " [1] " + (valuesResult[1])
//                        + " [2] " + (valuesResult[2]));
//                Log.d(LOG_TAG, "degrees[0] " + (float) toDegrees(valuesResult[0])
//                        + " degrees[1] " + (float) toDegrees(valuesResult[1])
//                        + " degrees[2] " + (float) toDegrees(valuesResult[2]));
//                float floatAzimuth = (float) (toDegrees(valuesResult[0]) + 360) % 360;

                floatAzimuth = (float) toDegrees(valuesResult[0]);
                if (abs(floatAzimuth - angle) >= 15) {
//                if (geomagneticField != null) {
//                    floatAzimuth += geomagneticField.getDeclination();
//                }
//                if(floatAzimuth < 0) {
//                    floatAzimuth += 360;
//                }
                    float value = 0;
//                Log.d(LOG_TAG, "floatAzimuth = " + floatAzimuth);
                    if (map != null) {
//                  value = avg(angle, floatAzimuth);
//                        temp = angle;
//                        while (temp < floatAzimuth) {

//                            temp += 0.5;
//                        temp = getAverage(floatAzimuth);
//                    if(temp <= angle) {
//                            value = getAverage(temp);
                        value = avg(angle, temp);  //old
                    }
//                    else {
//                        value = avg(temp, angle);
//                    }
                    angle = value;
                    s = floatAzimuth;
//                        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(14).bearing(value).build();
//                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000, null);
//                    triangle.updateTriangle(value);
//                    Log.d(LOG_TAG, "value to rotate " + value);
//                    }
//                        }
//                    }
                    angle = (float) toDegrees(valuesResult[0]);
                    coords = triangle.getCoords();
                    showPointsOnTriangle();
                }
            }
        }
    }

    public float avg(float oldAngle, float angle) {
        float diff = abs(angle - oldAngle);
//        if ((floatAzimuth >= s) && (floatAzimuth - s) > 180) {
//            diff = 360 - (floatAzimuth - s);
//        }
//        else if((floatAzimuth < s) && (s - floatAzimuth) > 180) {
//            diff = 360 - (s - floatAzimuth);
//        }
//        Log.d(LOG_TAG, "diff " + diff);
        if (angle < oldAngle) {
            return oldAngle - diff / 2;
        }
        return oldAngle + diff / 2;

//        float diff = angle - oldAngle;
////        if ((floatAzimuth >= oldAngle) && (floatAzimuth + oldAngle) > 180) {
////            diff = 360 - (floatAzimuth + oldAngle);
////        }
////        else if((floatAzimuth < oldAngle) && (oldAngle - floatAzimuth) > 180) {
////            diff = 360 - (oldAngle - floatAzimuth);
////        }
//        Log.d(LOG_TAG, "diff " + diff);
//        if(angle < oldAngle) {
//            return oldAngle - diff / 2;
//        }
//        return oldAngle + diff / 2;
    }

    int window = 20;
    float sum = 0;
    float[] arr = new float[window];
    int index = 0;

    public float getAverage(float value) {
        float result;
        sum -= arr[index];
        arr[index] = value;
        sum += arr[index];
        index++;
        if (index >= window) {
            index = 0;
        }
        result = sum / window;
//        Log.d(LOG_TAG, "res" + result);
//        Log.d(LOG_TAG, "abs(angle - value)" + abs(angle - value));
//        if (abs(angle - value) > 180) {
//            result = 360 - abs(result);
//        }
//        Log.d(LOG_TAG, "value " + value);
//        Log.d(LOG_TAG, "result " + result);
        return result;
    }

    public static void addMyMarkers() {
        Point p;
        pointsOnMap = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            p = new Point(new JSONObject());
            p.setId("id" + i);
            p.setDescription("id" + i);
            switch (i) {
                case 0:
                    p.setCoordinates(59.9978704, 30.3258796);
                    break;
                case 1:
                    p.setCoordinates(59.9998704, 30.3108796);
                    break;
                case 2:
                    p.setCoordinates(59.9848704, 30.3208796);
                    break;
                case 3:
                    p.setCoordinates(59.9908704, 30.3308796);
                    break;
                case 4:
                    p.setCoordinates(59.973386, 30.321369);
                    break;
                case 5:
                    p.setCoordinates(59.970852, 30.320211);
                    break;
            }
            pointsOnMap.add(p);
            MarkerOptions markerOptions = getMarkerForItem(p);
            Marker myMarker = map.addMarker(markerOptions);
            visibleMarkers.put(p.getId(), myMarker);
            photoHashMap.put(myMarker.getId(), p.getImage());
            map.setInfoWindowAdapter(new MarkerInfoWindowAdapter(ctx));
        }
    }

    public static void showPointsOnTriangle() {
        if (coords != null) {
            for (Point point : pointsOnMap) {
                Marker marker = visibleMarkers.get(point.getId());
                if (marker != null) {
                    if (isInTriangle(point)) {
                        if (!marker.isInfoWindowShown())
                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        else {
                            if (point.isShown()) {
                                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                marker.showInfoWindow();
                                point.setShown(false);
                            }
                        }
                    } else {
                        if (!marker.isInfoWindowShown())
                            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        else {
                            if (!point.isShown()) {
                                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                marker.showInfoWindow();
                                point.setShown(true);
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean isInTriangle(Point point) {
        if (coords != null) {
            if (((point.getCoordinates().getLat() - coords[0].latitude) * (coords[0].longitude - coords[1].longitude) -
                    (point.getCoordinates().getLon() - coords[0].longitude) * (coords[0].latitude - coords[1].latitude) >= 0)) {
                if ((point.getCoordinates().getLat() - coords[1].latitude) * (coords[1].longitude - coords[2].longitude) -
                        (point.getCoordinates().getLon() - coords[1].longitude) * (coords[1].latitude - coords[2].latitude) >= 0) {
                    if ((point.getCoordinates().getLat() - coords[2].latitude) * (coords[2].longitude - coords[0].longitude) -
                            (point.getCoordinates().getLon() - coords[2].longitude) * (coords[2].latitude - coords[0].latitude) >= 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}