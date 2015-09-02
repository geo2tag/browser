package com.example.yana.map.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yana.map.MainActivity;
import com.example.yana.map.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by Yana on 02.09.2015.
 */
public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private LayoutInflater inflater;
    private Context ctx;

    public MarkerInfoWindowAdapter(Context ctx) {
        this.ctx = ctx;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {

        View view = inflater.inflate(R.layout.info_window, null);

        if (MainActivity.photoHashMap.containsKey(marker.getId())) {
            final ImageView imageView = (ImageView) view.findViewById(R.id.image);
            final String photoURI = MainActivity.photoHashMap.get(marker.getId());
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
