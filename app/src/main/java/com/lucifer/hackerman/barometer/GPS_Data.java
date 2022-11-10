package com.lucifer.hackerman.barometer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;

public class GPS_Data extends AppCompatActivity {

    String gpsData = "";
    TextView dataTextView;

    int altitudeChoice = 1;
    int speedChoice = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_data);

        Intent intent = getIntent();
        altitudeChoice = intent.getIntExtra("Altitude", 1);
        speedChoice = intent.getIntExtra("Speed", 1);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, myLocationListener);
        updateWithNewLocation(l);
    }

    private LocationListener myLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    DecimalFormat decimalFormat = new DecimalFormat("###.#");
    DecimalFormat df_coordinates = new DecimalFormat("###.000000°");
    DecimalFormat machFormat = new DecimalFormat(".000");
    DecimalFormat feetFormat = new DecimalFormat("######");

    private void updateWithNewLocation(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            float accuracy = location.getAccuracy();
            float bearing = location.getBearing();
            double altitude = location.getAltitude();
            float speed = location.getSpeed();

            String measureUnitAltitude;
            String measureUnitSpeed = "";
            String altitudeStr = "";
            String accuracyStr = "";
            if (altitudeChoice != 1) {
                altitude = altitude * 3.28084;
                accuracy = (float) (accuracy * 3.28084);
                altitudeStr = feetFormat.format(altitude);
                accuracyStr = feetFormat.format(accuracy);
                measureUnitAltitude = " ft";
            } else {
                altitudeStr = decimalFormat.format(altitude);
                accuracyStr = decimalFormat.format(accuracy);
                measureUnitAltitude = " m";
            }
            String speedStr = "";
            switch (speedChoice) {
                case 1:
                    speed = (float) (speed * 3.6); // km/h
                    speedStr = decimalFormat.format(speed);
                    measureUnitSpeed = " km/h";
                    break;
                case 2:
                    speed = (float) (speed * 2.23694); // mph
                    speedStr = decimalFormat.format(speed);
                    measureUnitSpeed = " mph";
                    break;
                case 3:
                    speed = (float) (speed * 1.94384); // knots
                    speedStr = decimalFormat.format(speed);
                    measureUnitSpeed = " knots";
                    break;
                case 4:
                    speed = (float) (speed * 0.00291545); // mach
                    speedStr = machFormat.format(speed);
                    measureUnitSpeed = " mach";
                    break;
                default:
                    break;
            }

            gpsData =
                    "Lat: " + df_coordinates.format(latitude) + "\n" +
                            "Lon: " + df_coordinates.format(longitude) + "\n" +
                            "Accuracy: " + accuracyStr + measureUnitAltitude + "\n\n" +
                            "Bearing: " + decimalFormat.format(bearing) + "°\n\n" +
                            "Altitude: " + altitudeStr + measureUnitAltitude + "\n\n" +
                            "Speed: " + speedStr + measureUnitSpeed;
            dataTextView = (TextView) findViewById(R.id.dataTextView);
            dataTextView.setText(gpsData);
        }
    }
}