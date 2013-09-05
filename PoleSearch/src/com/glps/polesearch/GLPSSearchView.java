package com.glps.polesearch;

/**
 * Created by ghensley on 9/05/13.
 *
 */
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.annotation.SuppressLint;
import android.content.Intent;




public class GLPSSearchView extends FragmentActivity {

	double latValue;
	double longValue;
	String poleString;



    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        latValue = extras.getDouble("lat");
        longValue = extras.getDouble("long");
        poleString = extras.getString("poleString");
        
        setUpMapIfNeeded();
        mMap.setMyLocationEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_map_view, menu);
        return true;
    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
        // Try to obtain the map from the SupportMapFragment.
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
        .getMap();
         //Check if we were successful in obtaining the map.
         if (mMap != null) {
             setUpMap();
         }
         }
    }

    private void setUpMap() {



        //String test = MainActivity.markers[0].getPole();

        // Add lots of markers to the map.
        addMarkersToMap();


        // Pan to see all markers in view.
        // Cannot zoom to bounds until the map has a size.
        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation") // We use the new method when supported
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {
                   LatLngBounds bounds = new LatLngBounds.Builder()

                    .include(new LatLng(latValue,longValue))
                    //.include(new LatLng(MainActivity.markers[1].getCoords()[0],MainActivity.markers[1].getCoords()[1]))
                   // .include(new LatLng(MainActivity.markers[2].getCoords()[0],MainActivity.markers[2].getCoords()[1]))
                    //.include(new LatLng(MainActivity.markers[3].getCoords()[0],MainActivity.markers[3].getCoords()[1]))
                   // .include(new LatLng(MainActivity.markers[4].getCoords()[0],MainActivity.markers[4].getCoords()[1]))
                    .build();
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));

                }
            });
        }
    }
    // TODO fix this
     private void addMarkersToMap() {
    // Uses a colored icon.

        
            mMap.addMarker(new MarkerOptions()
                     .position(new LatLng(latValue,longValue))
                     .title(poleString));
                     //.snippet(MainActivity.markers[i].getPole()));
         
    }
}

