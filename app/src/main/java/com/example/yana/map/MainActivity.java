package com.example.yana.map;

import android.content.Context;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yana.map.model.Coordinate;
import com.example.yana.map.model.Point;
import com.example.yana.map.util.Util;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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

public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, LocationListener {

    private static final long MIN_TIME = 10;
    private static final float MIN_DISTANCE = 100;
    private static final int METRES_IN_KILOMETRES = 1000;
    private static final int MAX_TRY = 2;
    private static final String LOG_TAG = "myLogs";
    static final int NUMBER = 50;
    static final float RESERVE = 1.5f;

    private static GoogleMap map;
    static Context ctx;
    static Location myLocation;
    private static LatLng latLng;
    private static int offset = 0;
    private SupportMapFragment mapFragment;
    private LocationManager locationManager;
    private static URL url;
    private LatLng savedLoc;
    private static Boolean available = false;
    private static List<Point> points;
    private static Map<String, Marker> visibleMarkers;
    public static Map<String, String> photoHashMap;
    private static String dateTo;
    private static String dateFrom;
    private static String resultJson = "";
    private static int tryConnect = 0;
    private static List<Point> newPoints;
    private AsyncTask myTask;
    private static AsyncTask innerMyTask;
    private static Menu myMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ctx = this;
        visibleMarkers = new HashMap<>();
        photoHashMap = new HashMap<>();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.setRetainInstance(true);
        map = mapFragment.getMap();
        if (map == null) {
            finish();
            return;
        }

//        myMenu = (Menu) getLastCustomNonConfigurationInstance();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location lastKnownLocation = locationManager.getLastKnownLocation(provider);

        if (lastKnownLocation != null) {
            new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            locationManager.removeUpdates(this);
        }

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        mapFragment.getMapAsync(this);
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
                        {
                            marker.hideInfoWindow();
                            marker.showInfoWindow();
                        }
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
                    CameraUpdate center = CameraUpdateFactory
                            .newLatLng(latLng);
                    map.animateCamera(center);
                    map.moveCamera(center);

                    getPoints();
                    addPointsToMap(points);
                    savedLoc = new LatLng(location.getLatitude(), location.getLongitude());
                } else {
                    Coordinate coordinate = new Coordinate(location.getLongitude(), location.getLatitude());
                    double distance = CalculationDistance(coordinate, savedLoc);
                    if (distance >= (Util.getRadius(ctx) * 0.5)) {
                        getPoints();
                        savedLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                    addPointsToMap(points);
                }
            }
        });
    }

//    @Override
//    public Menu onRetainCustomNonConfigurationInstance() {
//        return myMenu;
//    }

    public static URL makeURL(int number, int offset, int radius, float reserve, String dateTimeFrom, String dateTimeTo, double lon, double lat) {
        try {
            return new URL("http://geomongo/instance/service/testservice/point?number=" + number + "&offset=" + offset + "&channel_ids=556721a52a2e7febd2744202&channel_ids=556721a52a2e7febd2744201" +
                    "&radius=" + (radius * reserve) + "&geometry={\"type\":\"Point\",\"coordinates\":[" + lon + "," + lat + "]}&date_from=" + dateTimeFrom + "&date_to=" + dateTimeTo);
        } catch (MalformedURLException e) {
            Toast.makeText(ctx, "Bad URL", Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "bad url");
            e.printStackTrace();
        }
        return null;
    }

    private static void addPointsToMap(List<Point> points) {
        if ((map != null) && (points != null)) {
            Log.d("points", "" + points);
            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            Location location = map.getMyLocation();
            if(location != null) {
                for (final Point point : points) {
                    if (bounds.contains(new LatLng(point.getCoordinates().getLat(), point.getCoordinates().getLon()))
                            && (CalculationDistance(point.getCoordinates(), new LatLng(location.getLatitude(), location.getLongitude())) <= Util.getRadius(ctx))
                            && ((point.getDate() >= Util.getDateTimeFrom(ctx)) && (point.getDate() <= Util.getDateTimeTo(ctx)))) {

                        if (!visibleMarkers.containsKey(point.getId())) {
                            MarkerOptions markerOptions = getMarkerForItem(point);
                            Marker myMarker = map.addMarker(markerOptions);
                            visibleMarkers.put(point.getId(), myMarker);
                            photoHashMap.put(myMarker.getId(), point.getImage());
                            map.setInfoWindowAdapter(new MarkerInfoWindowAdapter());
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
    }

    public void setDates() {
        dateFrom = "" + DateFormat.format("yyyy-MM-dd", new Date(Util.getDateTimeFrom(ctx))) +
                "T" + DateFormat.format("HH:mm", new Date(Util.getDateTimeFrom(ctx)));
        dateTo = "" + DateFormat.format("yyyy-MM-dd", new Date(Util.getDateTimeTo(ctx))) +
                "T" + DateFormat.format("HH:mm", new Date(Util.getDateTimeTo(ctx)));
    }

    public static class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private LayoutInflater inflater;

        public MarkerInfoWindowAdapter() {
            inflater = (LayoutInflater) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(final Marker marker) {

            View view = inflater.inflate(R.layout.info_window, null);

            if (photoHashMap.containsKey(marker.getId())) {
                final ImageView imageView = (ImageView) view.findViewById(R.id.image);
                final String photoURI = photoHashMap.get(marker.getId());
                Picasso.with(ctx).load(photoURI).resize(130, 130).centerCrop()
                        .placeholder(R.drawable.ic_launcher).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                        if (marker.isInfoWindowShown()) {
                            marker.hideInfoWindow();
                            marker.showInfoWindow();
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });
            }

            TextView tvName = (TextView) view.findViewById(R.id.name);
            TextView tvDate = (TextView) view.findViewById(R.id.date);
            tvName.setText(marker.getTitle());
            tvDate.setText(marker.getSnippet());

            return view;
        }
    }

    private static double CalculationDistance(Coordinate coordinates, LatLng latLng) {
        return CalculationDistanceByCoord(coordinates.getLat(), coordinates.getLon(), latLng.latitude, latLng.longitude);
    }

    private static double CalculationDistanceByCoord(double myLat, double myLon, double pointLat, double pointLon) {
        float[] results = new float[1];
        Location.distanceBetween(myLat, myLon, pointLat, pointLon, results);
        return (results[0] / METRES_IN_KILOMETRES);
    }

    private static MarkerOptions getMarkerForItem(Point point) {
        return new MarkerOptions().position(new LatLng(point.getCoordinates().getLat(),
                point.getCoordinates().getLon())).title(point.getDescription()).snippet(point.getDateText());
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

        CameraUpdate z = CameraUpdateFactory.zoomTo(getZoomLevel(Util.getRadius(this) * METRES_IN_KILOMETRES));
        map.moveCamera(z);

        if (myLocation != null && map.isMyLocationEnabled()) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            CameraUpdate center = CameraUpdateFactory
                    .newLatLng(latLng);
            map.animateCamera(center);
            map.moveCamera(center);
//            getPoints();
            Location loc = map.getMyLocation();
            if (loc != null) {
                savedLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
            }
        }
    }

    private float getZoomLevel(int radius) {
        double scale = radius / 500;
        float zoomLevel = (float) (16 - Math.log(scale) / Math.log(2));
        return --zoomLevel;
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        if (myTask != null) {
            myTask.cancel(false);
            Log.d(LOG_TAG, "cancel myTask");
        }
        if (innerMyTask != null) {
            innerMyTask.cancel(false);
            Log.d(LOG_TAG, "cancel innerMyTask");
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

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), 1);
                break;
            case R.id.menuRefresh:
                getPoints();
                break;
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
        if (innerMyTask != null) {
            innerMyTask.cancel(false);
            Log.d(LOG_TAG, "cancel innerMyTask");
        }
        setDates();
        resultJson = "";
        offset = 0;
        newPoints = new ArrayList<>();
        url = makeURL(NUMBER, offset, Util.getRadius(ctx), RESERVE,
                dateFrom, dateTo, myLocation.getLongitude(), myLocation.getLatitude());
        myTask = (new ParseTask()).execute();
    }

    public static class ParseTask extends AsyncTask<Void, Void, String> {
        private HttpURLConnection urlConnection = null;
        private BufferedReader reader = null;

        @Override
        protected String doInBackground(Void... params) {

            if (isCancelled()) return null;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
                System.setProperty("http.keepAlive", "false");
            }

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
            } catch (IOException e) {
                available = false;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            JSONArray jsonPoints = null;

            if (available) {

                tryConnect = 0;
                try {
                    jsonPoints = new JSONArray(strJson);
                    for (int i = 0; i < jsonPoints.length(); i++) {
                        JSONObject point = jsonPoints.getJSONObject(i);
                        Point p = new Point(point);
                        newPoints.add(p);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(LOG_TAG, "Service unavailable");
                tryConnect++;
            }

            if (!strJson.equals("[]") && (tryConnect < MAX_TRY)) {
                if (available) {
                    offset += NUMBER;
                }
                url = makeURL(NUMBER, offset, Util.getRadius(ctx), RESERVE, dateFrom, dateTo,
                        myLocation.getLongitude(), myLocation.getLatitude());
                innerMyTask = (new ParseTask()).execute();
            } else {
                if (tryConnect >= MAX_TRY) {
                    tryConnect = 0;
                    Toast.makeText(ctx, "Check your internet connection", Toast.LENGTH_SHORT).show();
                }
                if (newPoints.size() != 0) {
                    clearMap();
                    points = newPoints;
                    addPointsToMap(points);
                }
                setRefreshActionButtonState(false);
            }
        }

        @Override
        protected void onCancelled(String result) {
            Log.d(LOG_TAG, "onCancelled(Void) start");
            super.onCancelled(result);
            tryConnect = 0;
            Log.d(LOG_TAG, "onCancelled(Void) finish");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d("log", "Cancel");
        }
    }
}
