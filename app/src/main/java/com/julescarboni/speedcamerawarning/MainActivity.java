package com.julescarboni.speedcamerawarning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.julescarboni.speedcamerawarning.databinding.ActivityMainBinding;

import java.util.Calendar;

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

    private LocationReceiver locationReceiver = null;
    private Boolean myReceiverIsRegistered = false;

    private Boolean locationAvailable = true;   // Initially true, so if location not found on first try, will warn user.
    private Boolean inSpeedCameraZone = false;  // Initially false, so if in mobile camera zone on first try, will warn user.
    private Boolean firstTimeRun = true;        // Is this the first time the process is running (e.g. on startup)?


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
                    startService(); // The toggle has been enabled
                } else if (!isChecked && isServiceRunning(LocationService.class)) {
                    stopService(); // The toggle has been disabled
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

    public void startService() {
        /* Start the Location Service */

        // Update status indicator
        txtService.setText(R.string.service_starting);

        // Check for updates to the database
        // TODO: check for database updates

        // Download database
        // TODO: download database!

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

    private void doProcess() {

        // THIS IS THE CODE THAT RUNS THE MAIN PROCESS OF THE SERVICE
        /*  1.  GET LOCATION
         *  2.  GEOCODE ADDRESS
         *  3.  CHECK DATABASE
         *  4.  NOTIFY USER (beep/bubble) */

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 1. GET LOCATION
        // Tries to get the last known location
        // If it is out of date or invalid, it will fetch a fresh location

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(
                this,
                new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            // Asked for last known location. In some rare situations this can be null.
                            if (location == null) {
                                // Failed to get last known location, get a fresh location
                                location = getFreshLocation();
                            } else {
                                // Got last known location, now check if it is older than the timer interval (what we call "expired")
                                long age = (Calendar.getInstance().getTimeInMillis() / 1000) - location.getTime();
                                if (age > LocationService.SERVICE_INTERVAL) {
                                    // Last known location is expired, get a fresh location
                                    location = getFreshLocation();
                                }
                            }

                            // If couldn't get location, handle that and then fail out.
                            if (location == null) {
                                // COULD NOT GET LOCATION
                                if (locationAvailable || firstTimeRun) { // If location was previously available...
                                    locationAvailable = false;
                                    txtStatus.setText(R.string.status_no_location);
                                    txtStatus.setTextColor(getResources().getColor(R.color.status_no_location));

                                    // Update bubble (grey)

                                    // TODO: announce that we lost location
                                    //       "Location lost"
                                }
                                return;
                            } else {
                                // FOUND LOCATION
                                if (!locationAvailable) { // If location was previously unavailable...
                                    locationAvailable = true;
                                    txtStatus.setText(R.string.status_waiting);
                                    txtStatus.setTextColor(getResources().getColor(R.color.status_waiting));

                                    // Update bubble (yellow)

                                    // TODO: announce that we found location again
                                    //       "Location locked on"
                                }
                                // Continue processing...
                            }

                            // At this point, the location exists and is fresh enough to do processing on it
                            // 2. GEOCODE ADDRESS

                            //ACCURACY OF ADDRESS (YELLOW BUBBLE) (WARNING NOT ACCURATE)

                            // 3. CHECK DATABASE



                            // 4. NOTIFY USER

                        }
                });

    }

    public Location getFreshLocation() {
        // Get a fresh location from a fused location service

        // TODO: GET FRESH LOCATION
        // Can pass fused service as input to this function?

        return new Location("null");

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
            mainActivity.doProcess();
        }
    }

}