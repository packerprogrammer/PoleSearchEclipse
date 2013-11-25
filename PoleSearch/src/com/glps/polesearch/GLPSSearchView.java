package com.glps.polesearch;

/**
 * Created by ghensley on 9/05/13.
 *
 */
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;




public class GLPSSearchView extends FragmentActivity 
	implements OnInfoWindowClickListener {

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

    	addMarkersToMap();
    	mMap.setOnInfoWindowClickListener(this);
    	mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latValue,longValue), 17));
    	
    }
    // TODO fix this
     private void addMarkersToMap() {
    // Uses a colored icon.

        
            mMap.addMarker(new MarkerOptions()
                     .position(new LatLng(latValue,longValue))
                     .title(poleString));
                     //.snippet(MainActivity.markers[i].getPole()));
         
    }

	@Override
	public void onInfoWindowClick(Marker arg0) {
		// TODO Auto-generated method stub
		double lat = arg0.getPosition().latitude; 
		double lon = arg0.getPosition().longitude;
		String dirString = "http://maps.google.com/maps?daddr=" + lat + "," + lon;
    	String geoloc = "http://maps.google.com/?q=test";// " + ypos + "," + xpos;
        Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dirString));
        // Verify it resolves
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
        boolean isIntentSafe = activities.size() > 0;
        if (isIntentSafe) {
            startActivity(mapIntent);
        }//if isIntentSafe
        
       
		
	}
}

