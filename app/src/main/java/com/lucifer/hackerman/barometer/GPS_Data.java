package com.lucifer.hackerman.barometer;

import android.Manifest;
import android.content.Context;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_data);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
    DecimalFormat df_coordinates = new DecimalFormat("##.######°");

    private void updateWithNewLocation(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            float accuracy = location.getAccuracy();
            float bearing = location.getBearing();
            float bearingAccuracy = location.getBearingAccuracyDegrees();
            double altitude = location.getAltitude();
            float altitudeAccuracy = location.getVerticalAccuracyMeters();
            float speed = location.getSpeed() * 3600;
            float speedAccuracy = location.getSpeedAccuracyMetersPerSecond();
            String provider = location.getProvider();
            gpsData = "Latitude: " + df_coordinates.format(latitude) + "\n" +
                    "Longitude: " + df_coordinates.format(longitude) + "\n" +
                    "Accuracy: " + accuracy + " m\n" +
                    "Bearing: " + decimalFormat.format(bearing) + "°\n" +
                    "Bearing Accuracy: " + bearingAccuracy + "°\n" +
                    "Altitude: " + decimalFormat.format(altitude) + " m\n" +
                    "Altitude Accuracy: " + decimalFormat.format(altitudeAccuracy) + " m\n" +
                    "Speed: " + decimalFormat.format(speed) + " km/h\n" +
                    "Speed Accuracy: " + decimalFormat.format(speedAccuracy) + " m/s\n" +
                    "Provider: " + provider;
            dataTextView = (TextView) findViewById(R.id.dataTextView);
            dataTextView.setText(gpsData);
        }
    }
}