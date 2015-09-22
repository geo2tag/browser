package com.example.yana.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Location;

import com.example.yana.map.util.Util;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Yana on 24.08.2015.
 */
public class Triangle {
    static GroundOverlay groundOverlay;
    private static Bitmap bm;
    private static GoogleMap mMap;

    Triangle(GoogleMap map, LatLng latLng, int radiusM) {
        mMap = map;
        int d = Util.getD(MainActivity.ctx);
        if(d > 0) {
            bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);

            Canvas c = new Canvas(bm);
            Paint p = new Paint();
            p.setColor(0x44ff0000);
            Point point1 = mMap.getProjection().toScreenLocation(latLng);
            Path path = getPath(point1, d / 2);
            c.drawPath(path, p);

            BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);
            GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions().
                    image(bmD).
                    position(latLng, radiusM * 2, radiusM * 2).
                    transparency(0.4f);
            groundOverlay = mMap.addGroundOverlay(groundOverlayOptions);
        }
    }

    private Path getPath(Point point1, int height) {
        point1.set(bm.getWidth() / 2, bm.getHeight() / 2);
        Point point2 = new Point(bm.getWidth() / 2 + height, 0);
        Point point3 = new Point(bm.getWidth() / 2 - height, 0);

        Path path = new Path();
        path.moveTo(point1.x, point1.y);
        path.lineTo(point2.x, point2.y);
        path.lineTo(point3.x, point3.y);
        path.close();
        return path;
    }

    public void updateTriangle(float value) {
        if (groundOverlay != null) {
            groundOverlay.setBearing(value);
        }
    }

    public static int metersToEquatorPixels(LatLng base, float meters, GoogleMap map) {
        final double OFFSET_LON = 0.5d;

        Location baseLoc = new Location("");
        baseLoc.setLatitude(base.latitude);
        baseLoc.setLongitude(base.longitude);

        Location dest = new Location("");
        dest.setLatitude(base.latitude);
        dest.setLongitude(base.longitude + OFFSET_LON);

        double degPerMeter = OFFSET_LON / baseLoc.distanceTo(dest);
        double lonDistance = meters * degPerMeter;

        Projection proj = map.getProjection();
        android.graphics.Point basePt = proj.toScreenLocation(base);
        android.graphics.Point destPt = proj.toScreenLocation(new LatLng(base.latitude, base.longitude + lonDistance));

        return Math.abs(destPt.x - basePt.x);
    }

    public LatLng[] getCoords() {

        if(groundOverlay != null) {
            LatLng[] coords = new LatLng[3];
            float mapBearing = mMap.getCameraPosition().bearing;
            LatLng position = groundOverlay.getPosition();
            double bearing = groundOverlay.getBearing();
            Point p1 = mMap.getProjection().toScreenLocation(position);
            Point northeastProj = mMap.getProjection().toScreenLocation(groundOverlay.getBounds().northeast);
            double zoomDist = Math.sqrt((p1.x - northeastProj.x) * (p1.x - northeastProj.x) +
                    (p1.y - northeastProj.y) * (p1.y - northeastProj.y));

            Point p2 = new Point((int) (p1.x - zoomDist * Math.cos(Math.toRadians(-45 - bearing + mapBearing))),
                    (int) (p1.y + zoomDist * Math.sin(Math.toRadians(-45 - bearing + mapBearing))));
            Point p3 = new Point((int) (p1.x - zoomDist * Math.cos(Math.toRadians(-135 - bearing + mapBearing))),
                    (int) (p1.y + zoomDist * Math.sin(Math.toRadians(-135 - bearing + mapBearing))));

            LatLng latLngP2 = mMap.getProjection().fromScreenLocation(p2);
            LatLng latLngP3 = mMap.getProjection().fromScreenLocation(p3);

            coords[0] = position;
            coords[1] = latLngP2;
            coords[2] = latLngP3;
            return coords;
        }
        return null;
    }
}