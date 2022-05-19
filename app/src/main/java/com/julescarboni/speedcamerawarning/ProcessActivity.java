package com.julescarboni.speedcamerawarning;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ProcessActivity extends AppCompatActivity {

    LocationReceiver locationReceiver = null;
    Boolean myReceiverIsRegistered = false;

    //TEMP
    TextView txtStatus = (TextView) findViewById(R.id.txtStatus);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationReceiver = new LocationReceiver();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!myReceiverIsRegistered) {
            registerReceiver(locationReceiver, new IntentFilter(LocationService.INTENT_ID));
            myReceiverIsRegistered = true;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();if (myReceiverIsRegistered) {
            unregisterReceiver(locationReceiver);
            myReceiverIsRegistered = false;
        }
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
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    // Got last known location. In some rare situations this can be null.
                    // Often will be "expired" (older than specified minimum).

                    long age = (Calendar.getInstance().getTimeInMillis() / 1000) - location.getTime();
                    if (location == null || age > LocationService.SERVICE_INTERVAL) {

                        // Last known location is expired, get a fresh location
                        // TODO: GET FRESH LOCATION

                    }

                    // Location is fresh enough to do processing on it now
                    // 2. GEOCODE ADDRESS

                    //TEMP
                    txtStatus.setText(location.getLatitude() + " " + location.getLongitude());


                    // 3. CHECK DATABASE



                    // 4. NOTIFY USER

                }
            });

    }

    public static class LocationReceiver extends BroadcastReceiver {
        // RECEIVES TIMER TRIGGERS FROM LOCATION SERVICE

        // Create new instance of the process activity
        // This instance contains all the code that we need to run each time the timer is triggered
        // I.e. it is the foreground process code
        ProcessActivity processActivity = new ProcessActivity();

        @Override
        public void onReceive(Context context, Intent intent ) {
            Log.d("LocationReceiver", "Trigger received, calling process now");
            processActivity.doProcess();
        }
    }

}
