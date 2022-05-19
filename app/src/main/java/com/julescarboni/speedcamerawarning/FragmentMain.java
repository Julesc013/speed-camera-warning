package com.julescarboni.speedcamerawarning;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.julescarboni.speedcamerawarning.databinding.FragmentFirstBinding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FragmentMain extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if LocationService is already running,
        // If it is, make the UI reflect that.
        if (isServiceRunning(LocationService.class)) {
            // Toggle switch and set status text
            binding.switchToggleService.setChecked(true);
            binding.txtStatus.setText(R.string.status_active);
        }

        /*// Get access to shared preferences
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        final SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();*/

        binding.switchToggleService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !isServiceRunning(LocationService.class)) {
                    // The toggle has been enabled

                    // Check for updates to the database

                    // Download database

                    // Get context and intent required to start the location service
                    Context context = getActivity().getApplicationContext();
                    Intent intentLocationService = new Intent(getActivity(), LocationService.class); // Build the intent for the service
                    // Start service
                    context.startForegroundService(intentLocationService);

                    // Update status indicator
                    binding.txtStatus.setText(R.string.status_active);

                    /*// Make the app remember that the service is active
                    sharedPreferencesEditor.putBoolean("service_active", true);
                    sharedPreferencesEditor.apply();*/

                } else if (!isChecked && isServiceRunning(LocationService.class)) {
                    // The toggle has been disabled

                    // Stop service

                    // Update status indicator
                    binding.txtStatus.setText(R.string.status_inactive);

                    /*// Make the app remember that the service is inactive
                    sharedPreferencesEditor.putBoolean("service_active", false);
                    sharedPreferencesEditor.apply();*/

                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        // Check if the service is already running
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}