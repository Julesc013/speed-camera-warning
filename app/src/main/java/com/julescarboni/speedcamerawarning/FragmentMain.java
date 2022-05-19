package com.julescarboni.speedcamerawarning;

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

        binding.switchToggleService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled

                    // Check for updates to the database

                    // Download database

                    // Get context and intent required to start the location service
                    Context context = getActivity().getApplicationContext();
                    Intent intentLocationService = new Intent(getActivity(), LocationService.class); // Build the intent for the service
                    // Start service
                    context.startForegroundService(intentLocationService);

                    // Create bubble

                    // Update status indicator
                    binding.txtStatus.setText(R.string.status_active);

                } else {
                    // The toggle is disabled

                    // Remove bubble

                    // Stop service

                    // Update status indicator
                    binding.txtStatus.setText(R.string.status_inactive);

                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}