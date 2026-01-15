package com.julescarboni.speedcamerawarning;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class LocationService extends Service {

    public static final String INTENT_ID = "com.julescarboni.speedcamerawarning.TIMER_TICK";
    private static final int ONGOING_NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    //private final Context context = getApplicationContext();
    private Timer timer = new Timer(); // Timer for the service to use
    public static final int SERVICE_INTERVAL = 5000; // TODO: Make variable/settable!
    public static final int EXPIRY_MULTIPLIER = 3; // Location data is considered expired if it is this many times older than the service interval. TODO: Make variable/settable!

    /*@Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        startService();
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // Execution of service will start on calling this method
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Create foreground service notification
        // TODO: Add icon to notification
        // TODO: Make notification text custom and resonable

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
        timer.scheduleAtFixedRate(new ProcessTrigger(), 0, SERVICE_INTERVAL);

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

    // This is what the service actually runs
    // It simply sends a signal that it is time to run the process code

    private class ProcessTrigger extends TimerTask {
        @Override
        public void run() {
            // This is the process we do every time the timer triggers
            Log.d("LocationService", "Timer triggered");
            Intent intent = new Intent();
            intent.setAction(INTENT_ID);
            //intent.putExtra("data", "null");
            sendBroadcast(intent);
        }
    }

    @Nullable
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