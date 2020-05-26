package com.project.reminder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

import com.project.reminder.R;

public class SettingsFragment extends Fragment {

    Switch nightModeSwitch;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        //get night mode status from shared preferences
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean nightMode = mPrefs.getBoolean("NIGHT_MODE", true);

        nightModeSwitch = view.findViewById(R.id.switch_night_mode);

        //set form element
        if (nightMode) {
            nightModeSwitch.setChecked(true);
        } else {
            nightModeSwitch.setChecked(false);
        }

        //set listener
        nightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on){
                if(on)
                {
                    //save selection
                    ((DashBoardActivity)getActivity()).setNightMode(true);
                }
                else
                {
                    //save selection
                    ((DashBoardActivity)getActivity()).setNightMode(false);
                }
            }
        });

        return view;
    }
}