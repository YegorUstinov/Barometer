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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor pressure;

    private float[] pressValue = new float[1];
    private boolean altitudeChoice = true;
    private int pressureMeasureChoice = 2;
    private int speedChoice = 1;
    float calibrator = 0.0f;
    float meanSeaLevelPressure = 1013.25f;
    double gpsAlt = 0.0;

    final String SAVED_CALIBRATION = "SAVED_CALIBRATION";
    final String SAVED_QNH = "SAVED_QNH";
    final String SAVED_PRESSURE_MEASURE_CHOICE = "SAVED_PRESSURE_MEASURE_CHOICE";
    final String SAVED_ALTITUDE_CHOICE = "SAVED_ALTITUDE_CHOICE";
    final String SAVED_SPEED_CHOICE = "SAVED_SPEED_CHOICE";
    SharedPreferences preferences;
    TextView editBarometerCalibration;
    TextView setQNH;
    TextView mBars;

    DecimalFormat decimalFormat = new DecimalFormat("####");
    DecimalFormat df_withDecimal = new DecimalFormat("##.##");
    DecimalFormat df_Calibration_mBar_And_mm = new DecimalFormat("#.#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mBars = (TextView) findViewById(R.id.currentPressure);
        editBarometerCalibration = (TextView) findViewById(R.id.barometerCalibration);
        setQNH = (TextView) findViewById(R.id.setQNH);
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

        locationManager.requestLocationUpdates(provider, 10, 0, myLocationListener);
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
        TextView speedText = (TextView) findViewById(R.id.speedText);
        if (location != null) {
            float speed = location.getSpeed();
            gpsAlt = location.getAltitude();
            DecimalFormat df = new DecimalFormat("###");
            switch (speedChoice) {
                case 1:
                    speedText.setText("Speed, km/h");
                    speedTV.setText(df.format(speed * 3.6f));
                    break;
                case 2:
                    speedText.setText("Speed, mph");
                    speedTV.setText(df.format(speed * 2.23694f));
                    break;
                case 3:
                    speedText.setText("Speed, kts");
                    speedTV.setText(df.format(speed * 1.94384f));
                    break;
                default:
                    break;
            }
        }

    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            final float alfa = 0.8f;
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
        editBarometerCalibration.setText(String.valueOf(decimalFormat.format(calibrator)));

        float barCal = 0.0f;
        try {
            barCal = calibrator;
        } catch (Exception e) {
            System.out.println("calibration value error!");
        }

        //getting altitude
        mBar = mBar + barCal;
        float altitude = SensorManager.getAltitude(meanSeaLevelPressure, mBar);
        float inhg = (mBar * 0.02953f);
        float mmhg = (mBar * 0.750062f);

        TextView currentPressure = (TextView) findViewById(R.id.currentPressure);
        TextView altimeter = (TextView) findViewById(R.id.altimeter);
        TextView altitudeText = (TextView) findViewById(R.id.altitudeText);
        TextView baroText = (TextView) findViewById(R.id.baroText);
        TextView qnhText = (TextView) findViewById(R.id.qnhText);

        switch (pressureMeasureChoice) {
            case (1):
                // show pressure in millibars
                meanSeaLevelPressure = Float.parseFloat(decimalFormat.format(meanSeaLevelPressure));
                calibrator = Float.parseFloat(df_Calibration_mBar_And_mm.format(calibrator));
                currentPressure.setText(decimalFormat.format(mBar));
                baroText.setText("Barometer, hPa");
                qnhText.setText("QNH, hPa");
                editBarometerCalibration.setText(df_Calibration_mBar_And_mm.format(calibrator));
                setQNH.setText(decimalFormat.format(meanSeaLevelPressure));
                break;
            case (2):
                // show pressure in inchHg
                currentPressure.setText(df_withDecimal.format(inhg));
                baroText.setText("Barometer, in Hg");
                qnhText.setText("QNH, in Hg");
                editBarometerCalibration.setText(df_withDecimal.format(calibrator * 0.02953f));
                setQNH.setText(df_withDecimal.format(meanSeaLevelPressure * 0.02953f));
                break;
            case (3):
                // show pressure in mmHg
                currentPressure.setText(decimalFormat.format(mmhg));
                baroText.setText("Barometer, mm Hg");
                qnhText.setText("QNH, mm Hg");
                editBarometerCalibration.setText(df_Calibration_mBar_And_mm.format(calibrator * 0.750062f));
                setQNH.setText(decimalFormat.format(meanSeaLevelPressure * 0.750062f));
            default:
                break;
        }

        if (altitudeChoice) {
            // show altitude in meter
            String gpsAltString = String.valueOf(decimalFormat.format(gpsAlt));
            altitudeText.setText("Altitude, m " + "<GPS: " + gpsAltString + ">");
            altimeter.setText(decimalFormat.format(altitude));
        } else {
            // show in foot
            float footSize = 3.28084f;
            String gpsAltString = String.valueOf(decimalFormat.format(gpsAlt * footSize));
            altitudeText.setText("Altitude, ft " + "<GPS: " + gpsAltString + ">");
            altimeter.setText(decimalFormat.format(altitude * footSize));
        }
    }

    // saving editTexts contains, calls in onDestroy()
    void saveEditTextsState() {
        preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(SAVED_CALIBRATION, calibrator);
        editor.putFloat(SAVED_QNH, meanSeaLevelPressure);
        editor.putInt(SAVED_PRESSURE_MEASURE_CHOICE, pressureMeasureChoice);
        editor.putBoolean(SAVED_ALTITUDE_CHOICE, altitudeChoice);
        editor.putInt(SAVED_SPEED_CHOICE, speedChoice);
        editor.commit();
    }

    // load data before destroying and push it to editTexts, calls in onCreate()
    void loadEditTextsState() {
        preferences = getPreferences(MODE_PRIVATE);
        float cal = preferences.getFloat(SAVED_CALIBRATION, 0);
        float qnh = preferences.getFloat(SAVED_QNH, 1013.25f);
        int pressureMeasure = preferences.getInt(SAVED_PRESSURE_MEASURE_CHOICE, 2);
        int speed = preferences.getInt(SAVED_SPEED_CHOICE, 1);
        boolean altitudeChoiceLoad = preferences.getBoolean(SAVED_ALTITUDE_CHOICE, true);
        calibrator = cal;
        meanSeaLevelPressure = qnh;
        pressureMeasureChoice = pressureMeasure;
        altitudeChoice = altitudeChoiceLoad;
        speedChoice = speed;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, pressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveEditTextsState();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveEditTextsState();
    }

    public void onClickAltitude(View view) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.EFFECT_TICK));
        if (altitudeChoice) {
            altitudeChoice = false;
        } else {
            altitudeChoice = true;
        }
    }

    public void onClickCurrentPressure(View view) {
        vibration();
        pressureMeasureChoice++;
        if (pressureMeasureChoice > 3)
            pressureMeasureChoice = 1;
    }

    public void onClickSpeed(View view) {
        vibration();
        speedChoice++;
        if (speedChoice > 3)
            speedChoice = 1;
    }

    public void onClickCalibrationMinus(View view) {
        vibration();
        if (calibrator > -10) {
            switch (pressureMeasureChoice) {
                case (1):
                    calibrator = calibrator - 0.1f;
                    break;
                case (2):
                    calibrator = calibrator - 0.3386389f;
                    break;
                case (3):
                    calibrator = calibrator - 1.33322f;
                default:
                    break;
            }
        }
    }

    public void onClickCalibrationPlus(View view) {
        vibration();
        if (calibrator < 10) {
            switch (pressureMeasureChoice) {
                case (1):
                    calibrator = calibrator + 0.1f;
                    break;
                case (2):
                    calibrator = calibrator + 0.3386389f;
                    break;
                case (3):
                    calibrator = calibrator + 1.33322f;
                default:
                    break;
            }
        }
    }

    public void onClickPlus(View view) {
        vibration();
        if (meanSeaLevelPressure < 1084.0f) {
            switch (pressureMeasureChoice) {
                case (1):
                    meanSeaLevelPressure = meanSeaLevelPressure + 1.0f;
                    break;
                case (2):
                    meanSeaLevelPressure = meanSeaLevelPressure + 0.3386389f;
                    break;
                case (3):
                    meanSeaLevelPressure = meanSeaLevelPressure + 1.33322f;
                default:
                    break;
            }
        }
    }

    public void onClickMinus(View view) {
        vibration();
        if (meanSeaLevelPressure > 948.0f) {
            switch (pressureMeasureChoice) {
                case (1):
                    meanSeaLevelPressure = meanSeaLevelPressure - 1.0f;
                    break;
                case (2):
                    meanSeaLevelPressure = meanSeaLevelPressure - 0.3386389f;
                    break;
                case (3):
                    meanSeaLevelPressure = meanSeaLevelPressure - 1.33322f;
                default:
                    break;
            }
        }
    }

    public void vibration() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(5, VibrationEffect.EFFECT_TICK));
    }
}