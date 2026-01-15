package com.julescarboni.speedcamerawarning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.julescarboni.speedcamerawarning.databinding.ActivityMainBinding;
import com.julescarboni.speedcamerawarning.enums.CameraZoneType;
import com.julescarboni.speedcamerawarning.enums.LocationStatus;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    // TODO: MAKE THINGS PRIVATE!!!!

    private AppBarConfiguration appBarConfiguration;

    // UI elements
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchToggleService;
    private TextView txtService;
    private TextView txtStatus;
    private TextView txtTempLocation; //TODO: REMOVE THIS TEMP

    private LocationReceiver locationReceiver = null;
    private Boolean myReceiverIsRegistered = false;

    // State tracking variables
    private LocationStatus currentLocationStatus = LocationStatus.GOOD_LOCATION;   // Initially so if location not found on first try, will warn user.
    private CameraZoneType currentZoneType = CameraZoneType.NO_CAMERAS;     // Initially so if in mobile camera zone on first try, will warn user.
    private Boolean firstTimeRun = true;    // Is this the first time the process is running (e.g. on startup)?

    // Camera location database
    private List<CameraLocation> mobileCameraLocations = new ArrayList<>();

    // Download progress dialog
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.julescarboni.speedcamerawarning.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* Add in Oncreate() funtion after setContentView() */
        // Initiate elements that will be use programmatically throughout the app
        switchToggleService = (Switch) findViewById(R.id.switchToggleService);
        txtService = (TextView) findViewById(R.id.txtService);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtTempLocation = (TextView) findViewById(R.id.txtTempLocation); //TODO: REMOVE THIS TEMP

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        /* My code */
        firstTimeRun = true; // If recreating this view, then means the app is restarting, so reset this just to be sure.

        // Check if LocationService is already running,
        // If it is, make the UI reflect that.
        // Toggle switch and set status text
        if (isServiceRunning(LocationService.class)) {
            switchToggleService.setChecked(true);
            txtService.setText(R.string.service_active);
            txtStatus.setText(R.string.status_waiting);
            txtStatus.setTextColor(getResources().getColor(R.color.status_waiting));
        } else {
            switchToggleService.setChecked(false);
            txtService.setText(R.string.service_inactive);
            txtStatus.setText(R.string.status_service_inactive);
            txtStatus.setTextColor(getResources().getColor(R.color.status_service_inactive));
        }

        locationReceiver = new LocationReceiver();
        locationReceiver.setMainActivityHandler(this);
        // Register receiver
        registerReceiver(locationReceiver, new IntentFilter(LocationService.INTENT_ID));
        myReceiverIsRegistered = true;

        switchToggleService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !isServiceRunning(LocationService.class)) {
                    // The toggle has been enabled
                    startService();
                } else if (!isChecked && isServiceRunning(LocationService.class)) {
                    // The toggle has been disabled
                    stopService();
                }
            }
        });

    }

    /*@Override
    protected void onResume() {
        super.onResume();
        if (!myReceiverIsRegistered) {
            locationReceiver.setProcessActivityHandler(this);
            registerReceiver(locationReceiver, new IntentFilter(LocationService.INTENT_ID));
            myReceiverIsRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myReceiverIsRegistered) {
            unregisterReceiver(locationReceiver);
            myReceiverIsRegistered = false;
        }
    }*/

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        // Check if the service is already running
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        /* Show dialog for download progress */
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading database. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        /* Background Async Task to download file */

        // Before starting background thread Show Progress Bar Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        // Downloading file in background thread
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                OutputStream output = new FileOutputStream(Environment
                        .getExternalStorageDirectory().toString()
                        + "/mobile_cameras_latest.xlsx");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        // Updating progress bar
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        // After completing background task Dismiss the progress dialog
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);

        }

    }

    public void startService() {
        /* Start the Location Service */

        /* Firstly update the databases of camera locations */

        // Update status indicator
        txtService.setText(R.string.service_starting);

        // Check for updates to the database
        // Only download if not up to date
        // TODO: check for database updates

        // Download database
        // TODO: download database!


        /* Secondly start the service*/

        // Get context and intent required to start the location service
        Context context = this.getApplicationContext();
        Intent intentLocationService = new Intent(this, LocationService.class);
        // Start service
        intentLocationService.putExtra("inputExtra", "Foreground Service Example in Android");
        //getActivity().startService(intentLocationService);
        ContextCompat.startForegroundService(context, intentLocationService);
        //context.startForegroundService(intentLocationService);

        // Update status indicator
        txtService.setText(R.string.service_active);
        txtStatus.setText(R.string.status_waiting);
        txtStatus.setTextColor(getResources().getColor(R.color.status_waiting));
    }

    public void stopService() {
        /* Stop the Location Service */

        // Get context and intent required to stop the location service
        Context context = this.getApplicationContext();
        // Stop service
        Intent intentLocationService = new Intent(this, LocationService.class);
        context.stopService(intentLocationService);

        // Update status indicator
        txtService.setText(R.string.service_inactive);
        txtStatus.setText(R.string.status_service_inactive);
        txtStatus.setTextColor(getResources().getColor(R.color.status_service_inactive));
    }

    private void getLocationPermissions() {
        // LOCATION PERMISSIONS NOT ADEQUATE
        // GET PERMISSIONS FROM USER
        // TODO: Consider calling ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        // TODO: Implement this!
        return;
    }

    private Location getFreshLocation(FusedLocationProviderClient fusedLocationClient) {
        // GET FRESH CURRENT LOCATION USING ANY MEANS NECESSARY
        // TODO: Might have to move this into the doProcess() function because it cannot be called here.
        // Create a new fused location client and try to get a location

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermissions();
        }
        final Location[] currentLocation = new Location[1];
        /*fusedLocationClient.getCurrentLocation().addOnSuccessListener(
                this,
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // SUCCESSFULLY QUERIED FUSED LOCATION CLIENT
                        currentLocation[0] = location;
                    }
                });*/
        return currentLocation[0];
    }

    private DoProcessReturnType doProcess() {

        // THIS IS THE CODE THAT RUNS THE MAIN PROCESS OF THE SERVICE
        /*  1.  TRY TO GET LOCATION
         *  2.  CHECK IF GOT LOCATION
         *  3.  GEOCODE ADDRESS
         *  4.  CHECK DATABASE
         *  5.  NOTIFY USER (beep/bubble) */

        /* 1. TRY TO GET LOCATION */
        // Tries to get the last known location
        // If it is out of date or invalid, it will fetch a fresh location

        // (PLAN A) FUSED LOCATION PROVIDER METHOD
        /*Location[] lastKnownLocation = new Location[1]; // For getting location OUT of a fused location provider
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            GetLocationPermissions();
        }
        // Create a new fused location client and try to get a location
        fusedLocationClient.getLastLocation().addOnSuccessListener(
                this,
                new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // SUCCESSFULLY QUERIED FUSED LOCATION CLIENT
                            lastKnownLocation[0] = location;
                        }
                });
        Location location = lastKnownLocation[0];*/

        // (PLAN B) TRADITIONAL LOCATION MANAGER METHOD
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location location = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermissions();
        }
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (location == null || l.getAccuracy() < location.getAccuracy()) {
                // Found best last known location:
                location = l;
            }
        }

        // Asked for last known location. In some rare situations this can be null.
        if (location == null) {
            // Failed to get last known location, get a fresh location
            // TODO: GET FRESH LOCATION
        } else {
            // Got last known location, now check if it is older than the timer interval (what we call "expired")
            long age = (Calendar.getInstance().getTimeInMillis() / 1000) - location.getTime();
            if (age > (LocationService.SERVICE_INTERVAL * LocationService.EXPIRY_MULTIPLIER)) {
                // Last known location is expired, get a fresh location
                // TODO: GET FRESH LOCATION
            }
        }

        /* 2. CHECK IF GOT LOCATION */
        // If couldn't get location, handle that and then fail out.
        if (location == null) {
            // COULD NOT GET LOCATION
            return new DoProcessReturnType(LocationStatus.NO_LOCATION, CameraZoneType.UNCERTAIN, new ArrayList<>());
        }
        // Handle FOUND LOCATION later, because need to see what address or camera status is before updating.
        // At this point, the location exists and is fresh enough to do processing on it


        /* 3. GEOCODE ADDRESS */

        try {
            Geocoder geocoder = new Geocoder(getApplication().getApplicationContext(), Locale.getDefault());
            // TODO: Use updated getFromLocation() because this method can block up threads
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
            if (addresses.isEmpty()) {
                // COULD NOT GET ADDRESS FROM LOCATION
                return new DoProcessReturnType(LocationStatus.UNCERTAIN_LOCATION, CameraZoneType.UNCERTAIN, addresses);
            }
            else {

                // GOT LOCATION

                // Record location details for lookup in next step
                //yourtextboxname.setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                String addressThoroughfare = addresses.get(0).getThoroughfare();
                String addressLocality = addresses.get(0).getLocality();

                // Check if it is accurate or approximate
                if (addressThoroughfare == null) {
                    // TODO: CHECK THIS CODE AND SEE WHAT PARTS OF THE ADDRESS EQUAL WHAT
                    // COULD NOT GET ACCURATE ADDRESS FROM LOCATION
                    return new DoProcessReturnType(LocationStatus.UNCERTAIN_LOCATION, CameraZoneType.UNCERTAIN, addresses);
                }


                /* 4. CHECK DATABASE */

                // Search through all databases to see if address has a camera
                // TODO: Check if has fixed or wet film cameras

                // Check if has a mobile camera
                for (CameraLocation cameraLocation : mobileCameraLocations) {
                    if (cameraLocation.isMatch(addressThoroughfare, addressLocality)) {
                        // Mobile camera certificate found
                        return new DoProcessReturnType(LocationStatus.GOOD_LOCATION, CameraZoneType.MOBILE_ONLY, addresses);
                    }
                }
                // No cameras
                return new DoProcessReturnType(LocationStatus.GOOD_LOCATION, CameraZoneType.NO_CAMERAS, addresses);

                // All cases handled and returned.
                /* DONE. PROCESSING FINISHED. */

            }
        } catch (IOException e) {
            // TODO: Handle this exception more appropriately... Why IO Exception? Can fix it?
            // COULD NOT GET ADDRESS FROM LOCATION
            return new DoProcessReturnType(LocationStatus.UNCERTAIN_LOCATION, CameraZoneType.UNCERTAIN, new ArrayList<>());
        }
    }

    public void updateStatus(MainActivity mainActivity, LocationStatus newLocationStatus, CameraZoneType newCameraZoneType) {
        /* 5. NOTIFY USER */

        if (currentLocationStatus != newLocationStatus || currentZoneType != newCameraZoneType || firstTimeRun) {
            // If something has changed...
            // Always update and warn if this is the first time run.

            int newStatusText;
            int newStatusColor;
            //int announcementToMake; // Voice-line to read out
            // TODO: ADD voice-line variables to switch case

            switch (newLocationStatus) {
                case NO_LOCATION:
                    newStatusText = R.string.status_no_location;
                    newStatusColor = getResources().getColor(R.color.status_no_location);
                    break;

                case UNCERTAIN_LOCATION:
                    newStatusText = R.string.status_uncertain_location;
                    newStatusColor = getResources().getColor(R.color.status_uncertain_location);
                    break;

                default:
                    // If no appropriate value passed, just say no location. TODO: Handle this better.
                    newStatusText = R.string.status_no_location;
                    newStatusColor = getResources().getColor(R.color.status_no_location);
                    break;
            }
            switch (newCameraZoneType) {
                case NO_CAMERAS:
                    newStatusText = R.string.status_no_cameras;
                    newStatusColor = getResources().getColor(R.color.status_no_cameras);
                    break;

                case FIXED_ONLY:
                    newStatusText = R.string.status_fixed_cameras;
                    newStatusColor = getResources().getColor(R.color.status_fixed_cameras);
                    break;

                case MOBILE_ONLY:
                    newStatusText = R.string.status_mobile_cameras;
                    newStatusColor = getResources().getColor(R.color.status_mobile_cameras);
                    break;

                case BOTH_CAMERAS:
                    newStatusText = R.string.status_both_cameras;
                    newStatusColor = getResources().getColor(R.color.status_both_cameras);
                    break;

                /*default:
                    // TODO: Handle this erroneous case
                    // No default case, because if no location, then this is irrelevant.
                    break;*/
            }

            mainActivity.txtStatus.setText(newStatusText);
            mainActivity.txtStatus.setTextColor(newStatusColor);

            // Update bubble
            // TODO: update bubble

            // Announce new status
            // TODO: announce new status

            // Update memory
            mainActivity.currentLocationStatus = newLocationStatus;
            mainActivity.currentZoneType = newCameraZoneType;
        }

    }

    public static class LocationReceiver extends BroadcastReceiver {
        // RECEIVES TIMER TRIGGERS FROM LOCATION SERVICE

        private MainActivity mainActivity = null;

        public void setMainActivityHandler(MainActivity mainActivity){
            this.mainActivity = mainActivity;
        }

        @Override
        public void onReceive(Context context, Intent intent ) {
            Log.d("LocationReceiver", "Trigger received, calling process now");

            // Do processing... Check what the resultant status of our current location is.
            DoProcessReturnType result = mainActivity.doProcess();

            //TODO: TEMP DEBUG REMOVE THIS CODE
            List<Address> addresses = result.getAddresses();
            if (addresses.size() != 0) {
                String tempAddress = addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                mainActivity.txtTempLocation.setText(tempAddress);
            }

            // Use status to update indicators...
            // Text views, Bubble, and Audio alert
            mainActivity.updateStatus(mainActivity, result.getLocationStatus(), result.getCameraZoneType());
        }
    }

    public static class DoProcessReturnType {
        // CLASS USED TO RETURN STATUSES FROM DO PROCESS FUNCTION

        private final LocationStatus locationStatus;
        private final CameraZoneType cameraZoneType;
        private final List<Address> addresses;

        public DoProcessReturnType(LocationStatus locationStatus, CameraZoneType cameraZoneType, List<Address> addresses) {
            this.locationStatus = locationStatus;
            this.cameraZoneType = cameraZoneType;
            this.addresses = addresses;
        }
        public LocationStatus getLocationStatus(){
            return this.locationStatus;
        }
        public CameraZoneType getCameraZoneType(){
            return this.cameraZoneType;
        }
        public List<Address> getAddresses(){
            return this.addresses;
        }
    }

    public static class CameraLocation {
        // CLASS USED TO STORE EACH CAMERA LOCATION

        private final String roadName;
        private final String suburbName;

        public CameraLocation(String roadName, String suburbName) {
            this.roadName = roadName;
            this.suburbName = suburbName;
        }
        public Boolean isMatch(String thisRoadName, String thisSuburbName){
            return Objects.equals(roadName.toLowerCase(), thisRoadName.toLowerCase())
                    && Objects.equals(suburbName.toLowerCase(), thisSuburbName.toLowerCase());
        }
    }

}