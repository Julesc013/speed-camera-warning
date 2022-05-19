package com.julescarboni.speedcamerawarning;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class LocationService extends Service {

    private static final int ONGOING_NOTIFICATION_ID = 1;

    Timer timer = new Timer();
    private TimerTask timerTaskGetLocation = new TimerTask() {
        @Override
        public void run() {

            // Get location!!
            Log.d("myTag", "This is my message");

        }
    };

    // Execution of service will start on calling this method
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Create foreground service notification
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        // Activate timer with location getting task
        //TimerTask timerTaskGetLocation = new timerTaskGetLocation();
        timer.scheduleAtFixedRate(timerTaskGetLocation, 0, 1000);

        // Returns the status of the program
        return START_STICKY;
    }

    @Override

    // execution of the service will
    // stop on calling this method
    public void onDestroy() {
        super.onDestroy();

        // Stop the timer and task
        timer.cancel();
        timer = null;

        // Stop the process
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void showNotification()
    {
        // If the notification supports a direct reply action, use
        // PendingIntent.FLAG_MUTABLE instead.
        Intent notificationIntent = new Intent(this, LocationService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        /*Notification notification =
                new Notification.Builder(this, getString(R.string.channel_ID))
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
                        //.setSmallIcon(R.drawable.icon)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.ticker_text))
                        .setOngoing(true)
                        .build();
        // TODO: Add icon to notification

        startForeground(ONGOING_NOTIFICATION_ID, notification); // Notification ID cannot be 0.*/


        // Create notification channel
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
                // TODO: Add icon to notification

        // Start notification
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notificationBuilder.build());
    }

    public void createChannel(NotificationManager notificationManager){
        // Create notification channel for foreground service notifications
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library

        // Build API level must be 26 or greater (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O).

        // Channel variables
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        String channel_ID = getString(R.string.channel_ID);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        // Create the channel
        NotificationChannel channelLocationService = new NotificationChannel(channel_ID, name, importance);
        channelLocationService.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        notificationManager.createNotificationChannel(channelLocationService);
    }

}