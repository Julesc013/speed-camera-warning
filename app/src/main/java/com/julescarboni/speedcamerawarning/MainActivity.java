package com.julescarboni.speedcamerawarning;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;

import com.julescarboni.speedcamerawarning.databinding.ActivityMainBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

@SuppressLint("UseSwitchCompatOrMaterialCode")

public class MainActivity extends AppCompatActivity {

    public FragmentCommunicator fragmentCommunicator;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.julescarboni.speedcamerawarning.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* Add in Oncreate() funtion after setContentView() */

        // Create notification channel for foreground service notifications
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            String channel_ID = getString(R.string.channel_ID);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channelLocationService = new NotificationChannel(channel_ID, name, importance);
            channelLocationService.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channelLocationService);
        }*/

        // Get context and intent required to start the location service
        Context context = getApplicationContext();
        Intent intentLocationService = new Intent(this, LocationService.class); // Build the intent for the service
        // Pass context and intent to fragment that will manage the service
        fragmentCommunicator.passContextToFragment(context);
        fragmentCommunicator.passIntentToFragment(intentLocationService);

        // Initiate elements that will be use programmatically throughout the app
        Switch switchToggleService = (Switch) findViewById(R.id.switchToggleService);
        TextView txtStatus = (TextView) findViewById(R.id.txtStatus);

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }

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
}