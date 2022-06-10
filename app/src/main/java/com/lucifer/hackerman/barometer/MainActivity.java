package com.lucifer.hackerman.barometer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;
// release version 1.0
public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor pressure;

    private float[] pressValue = new float[1];
    private int altitudeChoice = 1;
    public int pressureMeasureChoice = 2;
    private int speedChoice = 1;
    float calibrator = 0.0f;
    float meanSeaLevelPressure = 1013.25f;
    double gpsAlt = 0.0;
    float speed = 0.0f;

    final String SHARED_PREFERENCES = "SHARE_PREFERENCES";
    final String SAVED_CALIBRATION = "SAVED_CALIBRATION";
    final String SAVED_QNH = "SAVED_QNH";
    final String SAVED_PRESSURE_MEASURE_CHOICE = "SAVED_PRESSURE_MEASURE_CHOICE";
    final String SAVED_ALTITUDE_CHOICE = "SAVED_ALTITUDE_CHOICE";
    final String SAVED_SPEED_CHOICE = "SAVED_SPEED_CHOICE";
    public SharedPreferences preferences;

    TextView setQNH;
    TextView mBars;

    DecimalFormat decimalFormat = new DecimalFormat("####");
    DecimalFormat df_withDecimal = new DecimalFormat("#0.00");
    DecimalFormat df_Calibration_mBar_And_mm = new DecimalFormat("0.0");
    DecimalFormat df_mach = new DecimalFormat(".000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mBars = (TextView) findViewById(R.id.currentPressureTV);
        //editBarometerCalibration = (TextView) findViewById(R.id.barometerCalibration);
        setQNH = (TextView) findViewById(R.id.setQNH);
        loadEditTextsState();

        // Location
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // permission request
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, myLocationListener);
        updateWithNewLocation(l);

        buttonsClick();
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
        if (location != null) {
            speed = location.getSpeed();
            gpsAlt = location.getAltitude();
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
        //editBarometerCalibration.setText(String.valueOf(decimalFormat.format(calibrator)));

        //getting altitude
        mBar = mBar + calibrator;
        float altitude = SensorManager.getAltitude(meanSeaLevelPressure, mBar);
        float inhg = (mBar * 0.02953f);
        float mmhg = (mBar * 0.750062f);

        TextView currentPressure = (TextView) findViewById(R.id.currentPressureTV);
        TextView altimeter = (TextView) findViewById(R.id.altimeter);
        TextView altitudeText = (TextView) findViewById(R.id.altitudeText);
        //TextView baroText = (TextView) findViewById(R.id.baroText);
        TextView qnhText = (TextView) findViewById(R.id.qnhText);
        TextView speedTV = (TextView) findViewById(R.id.speed);
        TextView speedText = (TextView) findViewById(R.id.speedText);

        // show pressure
        switch (pressureMeasureChoice) {
            case (1):
                meanSeaLevelPressure = Float.parseFloat(decimalFormat.format(meanSeaLevelPressure));
                calibrator = Float.parseFloat(df_Calibration_mBar_And_mm.format(calibrator));
                qnhText.setText(R.string.QNH_hPa);
                setQNH.setText(decimalFormat.format(meanSeaLevelPressure));
                break;
            case (2):
                qnhText.setText(R.string.QNH_inHg);
                setQNH.setText(df_withDecimal.format(meanSeaLevelPressure * 0.02953f));
                break;
            case (3):
                qnhText.setText(R.string.QNH_mmHg);
                setQNH.setText(decimalFormat.format(meanSeaLevelPressure * 0.750062f));
            default:
                break;
        }

        // show altitude
        String altitudeTextM = "Altitude, m";
        String altitudeTextFt = "Altitude, ft";
        if (altitudeChoice == 1) {
            // show altitude in meter
            altimeter.setText(decimalFormat.format(altitude));
        } else {
            // show in foot
            float footSize = 3.28084f;
            altimeter.setText(decimalFormat.format(altitude * footSize));
        }

        // show speed
        switch (speedChoice) {
            case 1:
                speedText.setText(R.string.Speed_kmh);
                speedTV.setText(decimalFormat.format(speed * 3.6f));
                break;
            case 2:
                speedText.setText(R.string.Speed_mph);
                speedTV.setText(decimalFormat.format(speed * 2.23694f));
                break;
            case 3:
                speedText.setText(R.string.Speed_kts);
                speedTV.setText(decimalFormat.format(speed * 1.94384f));
                break;
            case 4:
                speedText.setText(R.string.Speed_mach);
                speedTV.setText(df_mach.format(speed * 0.00291545));
            default:
                break;
        }
    }

    // saving editTexts contains, calls in onDestroy()
    void saveEditTextsState() {
        preferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(SAVED_CALIBRATION, calibrator);
        editor.putFloat(SAVED_QNH, meanSeaLevelPressure);
        editor.putInt(SAVED_PRESSURE_MEASURE_CHOICE, pressureMeasureChoice);
        editor.putInt(SAVED_ALTITUDE_CHOICE, altitudeChoice);
        editor.putInt(SAVED_SPEED_CHOICE, speedChoice);
        editor.commit();
    }

    // load data before destroying and push it to editTexts, calls in onCreate()
    void loadEditTextsState() {
        preferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        float cal = preferences.getFloat(SAVED_CALIBRATION, 0);
        float qnh = preferences.getFloat(SAVED_QNH, 1013.25f);
        int pressureMeasure = preferences.getInt(SAVED_PRESSURE_MEASURE_CHOICE, 2);
        int speed = preferences.getInt(SAVED_SPEED_CHOICE, 1);
        int altitudeChoiceLoad = preferences.getInt(SAVED_ALTITUDE_CHOICE, 1);
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
        loadEditTextsState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveEditTextsState();
        sensorManager.unregisterListener(sensorEventListener);
        loadEditTextsState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveEditTextsState();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void vibration() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
    }

    private void buttonsClick() {
        Button minus = (Button) findViewById(R.id.minus);
        Button plus = (Button) findViewById(R.id.plus);
        Button settings = (Button) findViewById(R.id.buttonSettings);
        Button GPS = (Button) findViewById(R.id.buttonGPS);
        Button about = (Button) findViewById(R.id.buttonAbout);

        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                minusQNH();
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plusQNH();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSettings();
            }
        });

        GPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToGPS_Data();
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToAbout();
            }
        });
    }

    private void goToSettings() {
        vibration();
        Intent intent = new Intent(this, BarometerSetting.class);
        startActivity(intent);
    }

    private void goToGPS_Data() {
        vibration();
        Intent intent = new Intent(this, GPS_Data.class);
        intent.putExtra("Altitude", altitudeChoice);
        intent.putExtra("Speed", speedChoice);
        startActivity(intent);
    }

    private void goToAbout() {
        vibration();
        Intent intent = new Intent(this, About.class);
        startActivity(intent);
    }

    private void minusQNH() {
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

    private void plusQNH() {
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

}