package com.lucifer.hackerman.barometer;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor pressure;
    private float[] pressValue = new float[1];
    final String SAVED_CALIBRATION = "SAVED_CALIBRATION";
    final String SAVED_QNH = "SAVED_QNH";
    final String SAVED_QNH_INCH = "SAVED_QNH_INCH";
    SharedPreferences preferences;
    EditText editBarometerCalibration;
    EditText setQNH;
    EditText setQNHinch;
    TextView mBars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mBars = (TextView) findViewById(R.id.millibars);
        editBarometerCalibration = (EditText) findViewById(R.id.barometerCalibration);
        setQNH = (EditText) findViewById(R.id.setQNH);
        setQNHinch = (EditText) findViewById(R.id.setQNHinch);
        loadEditTextsState();

        // Location
        LocationManager locationManager;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(true);
        String provider = locationManager.getBestProvider(criteria, true);

        Location l = locationManager.getLastKnownLocation(provider);
        updateWithNewLocation(l);

        locationManager.requestLocationUpdates(provider, 1000, 1, myLocationListener);
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

    private void updateWithNewLocation(Location location) {
        TextView speedTV = (TextView) findViewById(R.id.speed);
        if (location != null) {
            double latitude = location.getLatitude();
            double longtitude = location.getLongitude();
            double altitude = location.getAltitude();
            float speed = location.getSpeed();
            DecimalFormat df = new DecimalFormat("###");
            speedTV.setText(String.valueOf(df.format(speed)));
        }

    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            final float alfa = 0.9f;
            pressValue[0] = alfa * pressValue[0] + (1 - alfa) * event.values[0]; // low pass filter
            while (pressValue[0] < 300) {
                pressValue[0] = event.values[0];
            }
            updateLayout(pressValue);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void updateLayout(float[] value) {
        float mBar = value[0];
        //barometer calibration
        String barometerCalibrationString = editBarometerCalibration.getText().toString();
        try {
            float barCal = Float.valueOf(barometerCalibrationString);
            mBar = mBar + barCal;
        } catch (Exception e) {
            System.out.println("calibration value error!");
        }

        // show pressure in millibars
        TextView millibars = (TextView) findViewById(R.id.millibars);
        DecimalFormat df_mBar = new DecimalFormat("####.#");
        millibars.setText(String.valueOf(df_mBar.format(mBar)));

        // show pressure in inches
        TextView inHg = (TextView) findViewById(R.id.inch);
        float inhg = (mBar * 0.02953f);
        DecimalFormat df_inHg = new DecimalFormat("##.##");
        inHg.setText(String.valueOf(df_inHg.format(inhg)));

        // getting QNH
        String QNH = setQNH.getText().toString();
        String QNHinch = setQNHinch.getText().toString();

        float meanSeaLevelPressure = 0;
        try {
            Float qnh = Float.valueOf(QNH);
            meanSeaLevelPressure = qnh;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        float meanSeaLevelPressureInch = 0;
        try {
            Float qnhinch = Float.valueOf(QNHinch);
            meanSeaLevelPressureInch = qnhinch;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        // checking focus
        final double finalMeanSeaLevelPressure = meanSeaLevelPressure;
        final double finalMeanSeaLevelPressureInch = meanSeaLevelPressureInch;

        // focus pressure
        setQNH.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setQNHinch.setText("");
            }
        });

        setQNHinch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setQNH.setText("");
            }
        });

        if (setQNH.getText().length() > 0 || setQNHinch.getText().length() > 0) {
            setQNHinch.setHint(String.valueOf(df_inHg.format(finalMeanSeaLevelPressure * 0.02953)) + " in Hg");
            setQNH.setHint(String.valueOf(df_mBar.format(finalMeanSeaLevelPressureInch / 0.02953)) + " hPa");
        } else {
            setQNHinch.setHint("29.92 in Hg");
            setQNH.setHint("1013.3 hPa");
        }

        //getting altitude
        float pressureAtStationLevel = mBar;
        float pressureAtStationLevelInInchHg = inhg;
        float altitude = 0;

        if (setQNH.getText().length() > 0)
            altitude = SensorManager.getAltitude(meanSeaLevelPressure, pressureAtStationLevel);
        else if (setQNHinch.getText().length() > 0)
            altitude = SensorManager.getAltitude(meanSeaLevelPressureInch, pressureAtStationLevelInInchHg);

        DecimalFormat df_altitude = new DecimalFormat("######");
        // show altitude in meter
        TextView altimeter = (TextView) findViewById(R.id.altimeter);
        altimeter.setText(String.valueOf(df_altitude.format(altitude)) + " m");
        // show in foot
        TextView altimeterInFoot = (TextView) findViewById(R.id.altimeterft);
        float footSize = 3.28084f;
        altimeterInFoot.setText(String.valueOf(df_altitude.format(altitude * footSize)) + " ft");
    }

    // saving editTexts contains, calls in onDestroy()
    void saveEditTextsState() {
        preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SAVED_CALIBRATION, editBarometerCalibration.getText().toString());
        editor.putString(SAVED_QNH, setQNH.getText().toString());
        editor.putString(SAVED_QNH_INCH, setQNHinch.getText().toString());
        editor.commit();
    }

    // load data before destroying and push it to editTexts, calls in onCreate()
    void loadEditTextsState() {
        preferences = getPreferences(MODE_PRIVATE);
        String savedCalibration = preferences.getString(SAVED_CALIBRATION, "");
        String savedQNH = preferences.getString(SAVED_QNH, "");
        String savedQNHinch = preferences.getString(SAVED_QNH_INCH, "");
        editBarometerCalibration.setText(savedCalibration);
        setQNH.setText(savedQNH);
        setQNHinch.setText(savedQNHinch);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, pressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveEditTextsState();
    }
}