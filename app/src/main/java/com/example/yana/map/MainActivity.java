package com.example.yana.map;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.yana.map.util.Util;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends ActionBarActivity implements OnMapReadyCallback, LocationListener {

    GoogleMap map;
    SupportMapFragment mapFragment;
    LocationManager locationManager;
    String provider;
    Circle mCircle;
    private static final long MIN_TIME = 10;
    private static final float MIN_DISTANCE = 100;
    final String TAG = "myLogs";
    final int metresInKiilometres = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null) {
            finish();
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true);

        Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
        if(lastKnownLocation != null) {
            LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            locationManager.removeUpdates(this);
        }

        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (mCircle == null) {
                    drawCircle(latLng);
                } else {
                    updateCircle(latLng);
                }
            }
        });
        mapFragment.getMapAsync(this);
    }

    private void updateCircleRadius(Circle circle, int radius) {
        circle.setRadius(radius);
    }

    private void updateCircle(LatLng position) {
        mCircle.setCenter(position);
    }

    private void drawCircle(LatLng position){
        double radiusInMeters = Util.getRadius(this) * metresInKiilometres;
        int strokeColor = 0xffff7f50; //outline
        int shadeColor = 0x44ff7f50; //opaque fill

        CircleOptions circleOptions = new CircleOptions()
                .center(position)
                .radius(radiusInMeters)
                .fillColor(shadeColor)
                .strokeColor(strokeColor)
                .strokeWidth(1);
        mCircle = map.addCircle(circleOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
        //Toast.makeText(this, "Selected Provider " + provider, Toast.LENGTH_SHORT).show();

        CameraUpdate z = CameraUpdateFactory.zoomTo(getZoomLevel(Util.getRadius(this) * metresInKiilometres));
        map.moveCamera(z);

        if(mCircle != null) {
            updateCircleRadius(mCircle, Util.getRadius(this) * metresInKiilometres);
        }
    }

    private float getZoomLevel(int radius) {
        double scale = radius/500;
        float zoomLevel = (float) (16 - Math.log(scale)/Math.log(2));
        return --zoomLevel;
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        CameraUpdate center = CameraUpdateFactory
                .newLatLng(latLng);
        map.moveCamera(center);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Status changed: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "enable " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "disable " + provider);
    }
}
