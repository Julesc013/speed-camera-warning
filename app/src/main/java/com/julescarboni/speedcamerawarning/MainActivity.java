package com.julescarboni.speedcamerawarning;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.julescarboni.speedcamerawarning.databinding.ActivityMainBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchToggleService;
    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.julescarboni.speedcamerawarning.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /* Add in Oncreate() funtion after setContentView() */
        // Initiate elements that will be use programmatically throughout the app
        this.switchToggleService = (Switch) findViewById(R.id.switchToggleService);
        this.txtStatus = (TextView) findViewById(R.id.txtStatus);

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Check if LocationService is already running,
        // If it is, make the UI reflect that.
        if (isServiceRunning(LocationService.class)) {
            // Toggle switch and set status text
            switchToggleService.setChecked(true);
            txtStatus.setText(R.string.status_active);
        }

        /*// Get access to shared preferences
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        final SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();*/

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

        // Check for updates to the database

        // Download database

        // Get context and intent required to start the location service
        Context context = this.getApplicationContext();
        Intent intentLocationService = new Intent(this, LocationService.class);
        // Start service
        intentLocationService.putExtra("inputExtra", "Foreground Service Example in Android");
        //getActivity().startService(intentLocationService);
        ContextCompat.startForegroundService(context, intentLocationService);
        //context.startForegroundService(intentLocationService);

        // Update status indicator
        txtStatus.setText(R.string.status_active);

        /*// Make the app remember that the service is active
        sharedPreferencesEditor.putBoolean("service_active", true);
        sharedPreferencesEditor.apply();*/
    }

    public void stopService() {
        /* Stop the Location Service */

        // Get context and intent required to stop the location service
        Context context = this.getApplicationContext();
        // Stop service
        Intent intentLocationService = new Intent(this, LocationService.class);
        context.stopService(intentLocationService);

        // Update status indicator
        txtStatus.setText(R.string.status_inactive);

        /*// Make the app remember that the service is inactive
        sharedPreferencesEditor.putBoolean("service_active", false);
        sharedPreferencesEditor.apply();*/
    }

}