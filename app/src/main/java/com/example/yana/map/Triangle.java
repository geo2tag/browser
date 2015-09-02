package com.example.yana.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
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

    public static void drawTriangle(Context ctx, Location myLocation, GoogleMap map) {
        int radiusM = Util.getRadius(ctx) * MainActivity.METRES_IN_KILOMETRES;
        double latitude = myLocation.getLatitude();
        double longitude = myLocation.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        int d = metersToEquatorPixels(latLng, radiusM * 2, map);
        bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bm);
        Paint p = new Paint();
        p.setColor(0x44ff0000);
        android.graphics.Point point1 = map.getProjection().toScreenLocation(latLng);
        Path path = getPath(point1, d / 2);
        c.drawPath(path, p);

        BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);
        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions().
                image(bmD).
                position(latLng, radiusM * 2, radiusM * 2).
                transparency(0.4f);
        groundOverlay = map.addGroundOverlay(groundOverlayOptions);
    }

    private static android.graphics.Path getPath(android.graphics.Point point1, int height) {
        point1.set(bm.getWidth() / 2, bm.getHeight() / 2);
        android.graphics.Point point2 = new android.graphics.Point(bm.getWidth() / 2 + height, 0);
        android.graphics.Point point3 = new android.graphics.Point(bm.getWidth() / 2 - height, 0);

        Path path = new Path();
        path.moveTo(point1.x, point1.y);
        path.lineTo(point2.x, point2.y);
        path.lineTo(point3.x, point3.y);
        path.close();
        return path;
    }

    public static void updateTriangle(float value) {
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
}
