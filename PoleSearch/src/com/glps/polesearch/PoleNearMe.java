package com.glps.polesearch;

/**
 * Created by ghensley on 5/22/13.
 */


import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class PoleNearMe extends Activity implements LocationListener {
    public static GlpsMarker[] markers;
    private List<String> listItems;
    private TextView latitudeField;
    private TextView longitudeField;
    private static final int TEXT_VIEW_CNT = 5;
    private TextView[] textViewArray = new TextView[TEXT_VIEW_CNT];
    private LocationManager locationManager;
    private String provider;
    final PoleDatabase datasource = new PoleDatabase(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pole_near_me);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        latitudeField = (TextView) findViewById(R.id.TextView02);
        longitudeField = (TextView) findViewById(R.id.TextView04);

        textViewArray[0] = (TextView) findViewById(R.id.TextView05);
        textViewArray[1] = (TextView) findViewById(R.id.TextView06);
        textViewArray[2] = (TextView) findViewById(R.id.TextView07);
        textViewArray[3] = (TextView) findViewById(R.id.TextView08);
        textViewArray[4] = (TextView) findViewById(R.id.TextView09);

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use
        // default
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            latitudeField.setText("Location not available");
            longitudeField.setText("Location not available");

        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
        }

        else
        {
            showGPSDisabledAlertToUser();
        }
    }

    private void showGPSDisabledAlertToUser()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.cancel();
            }
        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();


    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }
    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_pole_near_me, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        float lat = (float) (location.getLatitude());
        float lng = (float) (location.getLongitude());
        latitudeField.setText(String.valueOf(lat));
        longitudeField.setText(String.valueOf(lng));


        listItems = datasource.GetNearby(lat, lng);
        if (listItems == null) {
            for (int i = 0; i < 5; i++){
                textViewArray[i].setText("");
            }
            Toast.makeText(getApplicationContext(), "No Record Found", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            for (int i = 0; i < listItems.size() && i < 5; i++){
                textViewArray[i].setText(listItems.get(i));
            }

        }



    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Toast.makeText(this, "Disabled provider " + provider,
        //Toast.LENGTH_SHORT).show();

    }
    public static Float GetDistance(float x, float y, float lng, float lat) {
        float a = x - lng;
        float b = y - lat;
        return (float) Math.sqrt((a * a) + (b * b));
    }
    //Button Clicks
    public void ShowMapClick (View view){
        String[] sPoles = new String[5];
        //listItems = new ArrayList<String>();

        //listItems.add("012OYCOE");
        if (listItems == null)
        {
            final AlertDialog.Builder NullBuilder=new AlertDialog.Builder(this);
            NullBuilder
                    .setTitle("No Records")
                    .setMessage("There are no poles to display on the map.")
                    .setCancelable(false)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.cancel();
                        }
                    });
            AlertDialog NullAlert = NullBuilder.create();
            NullAlert.show();
            return;
        }

        for (int i = 0; i < listItems.size() && i < 5; i++){
            sPoles[i] = (listItems.get(i));
        }

        markers = datasource.getMarkers(sPoles);
        for (int i = 0; i < 5; i++)
        {
            if (markers[i] == null)
            {
                markers[i] = new GlpsMarker();
                markers[i].setCoord(markers[0].getCoords()[0],markers[0].getCoords()[1]);
            }
        }
        Intent intent = new Intent(getApplicationContext(), com.glps.polesearch.GLPSMapView.class);
        startActivity(intent);
    }

}

