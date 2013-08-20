package com.glps.polesearch;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends FragmentActivity implements
        ActionBar.TabListener,
        LocationListener,
        GooglePlayServicesClient.OnConnectionFailedListener,
        GooglePlayServicesClient.ConnectionCallbacks {


    public static GlpsMarker[] markers;
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    boolean mUpdatesRequested = false;

    private static Handler hm;
    private static int GRID_SEARCH = 1;
    private static int POLE_SEARCH = 2;
    private int iFirstColIndex;
    private int iSecondColIndex;
    private static TextView latitudeField;
    private static TextView longitudeField;
    private static EditText updateField;
    private List<String> listItems;
    final PoleDatabase datasource = new PoleDatabase(this);
    private static final int TEXT_VIEW_CNT = 5;
    private static TextView[] textViewArray = new TextView[TEXT_VIEW_CNT];
    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);


            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        hm = new Handler()
        {
            public void handleMessage(Message m) {
                if (m.what == 0)
                {
                    long t = m.getData().getLong("time");
                    String s = "Load Done. " + t/1000 + " s";
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                }

            }
        };

        datasource.open();
        mLocationRequest = LocationRequest.create();
        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Note that location updates are off until the user turns them on
        mUpdatesRequested = false;
        mLocationClient = new LocationClient(this,this,this);

        // Open Shared Preferences
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get an editor
        mEditor = mPrefs.edit();


    }

    /*
    * Called when the Activity is no longer visible at all.
    * Stop updates and disconnect.
    */
    @Override
    public void onStop() {

        // If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();

        super.onStop();
    }
    /*
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    @Override
    public void onPause() {

        // Save the current setting for updates
        mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
        mEditor.commit();
        super.onPause();
    }
    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {

        super.onStart();

        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mLocationClient.connect();

    }
    /*
     * Called when the system detects that this Activity is now visible.
     */
    @Override
    public void onResume() {
        super.onResume();

        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
            mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

            // Otherwise, turn off location updates until requested
        } else {
            mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }

    }
    public static Handler returnHandler()
    {
        return hm;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        if (tab.getPosition() == 1) {
            mUpdatesRequested = true;

            if (servicesConnected()) {
                startPeriodicUpdates();

            }
        }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        if (tab.getPosition() == 1){
            mUpdatesRequested = false;
            // If the client is connected
            if (mLocationClient.isConnected()) {
                stopPeriodicUpdates();
            }
        }

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLocationChanged(Location location) {
      // Report to the UI that the location was updated
        //long number = Integer.parseInt(updateField.getText().toString());
        //mLocationRequest.setInterval(number);
        latitudeField.setText(Double.toString(location.getLatitude()));
        longitudeField.setText(Double.toString(location.getLongitude()));
        listItems = datasource.GetNearby((float) location.getLatitude(), (float) location.getLongitude());
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
    public void onConnectionFailed(ConnectionResult connectionResult) {
         /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment; // = null;
            if (position == 0){
                fragment = new SearchFragment();
            }
            else {
                fragment = new NearbyFragment();
            }

            //Bundle args = new Bundle();
            //args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
            //fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);

            }
            return null;
        }
    }
    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public static class SearchFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public SearchFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search, container, false);
            //TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            //dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public static class NearbyFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public NearbyFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
            //updateField = (EditText) rootView.findViewById(R.id.updateText);
            latitudeField = (TextView) rootView.findViewById(R.id.TextView02);
            longitudeField = (TextView) rootView.findViewById(R.id.TextView04);
            textViewArray[0] = (TextView) rootView.findViewById(R.id.TextView05);
            textViewArray[1] = (TextView) rootView.findViewById(R.id.TextView06);
            textViewArray[2] = (TextView) rootView.findViewById(R.id.TextView07);
            textViewArray[3] = (TextView) rootView.findViewById(R.id.TextView08);
            textViewArray[4] = (TextView) rootView.findViewById(R.id.TextView09);


            return rootView;
        }
    }

    private boolean ValidData(EditText e){
        //Check the syntax of the data entered in the search box

        //make sure the user entered at least 3 chars
        if (e.length() >= 3) {
            return true;
        }//if
        else {
            return false;
        }//else
    }//ValidData

    private int SearchType(RadioGroup r){

        int idGridPole = r.getCheckedRadioButtonId();

        if (idGridPole == -1) {
			return -1;
		} else if (idGridPole == R.id.radioGrid) {
			iFirstColIndex = 2;
			iSecondColIndex = 3;
			return GRID_SEARCH;
		} else if (idGridPole == R.id.radioPole) {
			iFirstColIndex = 3;
			iSecondColIndex = 2;
			return POLE_SEARCH;
		} else {
			return -1;
		}



    }//SearchType
    //Button Clicks
    public void SearchClick (View view){

        int iSearchType;
        String Temp;


        //create object editText and populate it with the text field edit_message
        EditText editText = (EditText) findViewById(R.id.edit_message);
        if (ValidData(editText) == false) {
            Toast.makeText(getApplicationContext(), "3 Character Minimum", Toast.LENGTH_LONG).show();
            return;
        }//if validdata

        RadioGroup RadioGroupSearchType = (RadioGroup) findViewById(R.id.radioSearchType);
        iSearchType = SearchType(RadioGroupSearchType);
        if (iSearchType == -1){
            Toast.makeText(getApplicationContext(), "Grid or Pole?", Toast.LENGTH_LONG).show();
            return;
        }//if SearchType


        String sGridLoc = editText.getText().toString();


        final Cursor PoleValue = datasource.getPole(sGridLoc, iSearchType);
        List<String> listItems = new ArrayList<String>();


        if (PoleValue == null) {
            final AlertDialog.Builder NullBuilder=new AlertDialog.Builder(this);
            long rows = datasource.getRows();
            NullBuilder
                    .setTitle("No Records")
                    .setMessage("No Records were Found. If you have recently updated the application it's possible all poles have yet to be loaded." +
                            "There are currently " + rows + " poles loaded.")
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
        if (PoleValue.moveToFirst()){
            do {
                Temp=PoleValue.getString(iFirstColIndex) + "-" + PoleValue.getString(iSecondColIndex);
                listItems.add(Temp);
            } while  (PoleValue.moveToNext());
        }//if PoleValue
        else{
            PoleValue.close();
            Toast.makeText(getApplicationContext(), "No Record Found", Toast.LENGTH_LONG).show();
            return;
        }//else


        //setContentView(R.layout.activity_main);
        final CharSequence[] PoleList = listItems.toArray(new CharSequence[listItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Pole");
        builder.setItems(PoleList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(getApplicationContext(), PoleList[item], Toast.LENGTH_SHORT).show();

                PoleValue.moveToPosition(item);
                if (!SendToGoogleMaps(PoleValue.getDouble(0),PoleValue.getDouble(1),PoleList[item].toString())){
                    Toast.makeText(getApplicationContext(), "Unable to Launch Map", Toast.LENGTH_LONG).show();
                    return;
                }

            }// onClick
        });
        AlertDialog alert = builder.create();
        alert.show();
    }//else if rowcount

    public boolean SendToGoogleMaps (double xpos, double ypos, String label){
        
        String geoloc = "geo:0,0?q=" + ypos + "," + xpos +  "(" + label + ")";
    	//String geoloc = "http://maps.google.com/?q=test";// " + ypos + "," + xpos;
        Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(geoloc));
        // Verify it resolves
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
        boolean isIntentSafe = activities.size() > 0;
        
        //mapIntent.setComponent(new ComponentName("com.google.android.apps.maps","com.google.android.maps.MapsActivity"));
        // Start an activity if it's safe
        if (isIntentSafe) {
            startActivity(mapIntent);
            return true;
        }//if isIntentSafe
        return false;
    }



    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }


    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // Log the result
                        Log.d(LocationUtils.APPTAG, getString(R.string.resolved));


                        break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));


                        break;
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.d(LocationUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));

                break;
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
        }
    }
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                this,
                LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(), LocationUtils.APPTAG);
        }
    }
    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {

        mLocationClient.requestLocationUpdates(mLocationRequest, this);

    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);

    }


    public void BtnStopClick (View view){

        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }
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
