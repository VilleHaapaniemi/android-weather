package com.example.android_weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startGPS();
        //getWeatherData(61.50, 23.76);
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void startGPS() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (currentLocation != null) {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();

            // Round coordinates to 2 decimal precision
            DecimalFormat df = new DecimalFormat("#.##");
            latitude = Double.parseDouble(df.format(latitude));
            longitude = Double.parseDouble(df.format(longitude));

            String cityName = getCityNameByCoordinates(latitude, longitude);

            getWeatherData(latitude, longitude);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1000, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Round coordinates to 2 decimal precision
                DecimalFormat df = new DecimalFormat("#.##");
                latitude = Double.parseDouble(df.format(latitude));
                longitude = Double.parseDouble(df.format(longitude));

                String cityName = getCityNameByCoordinates(latitude, longitude);

                getWeatherData(latitude, longitude);
            }
        });
    }

    private String getCityNameByCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getWeatherData(double latitude, double longitude) {
        final String APIKEY = "642fe4b57dea28178ca8da02fab014f0";
        String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + APIKEY + "&units=metric";
        StringRequest request = new StringRequest(Request.Method.GET, WEATHER_URL, response -> {
            try {
                parseWeatherJsonAndUpdateUi(response);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }, error -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
        Volley.newRequestQueue(this).add(request);
    }

    private void parseWeatherJsonAndUpdateUi(String response) throws JSONException {
        try {
            JSONObject weatherJSON = new JSONObject(response);
            String weather = weatherJSON.getJSONArray("weather").getJSONObject(0).getString("main");
            double temperature = weatherJSON.getJSONObject("main").getDouble("temp");
            double wind = weatherJSON.getJSONObject("wind").getDouble("speed");

            TextView temperatureTextView = findViewById(R.id.temperatureTextView);
            String temperatureText = String.format(temperature + " C");
            temperatureTextView.setText(temperatureText);
        } catch (JSONException e ) {
            throw new JSONException(e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGPS();
        }
    }
}