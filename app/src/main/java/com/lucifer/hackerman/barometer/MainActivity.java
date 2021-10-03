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
    private boolean altitudeChoice = true;
    private int pressureMeasureChoice = 1;
    private int speedChoice = 1;
    private String barometerCalibrationString = "";
    String QNH = "";

    final String SAVED_CALIBRATION = "SAVED_CALIBRATION";
    final String SAVED_QNH = "SAVED_QNH";
    final String SAVED_PRESSURE_MEASURE_CHOICE = "SAVED_PRESSURE_MEASURE_CHOICE";
    final String SAVED_ALTITUDE_CHOICE = "SAVED_ALTITUDE_CHOICE";
    final String SAVED_SPEED_CHOICE = "SAVED_SPEED_CHOICE";
    SharedPreferences preferences;
    EditText editBarometerCalibration;
    EditText setQNH;
    TextView mBars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mBars = (TextView) findViewById(R.id.currentPressure);
        editBarometerCalibration = (EditText) findViewById(R.id.barometerCalibration);
        setQNH = (EditText) findViewById(R.id.setQNH);
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
        if (location != null) {
            float speed = location.getSpeed();
            DecimalFormat df = new DecimalFormat("###");
            switch (speedChoice) {
                case 1:
                    speedTV.setText(df.format(speed * 3.6f) + " km/h");
                    break;
                case 2:
                    speedTV.setText(df.format(speed * 2.23694f) + " mph");
                    break;
                case 3:
                    speedTV.setText(df.format(speed * 1.94384f) + " knots");
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
        //barometer calibration
        barometerCalibrationString = editBarometerCalibration.getText().toString();
        float barCal = 0.0f;
        try {
            barCal = Float.valueOf(barometerCalibrationString);
        } catch (Exception e) {
            System.out.println("calibration value error!");
        }
        // getting QNH
        QNH = setQNH.getText().toString();
        float meanSeaLevelPressure = 0.0f;
        try {
            Float qnh = Float.valueOf(QNH);
            meanSeaLevelPressure = qnh;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        //getting altitude
        float altitude = 0.0f;
        float inhg = (mBar * 0.02953f);
        float mmhg = (mBar * 0.750062f);
        DecimalFormat df_altitude = new DecimalFormat("######");
        DecimalFormat df_mBar = new DecimalFormat("####.#");
        DecimalFormat df_inHg = new DecimalFormat("##.##");
        DecimalFormat df_mmHg = new DecimalFormat("###");
        TextView currentPressure = (TextView) findViewById(R.id.currentPressure);
        TextView altimeter = (TextView) findViewById(R.id.altimeter);

        switch (pressureMeasureChoice) {
            case (1):
                // show pressure in millibars
                mBar = mBar + barCal;
                if (setQNH.getText().length() > 0)
                    altitude = SensorManager.getAltitude(meanSeaLevelPressure, mBar);
                currentPressure.setText(df_mBar.format(mBar) + " hPa");
                break;
            case (2):
                // show pressure in inchHg
                inhg = inhg + barCal;
                if (setQNH.getText().length() > 0)
                    altitude = SensorManager.getAltitude(meanSeaLevelPressure, inhg);
                currentPressure.setText(df_inHg.format(inhg) + " inHg");
                break;
            case (3):
                // show pressure in mmHg
                mmhg = mmhg + barCal;
                if (setQNH.getText().length() > 0)
                    altitude = SensorManager.getAltitude(meanSeaLevelPressure, mmhg);
                currentPressure.setText(df_mmHg.format(mmhg) + " mmHg");
            default:
                break;
        }


        if (altitudeChoice) {
            // show altitude in meter
            altimeter.setText(df_altitude.format(altitude) + " m");
        } else {
            // show in foot
            float footSize = 3.28084f;
            altimeter.setText(df_altitude.format(altitude * footSize) + " ft");
        }
    }

    // saving editTexts contains, calls in onDestroy()
    void saveEditTextsState() {
        preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SAVED_CALIBRATION, editBarometerCalibration.getText().toString());
        editor.putString(SAVED_QNH, setQNH.getText().toString());
        editor.putInt(SAVED_PRESSURE_MEASURE_CHOICE, pressureMeasureChoice);
        editor.putBoolean(SAVED_ALTITUDE_CHOICE, altitudeChoice);
        editor.putInt(SAVED_SPEED_CHOICE, speedChoice);
        editor.commit();
    }

    // load data before destroying and push it to editTexts, calls in onCreate()
    void loadEditTextsState() {
        preferences = getPreferences(MODE_PRIVATE);
        String savedCalibration = preferences.getString(SAVED_CALIBRATION, "");
        String savedQNH = preferences.getString(SAVED_QNH, "");
        int pressureMeasure = preferences.getInt(SAVED_PRESSURE_MEASURE_CHOICE, 1);
        int speed = preferences.getInt(SAVED_SPEED_CHOICE, 1);
        boolean altitudeChoiceLoad = preferences.getBoolean(SAVED_ALTITUDE_CHOICE, true);

        editBarometerCalibration.setText(savedCalibration);
        setQNH.setText(savedQNH);
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
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveEditTextsState();
    }

    public void onClickAltitude(View view) {
        if (altitudeChoice) {
            altitudeChoice = false;
        } else {
            altitudeChoice = true;
        }
    }

    public void onClickCurrentPressure(View view) {
        pressureMeasureChoice++;
        if (pressureMeasureChoice > 3)
            pressureMeasureChoice = 1;
        barometerCalibrationString = editBarometerCalibration.getText().toString();
        if (barometerCalibrationString.length() > 0) {
            float barCal = 0f;
            try {
                barCal = Float.valueOf(barometerCalibrationString);
            } catch (Exception e) {
                System.out.println("calibration value error!");
            }

            DecimalFormat df_calibration = new DecimalFormat("###.##");
            switch (pressureMeasureChoice) {
                case 1:
                    barCal = barCal * 1.33322f;
                    String setPascal = Float.toString(Float.parseFloat(df_calibration.format(barCal)));
                    editBarometerCalibration.setText(setPascal);
                    break;
                case 2:

                    barCal = barCal * 0.02953f;
                    String setInch = Float.toString(Float.parseFloat(df_calibration.format(barCal)));
                    editBarometerCalibration.setText(setInch);
                    break;
                case 3:
                    barCal = barCal * 25.4f;
                    String setMillimetre = Float.toString(Float.parseFloat(df_calibration.format(barCal)));
                    editBarometerCalibration.setText(setMillimetre);
                    break;
                default:
                    break;
            }
        }
        if (QNH.length() > 0) {
            float qnh = 0.0f;
            QNH = setQNH.getText().toString();
            try {
                Float q = Float.valueOf(QNH);
                qnh = q;
            } catch (Exception e) {
                System.out.println("input error!");
            }
            DecimalFormat df_qnh = new DecimalFormat("####.##");
            switch (pressureMeasureChoice) {
                case 1:
                    qnh = qnh * 1.33322f;
                    String setPascal = Float.toString(Float.parseFloat(df_qnh.format(qnh)));
                    setQNH.setText(setPascal);
                    break;
                case 2:
                    qnh = qnh * 0.02953f;
                    String setInch = Float.toString(Float.parseFloat(df_qnh.format(qnh)));
                    setQNH.setText(setInch);

                    break;
                case 3:
                    qnh = qnh * 25.4f;
                    String setMillimetre = Float.toString(Float.parseFloat(df_qnh.format(qnh)));
                    setQNH.setText(setMillimetre);

                    break;
                default:
                    break;
            }
        }
    }

    public void onClickSpeed(View view) {
        speedChoice++;
        if (speedChoice > 3)
            speedChoice = 1;
    }
}