package com.julescarboni.speedcamerawarning;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class LocationService extends Service {

    private static final int ONGOING_NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private FusedLocationProviderClient fusedLocationClient;
    private final Context serviceContext = this;
    Timer timer = new Timer(); // Timer for the service to use
    private final int SERVICE_INTERVAL = 1000; // TODO: Set to 10 seconds

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    // Execution of service will start on calling this method
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Create foreground service notification
        // TODO: Add icon to notification

        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        // Activate timer with location getting task
        timer.scheduleAtFixedRate(timerTaskGetLocation, 0, SERVICE_INTERVAL);

        // Return status of the service
        return START_NOT_STICKY;
    }

    @Override
    // Execution of the service will stop on calling this method
    public void onDestroy() {
        super.onDestroy();

        // Stop the timer and task
        timer.cancel();
        timer = null;

        // Stop the process
        stopForeground(true);
    }

    // This is what the service actually does!!
    private TimerTask timerTaskGetLocation = new TimerTask() {
        @Override
        public void run() {
            // DO THE LOCATION SERVICE PROCESS
            /*  1.  Get location
             *   2.  Geocode address
             *   3.  Check if in database
             *   4.  If so, beep and update bubble color */

            Log.d("LocationService", "Getting location and checking if in database now!");

            // 1. Get location
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
                    .addOnSuccessListener((Activity) serviceContext, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            // Got last known location. In some rare situations this can be null.
                            // Often will be "expired" (older than specified minimum).
                            long age = (Calendar.getInstance().getTimeInMillis() / 1000) - location.getTime();
                            if (location == null || age > SERVICE_INTERVAL) {
                                // Last known location is expired, get a fresh location
                            }

                            // Location is fresh enough, do processing on it
                            // 2. Geocode address

                        }
                    });

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        // Create notification channel for foreground service notifications
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        // Build API level must be 26 or greater (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}