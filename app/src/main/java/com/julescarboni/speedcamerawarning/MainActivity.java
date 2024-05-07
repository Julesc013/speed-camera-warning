package com.julescarboni.speedcamerawarning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.julescarboni.speedcamerawarning.databinding.ActivityMainBinding;
import com.julescarboni.speedcamerawarning.enums.CameraZoneType;
import com.julescarboni.speedcamerawarning.enums.LocationStatus;

import java.io.IOException;
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

    private AppBarConfiguration appBarConfiguration;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchToggleService;
    private TextView txtService;
    private TextView txtStatus;

//    private LocationReceiver locationReceiver = null;
//    private Boolean myReceiverIsRegistered = false;

    private LocationStatus locationStatus = LocationStatus.GOOD_LOCATION;   // Initially so if location not found on first try, will warn user.
    private CameraZoneType currentZoneType = CameraZoneType.NO_CAMERAS;     // Initially so if in mobile camera zone on first try, will warn user.
    private Boolean firstTimeRun = true;    // Is this the first time the process is running (e.g. on startup)?

    private List<CameraLocation> mobileCameraLocations;
    //private List<CameraLocation> fixedCameraLocations;
    //private List<CameraLocation> wetFilmCameraLocations;
    //private List<CameraLocation> phoneCameraLocations;

    // Define FusedLocationProviderClient
    private FusedLocationProviderClient fusedLocationClient;

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

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        /* My code */
        firstTimeRun = true; // If recreating this view, then means the app is restarting, so reset this just to be sure.

//        // Check if LocationService is already running,
//        // If it is, make the UI reflect that.
//        // Toggle switch and set status text
//        if (isServiceRunning(LocationService.class)) {
//            switchToggleService.setChecked(true);
//            txtService.setText(R.string.service_active);
//            txtStatus.setText(R.string.status_waiting);
//            txtStatus.setTextColor(getResources().getColor(R.color.status_waiting));
//        } else {
//            switchToggleService.setChecked(false);
//            txtService.setText(R.string.service_inactive);
//            txtStatus.setText(R.string.status_service_inactive);
//            txtStatus.setTextColor(getResources().getColor(R.color.status_service_inactive));
//        }
        // ASSUME FUSED LOCATION SERVICES ARE NOT RUNNING YET
        switchToggleService.setChecked(false);
        txtService.setText(R.string.service_inactive);
        txtStatus.setText(R.string.status_service_inactive);
        txtStatus.setTextColor(getResources().getColor(R.color.status_service_inactive));

//        locationReceiver = new LocationReceiver();
//        locationReceiver.setMainActivityHandler(this);
//        // Register receiver
//        registerReceiver(locationReceiver, new IntentFilter(LocationService.INTENT_ID));
//        myReceiverIsRegistered = true;
//
//        switchToggleService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked && !isServiceRunning(LocationService.class)) {
//                    startService(); // The toggle has been enabled
//                } else if (!isChecked && isServiceRunning(LocationService.class)) {
//                    stopService(); // The toggle has been disabled
//                }
//            }
//        });

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Start location updates
        startLocationUpdates();

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
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void startService() {
        /* Start the Location Service */

        // Update status indicator
        txtService.setText(R.string.service_starting);

        // Check for updates to the database
        // TODO: check for database updates

        // Download database
        // TODO: download database!

//        // Get context and intent required to start the location service
//        Context context = this.getApplicationContext();
//        Intent intentLocationService = new Intent(this, LocationService.class);
//        // Start service
//        intentLocationService.putExtra("inputExtra", "Foreground Service Example in Android");
//        //getActivity().startService(intentLocationService);
//        ContextCompat.startForegroundService(context, intentLocationService);
//        //context.startForegroundService(intentLocationService);

        // Update status indicator
        txtService.setText(R.string.service_active);
        txtStatus.setText(R.string.status_waiting);
        txtStatus.setTextColor(getResources().getColor(R.color.status_waiting));
    }

    public void stopService() {
        /* Stop the Location Service */

//        // Get context and intent required to stop the location service
//        Context context = this.getApplicationContext();
//        // Stop service
//        Intent intentLocationService = new Intent(this, LocationService.class);
//        context.stopService(intentLocationService);

        // Update status indicator
        txtService.setText(R.string.service_inactive);
        txtStatus.setText(R.string.status_service_inactive);
        txtStatus.setTextColor(getResources().getColor(R.color.status_service_inactive));
    }

    private void startLocationUpdates() {
        // Create location request
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(LocationService.SERVICE_INTERVAL) // Interval in milliseconds (adjust as needed)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            return;
                        }
                        for (Location location : locationResult.getLocations()) {
                            // Handle location updates here
                            processLocation(location);
                        }
                    }
                },
                null); // Looper can be null for main thread
    }

    private void processLocation(Location location) {
        // Process location updates here
        // For example, update UI or perform any necessary tasks
        Log.d("Location Update", "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
        doProcess(location);
    }

    private void doProcess(Location location) {

        // THIS IS THE CODE THAT RUNS THE MAIN PROCESS OF THE SERVICE
        /*  1.  TRY TO GET LOCATION
         *  2.  CHECK IF GOT LOCATION
         *  3.  GEOCODE ADDRESS
         *  4.  CHECK DATABASE
         *  5.  NOTIFY USER (beep/bubble) */

//        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        /* 1. TRY TO GET LOCATION */
        // Tries to get the last known location
        // If it is out of date or invalid, it will fetch a fresh location

//        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // LOCATION PERMISSIONS NOT ADEQUATE
//            // GET PERMISSIONS FROM USER
//
//            // TODO: Consider calling ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//
//        }
//
//        // Create a new fused location client and try to get a location
//        fusedLocationClient.getLastLocation().addOnSuccessListener(
//                this,
//                new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            // SUCCESSFULLY QUERIED FUSED LOCATION CLIENT
//
//                            // Asked for last known location. In some rare situations this can be null.
//                            if (location == null) {
//                                // Failed to get last known location, get a fresh location
//                                location = getFreshLocation();
//                            } else {
//                                // Got last known location, now check if it is older than the timer interval (what we call "expired")
//                                long age = (Calendar.getInstance().getTimeInMillis() / 1000) - location.getTime();
//                                if (age > LocationService.SERVICE_INTERVAL) {
//                                    // Last known location is expired, get a fresh location
//                                    location = getFreshLocation();
//                                }
//                            }
//
//                            // Code moved to outside this block, just below here.
//
//                        }
//                });


        /* 2. CHECK IF GOT LOCATION */
        // If couldn't get location, handle that and then fail out.
        if (location == null) {
            // COULD NOT GET LOCATION
            updateStatus(LocationStatus.NO_LOCATION, CameraZoneType.UNCERTAIN);
            return;
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
                updateStatus(LocationStatus.UNCERTAIN_LOCATION, CameraZoneType.UNCERTAIN);
                return;
            }
            else {

                // GOT LOCATION

                // Record location details for lookup in next step
                //yourtextboxname.setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                String addressFeatureName = addresses.get(0).getFeatureName();
                String addressLocality = addresses.get(0).getLocality();

                // Check if it is accurate or approximate
                if (addressFeatureName == null) {
                    // TODO: CHECK THIS CODE AND SEE WHAT PARTS OF THE ADDRESS EQUAL WHAT
                    // COULD NOT GET ACCURATE ADDRESS FROM LOCATION
                    updateStatus(LocationStatus.UNCERTAIN_LOCATION, CameraZoneType.UNCERTAIN);
                    return;
                }


                /* 4. CHECK DATABASE */

                // Search through all databases to see if address has a camera
                // TODO: Check if has fixed, phone, or wet film cameras

                // Check if has a mobile camera
                for (CameraLocation cameraLocation : mobileCameraLocations) {
                    if (cameraLocation.isMatch(addressFeatureName, addressLocality)) {
                        // Mobile camera certificate found
                        updateStatus(LocationStatus.GOOD_LOCATION, CameraZoneType.MOBILE_ONLY);
                        return;
                    }
                }
                // No cameras
                updateStatus(LocationStatus.GOOD_LOCATION, CameraZoneType.NO_CAMERAS);
                return;

                // All cases handled and returned.
                /* DONE. PROCESSING FINISHED. */

            }
        } catch (IOException e) {
            // TODO: Handle this exception more appropriately... Why IO Exception? Can fix it?
            // COULD NOT GET ADDRESS FROM LOCATION
            updateStatus(LocationStatus.UNCERTAIN_LOCATION, CameraZoneType.UNCERTAIN);
            return;
        }

    }

    public void updateStatus(LocationStatus newLocationStatus, CameraZoneType newCameraZoneType) {
        /* 5. NOTIFY USER */

        if (locationStatus != newLocationStatus || currentZoneType != newCameraZoneType || firstTimeRun) {
            // If something has changed...
            // Always update and warn if this is the first time run.

            int newStatusText;
            int newStatusColor;
            //int announcementToMake; // Voice-line to read out
            // TODO: ADD voice-line variables to switch case

            if (Objects.requireNonNull(newLocationStatus) == LocationStatus.UNCERTAIN_LOCATION) {
                newStatusText = R.string.status_uncertain_location;
                newStatusColor = getResources().getColor(R.color.status_uncertain_location);
            } else {
                // If no appropriate value passed, just say no location. TODO: Handle this better.
                newStatusText = R.string.status_no_location;
                newStatusColor = getResources().getColor(R.color.status_no_location);
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

            txtStatus.setText(newStatusText);
            txtStatus.setTextColor(newStatusColor);

            // Update bubble
            // TODO: update bubble

            // Announce new status
            // TODO: announce new status

            // Update memory
            locationStatus = newLocationStatus;
            currentZoneType = newCameraZoneType;
        }

    }

//    public Location getFreshLocation() {
//        // Get a fresh location from a fused location service
//
//        // TODO: GET FRESH LOCATION
//        // Can pass fused service as input to this function?
//
//        return new Location("null");
//
//    }

//    public static class LocationReceiver extends BroadcastReceiver {
//        // RECEIVES TIMER TRIGGERS FROM LOCATION SERVICE
//
//        private MainActivity mainActivity = null;
//
//        public void setMainActivityHandler(MainActivity mainActivity){
//            this.mainActivity = mainActivity;
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent ) {
//            Log.d("LocationReceiver", "Trigger received, calling process now");
//            mainActivity.doProcess();
//        }
//    }

    public class CameraLocation {
        // CLASS USED TO STORE EACH CAMERA LOCATION

        private final String roadName;
        private final String suburbName;

        public CameraLocation(String newRoadName, String newSuburbName) {
            this.roadName = newRoadName;
            this.suburbName = newSuburbName;
        }
        public Boolean isMatch(String thisRoadName, String thisSuburbName){
            return Objects.equals(roadName, thisRoadName) && Objects.equals(suburbName, thisSuburbName);
        }
    }

}