package com.julescarboni.speedcamerawarning;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.julescarboni.speedcamerawarning.databinding.FragmentFirstBinding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FirstFragment extends Fragment {

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

                // Check for updates to the database

                // Check if the service is running
                // If not, start a new instance of the service and create a bubble
                // If yes, stop the service instance and remove the bubble



                if (isChecked) {
                    // The toggle is enabled

                    // Update status indicator
                    binding.txtStatus.setText("Status: ACTIVE!");
                } else {
                    // The toggle is disabled

                    // Update status indicator
                    binding.txtStatus.setText("Status: Inactive");
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