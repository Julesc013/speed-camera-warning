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

    private static final int ONGOING_NOTIFICATION_ID = 1;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    Timer timer = new Timer(); // Timer for the service to use

    @Override
    public void onCreate() {
        super.onCreate();
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
        timer.scheduleAtFixedRate(timerTaskGetLocation, 0, 1000);

        // Return status of the service
        return START_NOT_STICKY;


        /*// Create notification channel
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        createChannel(notificationManager);

        // If the notification supports a direct reply action, use
        // PendingIntent.FLAG_MUTABLE instead.
        Intent notificationIntent = new Intent(this, LocationService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        Notification notification =
                new Notification.Builder(this, getString(R.string.channel_ID))
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
                        //.setSmallIcon(R.drawable.icon)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.ticker_text))
                        .setOngoing(true)
                        .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification); // Notification ID cannot be 0.*/


        /*// Create notification channel
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel(notificationManager);

        // Create notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.channel_ID))
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.ticker_text))
                .setOngoing(true);

        // Start notification
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notificationBuilder.build());

        // Returns the status of the program
        return START_STICKY;*/
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

            // Get location!!
            Log.d("myTag", "This is my message");

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