package com.example.android_weather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private String units;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        units = intent.getStringExtra("UNITS");
        setCurrentUnitRadioGroup();

        addUnitsRadioGroupListener();
    }

    public void setCurrentUnitRadioGroup() {
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
    }

    public void addUnitsRadioGroupListener() {
        RadioGroup radioGroup = findViewById(R.id.unitsRadioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.metricsRadioButton) {
                units = "metric";
            } else if (checkedId == R.id.imperialRadioButton) {
                units = "imperial";
            }
        });
    }

    public void saveSettings(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("UNITS", units);
        startActivity(intent);
    }
}