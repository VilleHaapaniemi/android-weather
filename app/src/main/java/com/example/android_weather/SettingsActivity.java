package com.example.android_weather;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private String units;
    private boolean IsGPSLocationEnabled = true;
    private String cityNameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        units = intent.getStringExtra("UNITS");
        IsGPSLocationEnabled = intent.getBooleanExtra("GPS_LOCATION_ENABLED", true);
        if (!IsGPSLocationEnabled) {
            cityNameInput = intent.getStringExtra("CITY_NAME_INPUT");
        }

        setCurrentUnitRadioGroup();
        //setUnitsRadioGroupListener();

        initializeGPSLocationSwitch();
    }

    private void setCurrentUnitRadioGroup() {
        int currentUnitButtonId = 0;
        if (Objects.equals(units, "metric")) {
            currentUnitButtonId = R.id.metricsRadioButton;
        } else if (Objects.equals(units, "imperial")) {
            currentUnitButtonId = R.id.imperialRadioButton;
        }

        RadioGroup unitsRadioGroup = findViewById(R.id.unitsRadioGroup);
        if (currentUnitButtonId != 0) {
            unitsRadioGroup.check(currentUnitButtonId);
        }

        setUnitsRadioGroupListener(unitsRadioGroup);
    }

    private void setUnitsRadioGroupListener(RadioGroup radioGroup) {
        radioGroup = findViewById(R.id.unitsRadioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.metricsRadioButton) {
                units = "metric";
            } else if (checkedId == R.id.imperialRadioButton) {
                units = "imperial";
            }
        });
    }

    private void initializeGPSLocationSwitch() {
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch useGPSLocationSwitch = findViewById(R.id.useGPSLocationSwitch);
        useGPSLocationSwitch.setChecked(IsGPSLocationEnabled);
        setCityInputLayoutVisibility(IsGPSLocationEnabled);
        listenGPSLocationSwitchState(useGPSLocationSwitch);
    }

    private void listenGPSLocationSwitchState(@SuppressLint("UseSwitchCompatOrMaterialCode") Switch useGPSLocationSwitch) {
        useGPSLocationSwitch = findViewById(R.id.useGPSLocationSwitch);
        useGPSLocationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setCityInputLayoutVisibility(isChecked);
        });
    }

    private void setCityInputLayoutVisibility(boolean isChecked) {
        TextView cityInputTextView = findViewById(R.id.cityInputTextView);
        TextInputLayout cityTextInputLayout = findViewById(R.id.cityTextInputLayout);
        if (!isChecked) {
            IsGPSLocationEnabled = false;
            cityInputTextView.setVisibility(View.VISIBLE);
            cityTextInputLayout.setVisibility(View.VISIBLE);
            if (cityNameInput != null) {
                TextInputEditText cityNameInputTextEdit = findViewById(R.id.cityNameInputTextEdit);
                cityNameInputTextEdit.setText(cityNameInput);
            }
        } else {
            IsGPSLocationEnabled = true;
            cityInputTextView.setVisibility(View.GONE);
            cityTextInputLayout.setVisibility(View.GONE);
        }
    }

    private void setCityNameInput() {
        TextInputEditText cityNameInputEditText = findViewById(R.id.cityNameInputTextEdit);
        String cityName = String.valueOf(cityNameInputEditText.getText());
        cityName = cityName.toLowerCase();
        cityName = cityName.replaceAll("\\s", ""); // Removes whitespaces
        cityNameInput = cityName;
    }

    public void saveSettings(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("UNITS", units);
        intent.putExtra("GPS_LOCATION_ENABLED", IsGPSLocationEnabled);
        if (!IsGPSLocationEnabled) {
            setCityNameInput();
            intent.putExtra("CITY_NAME_INPUT", cityNameInput);
        }
        startActivity(intent);
    }
}