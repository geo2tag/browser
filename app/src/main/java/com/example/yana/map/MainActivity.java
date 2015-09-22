package com.example.yana.map;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.example.yana.map.model.Coordinate;
import com.example.yana.map.model.Point;
import com.example.yana.map.util.MarkerInfoWindowAdapter;
import com.example.yana.map.util.Util;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
    private AsyncTask myTask;
    private static Menu myMenu;
    private SensorManager sensorManager;
    private Sensor sensorAccel;
    private Sensor sensorMagnet;
    static int rotation;
    private Triangle triangle;
    private static LatLng[] coords;
    static int myD = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        ctx = this;
        visibleMarkers = new HashMap<>();
        photoHashMap = new HashMap<>();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.setRetainInstance(true);
        mapFragment.getMapAsync(this);
        map = mapFragment.getMap();
        if (map == null) {
            finish();
            return;
        }
        Log.d("marker", "map " + map);

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

                if (savedLoc == null) {
                    savedLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
                    map.animateCamera(center);
                    map.moveCamera(center);
                    getPoints();
                    myD = Triangle.metersToEquatorPixels(latLng, METRES_IN_KILOMETRES * 2, map);
                    if (myD != 0)
                        Util.saveD(ctx, myD);
                    triangle = new Triangle(map, latLng, METRES_IN_KILOMETRES);
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
        });
    }

    public static URL makeURL(int number, int offset, int radius, float reserve, String date_from, String date_to, double lon, double lat) {
        try {
            return new URL("http://geomongo//instance/service/testservice/point?number=" + number + "&offset=" + offset + "&channel_ids=556721a52a2e7febd2744202&channel_ids=556721a52a2e7febd2744201" +
                    "&radius=" + (radius * reserve) + "&geometry={\"type\":\"Point\",\"coordinates\":[" + lon + "," + lat + "]}&date_from=" + date_from + "&date_to=" + date_to);
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
//                            pointsOnMap.add(point);
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

    public void clearMap() {
        photoHashMap.clear();
        visibleMarkers.clear();
        map.clear();
        pointsOnMap = null;
        addMyMarkers();
        coords = null;
    }

    public void setDates() {
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
        triangle = new Triangle(map, new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), METRES_IN_KILOMETRES);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
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

    public void getPoints() {
        setRefreshActionButtonState(true);
        if (myTask != null) {
            myTask.cancel(false);
            Log.d(LOG_TAG, "cancel myTask");
        }
        setDates();
        offset = 0;
        newPoints = new ArrayList<>();
        myTask = (new ParseTask()).execute();
    }

    public class ParseTask extends AsyncTask<Void, Void, String> {
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
                resultJson = null;

                if (url != null) {
                    try {
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setConnectTimeout(5000);
                        urlConnection.connect();

                        int responseCode = urlConnection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {

                            available = true;
                            InputStream inputStream = urlConnection.getInputStream();

                            StringBuffer buffer = new StringBuffer();

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
                triangle = new Triangle(map, new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), METRES_IN_KILOMETRES);
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
    float[] outR = new float[9];
    float[] valuesAccel = new float[3];
    float[] valuesMagnet = new float[3];
    float[] valuesResult = new float[3];
    private long lastUpdate = 0;

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
        SensorManager.remapCoordinateSystem(inR, x_axis, y_axis, outR);
        SensorManager.getOrientation(outR, valuesResult);
        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 100) {
            lastUpdate = curTime;
            if (triangle != null) {
//                    Log.d(LOG_TAG, "sum = " + sum);
//                    Log.d(LOG_TAG, " RADIANS [0] " + (valuesResult[0])
//                            + " [1] " + (valuesResult[1])
//                            + " [2] " + (valuesResult[2]));
//                    Log.d(LOG_TAG, "degrees [0] " + (float) toDegrees(valuesResult[0])
//                    + "degrees [1] " + (float) toDegrees(valuesResult[1])
//                    + "degrees [2] " + (float) toDegrees(valuesResult[2]));
//                    float all = (float) (toDegrees(valuesResult[0]) + toDegrees(valuesResult[1]) + toDegrees(valuesResult[2]));
                float value = getAverage((float) toDegrees(valuesResult[0]));
//                    Log.d(LOG_TAG, "all = " + all);
//                    if(toDegrees(valuesResult[0]) < 0) {
//                        value = getAverage(-all);
//                    }

//                    if(toDegrees(valuesResult[0]) < 0) {
//                        value = -value;
//                    }
//                if (value < 0)
//                    value = 360 + value;
//                    Log.d(LOG_TAG, "value to rotate " + value);
                triangle.updateTriangle(value);
                coords = triangle.getCoords();
                showPointsOnTriangle();
            }
        }
    }

    int window = 10;
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
        return result;
    }

    public void addMyMarkers() {
        Point p;
        pointsOnMap = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
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
                if(marker != null) {
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