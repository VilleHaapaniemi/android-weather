package com.example.android_weather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private String units;
    private boolean IsGPSLocationEnabled = true;
    private String cityNameInput;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        getIntentData(intent);
        if (IsGPSLocationEnabled) {
            startGPS(); // Fetch weather using GPS latitude and longitude
        } else {
            getWeatherData(); // Fetch weather using user given city name
        }
    }

    private void getIntentData(Intent intent) {
        String receivedUnit = intent.getStringExtra("UNITS");
        if (receivedUnit == null) {
            receivedUnit = "metric";
        }
        units = receivedUnit;

        IsGPSLocationEnabled = intent.getBooleanExtra("GPS_LOCATION_ENABLED", true);
        if (!IsGPSLocationEnabled) {
            cityNameInput = intent.getStringExtra("CITY_NAME_INPUT");
        }
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
            double latitudeResult = currentLocation.getLatitude();
            double longitudeResult = currentLocation.getLongitude();

            // Round coordinates to 2 decimal precision
            DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US)); // Use always . as decimal point
            latitude = Double.parseDouble(df.format(latitudeResult));
            longitude = Double.parseDouble(df.format(longitudeResult));
            getWeatherData();
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1000, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitudeResult = location.getLatitude();
                double longitudeResult = location.getLongitude();

                // Round coordinates to 2 decimal precision
                DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US)); // Use always . as decimal point
                latitude = Double.parseDouble(df.format(latitudeResult));
                longitude = Double.parseDouble(df.format(longitudeResult));

                getWeatherData();
            }
        });
    }

    public void getWeatherData() {
        String WEATHER_URL = createAPIWeatherURL();
        StringRequest request = new StringRequest(Request.Method.GET, WEATHER_URL, response -> {
            try {
                parseWeatherJsonAndUpdateUi(response);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }, error -> {
            String errorMessageResult = new String(error.networkResponse.data);
            JSONObject errorJson;
            String errorMessage;
            try {
                errorJson = new JSONObject(errorMessageResult);
                errorMessage = errorJson.getString("message");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            });
        });
        Volley.newRequestQueue(this).add(request);
    }

    private String createAPIWeatherURL() {
        String weatherURL;
        final String APIKEY = "642fe4b57dea28178ca8da02fab014f0";
        Locale currentLocale = Locale.getDefault();
        String languageCode = currentLocale.getLanguage();

        if (IsGPSLocationEnabled) {
            weatherURL = "https://api.openweathermap.org/data/2.5/weather?" +
                    "lat=" + latitude +
                    "&lon=" + longitude +
                    "&appid=" + APIKEY +
                    "&units=" + units +
                    "&lang=" + languageCode;
        } else {
            weatherURL = "https://api.openweathermap.org/data/2.5/weather?" +
                    "q=" + cityNameInput +
                    "&appid=" + APIKEY +
                    "&units=" + units +
                    "&lang=" + languageCode;
        }
        return weatherURL;
    }

    private void parseWeatherJsonAndUpdateUi(String response) throws JSONException {
        try {
            JSONObject weatherJSON = new JSONObject(response);
            updateUIWeather(weatherJSON);
        } catch (JSONException e ) {
            throw new JSONException(e);
        }
    }

    private void updateUIWeather(JSONObject weatherJSON) throws JSONException {
        double latitude = weatherJSON.getJSONObject("coord").getDouble("lat");
        double longitude = weatherJSON.getJSONObject("coord").getDouble("lon");
        String cityName = getCityNameByCoordinates(latitude, longitude);
        TextView cityNameTextView = findViewById(R.id.cityNameTextView);
        cityNameTextView.setText(cityName);

        String neighborhoodName = weatherJSON.getString("name");
        TextView neighborhoodNameTextView = findViewById(R.id.neighborhoodNameTextView);
        neighborhoodNameTextView.setText(neighborhoodName);

        String weatherDescription = weatherJSON.getJSONArray("weather").getJSONObject(0).getString("description");
        weatherDescription = weatherDescription.substring(0, 1).toUpperCase() + weatherDescription.substring(1);
        TextView weatherDescriptionTextView = findViewById(R.id.weatherDescriptionTextView);
        weatherDescriptionTextView.setText(weatherDescription);

        String temperatureUnit = "";
        String speedUnit = "";
        if (Objects.equals(units, "metric")) {
            temperatureUnit = " C";
            speedUnit = " m/s";
        } else if (Objects.equals(units, "imperial")) {
            temperatureUnit = " F";
            speedUnit = " m/h";
        }
        double temperature = weatherJSON.getJSONObject("main").getDouble("temp");
        TextView temperatureTextView = findViewById(R.id.temperatureTextView);
        String temperatureText = String.format(temperature + temperatureUnit);
        temperatureTextView.setText(temperatureText);

        double windSpeed = weatherJSON.getJSONObject("wind").getDouble("speed");
        TextView windSpeedTextView = findViewById(R.id.windSpeedTextView);
        String windSpeedText = String.format(windSpeed + speedUnit);
        windSpeedTextView.setText(windSpeedText);

        String iconName = weatherJSON.getJSONArray("weather").getJSONObject(0).getString("icon");
        setUIWeatherIcon(iconName);
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

    private void setUIWeatherIcon(String iconName) {
        String imageUri = "https://openweathermap.org/img/wn/" + iconName +"@2x.png";
        ImageView weatherIcon = (ImageView) findViewById(R.id.weatherIconImageView);
        Picasso.get().load(imageUri).into(weatherIcon);
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("UNITS", units);
        intent.putExtra("GPS_LOCATION_ENABLED", IsGPSLocationEnabled);
        if (!IsGPSLocationEnabled) {
            intent.putExtra("CITY_NAME_INPUT", cityNameInput);
        }
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGPS();
        }
    }
}