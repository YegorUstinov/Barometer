package com.lucifer.hackerman.barometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class BarometerSetting extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor pressure;

    final String SHARED_PREFERENCES = "SHARE_PREFERENCES";
    final String SAVED_CALIBRATION = "SAVED_CALIBRATION";
    final String SAVED_PRESSURE_MEASURE_CHOICE = "SAVED_PRESSURE_MEASURE_CHOICE";
    final String SAVED_ALTITUDE_CHOICE = "SAVED_ALTITUDE_CHOICE";
    final String SAVED_SPEED_CHOICE = "SAVED_SPEED_CHOICE";
    public SharedPreferences preferences;

    private float[] pressValue = new float[1];

    private int altitudeChoice = 1;
    private int speedChoice = 1;
    float calibrator = 0.0f;
    private int pressureMeasureChoice = 2;

    Button minus;
    Button plus;

    TextView currentPressureTV;
    TextView barometerCalibrationTV;

    RadioGroup barometerUnitGroup;
    RadioGroup altitudeUnitGroup;
    RadioGroup speedUnitGroup;

    RadioButton radioButton_hPa;
    RadioButton radioButton_inHg;
    RadioButton radioButton_mmHg;

    RadioButton radioKilometer;
    RadioButton radioMile;
    RadioButton radioKnot;
    RadioButton radioMach;

    RadioButton radioFeet;
    RadioButton radioMeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barometer_setting);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        barometerCalibrationTV = (TextView) findViewById(R.id.barometerCalibrationTV);

        loadEditTextsState();

        barometerUnitGroup = (RadioGroup) findViewById(R.id.barometerUnitsGroup);
        altitudeUnitGroup = (RadioGroup) findViewById(R.id.altitudeUnitGroup);
        speedUnitGroup = (RadioGroup) findViewById(R.id.speedUnitGroup);

        radioButton_hPa = (RadioButton) findViewById(R.id.radioButton_hPa);
        radioButton_inHg = (RadioButton) findViewById(R.id.radioButton_inHg);
        radioButton_mmHg = (RadioButton) findViewById(R.id.radioButton_mmHg);

        radioKilometer = (RadioButton) findViewById(R.id.radioKilometers);
        radioMile = (RadioButton) findViewById(R.id.radioMiles);
        radioKnot = (RadioButton) findViewById(R.id.radioKnots);
        radioMach = (RadioButton) findViewById(R.id.radioMach);

        radioFeet = (RadioButton) findViewById(R.id.radioFeet);
        radioMeter = (RadioButton) findViewById(R.id.radioMeter);

        // pressure
        changePressureSettings();

        // altitude
        changeAltitudeSettings();

        // speed
        changeSpeedSettings();

        clickMinus();
        clickPlus();

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


    DecimalFormat hPa_format = new DecimalFormat("0.0");
    DecimalFormat inhg_format = new DecimalFormat("00.00");
    DecimalFormat mmhg_format = new DecimalFormat("0.0");

    private void updateLayout(float[] value) {
        float mBar = value[0];
        barometerCalibrationTV.setText(hPa_format.format(calibrator));
        mBar = mBar + calibrator;
        currentPressureTV = (TextView) findViewById(R.id.currentPressureTV);
        currentPressureTV.setText(hPa_format.format(mBar));

        switch (pressureMeasureChoice) {
            case (1):
                // show pressure in millibars
                calibrator = Float.parseFloat(hPa_format.format(calibrator));
                barometerCalibrationTV.setText(hPa_format.format(calibrator));
                break;
            case (2):
                // show pressure in inchHg
                currentPressureTV.setText(inhg_format.format(mBar * 0.02953f));
                barometerCalibrationTV.setText(inhg_format.format(calibrator * 0.02953f));
                break;
            case (3):
                // show pressure in mmHg
                currentPressureTV.setText(mmhg_format.format(mBar * 0.750062f));
                barometerCalibrationTV.setText(mmhg_format.format(calibrator * 0.750062f));
            default:
                break;
        }
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
        sensorManager.unregisterListener(sensorEventListener);
        saveEditTextsState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveEditTextsState();
    }

    // saving editTexts contains, calls in onDestroy()
    void saveEditTextsState() {
        preferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(SAVED_CALIBRATION, calibrator);
        editor.putInt(SAVED_PRESSURE_MEASURE_CHOICE, pressureMeasureChoice);
        editor.putInt(SAVED_ALTITUDE_CHOICE, altitudeChoice);
        editor.putInt(SAVED_SPEED_CHOICE, speedChoice);
        editor.commit();
    }

    // load data before destroying and push it to editTexts, calls in onCreate()
    void loadEditTextsState() {
        preferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        float cal = preferences.getFloat(SAVED_CALIBRATION, 0);
        int pressureMeasure = preferences.getInt(SAVED_PRESSURE_MEASURE_CHOICE, 2);
        int speed = preferences.getInt(SAVED_SPEED_CHOICE, 1);
        int altitudeChoiceLoad = preferences.getInt(SAVED_ALTITUDE_CHOICE, 1);
        calibrator = cal;
        pressureMeasureChoice = pressureMeasure;
        altitudeChoice = altitudeChoiceLoad;
        speedChoice = speed;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void vibration() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
    }

    private void changePressureSettings() {
        switch (pressureMeasureChoice) {
            case 1:
                radioButton_hPa.toggle();
                break;
            case 2:
                radioButton_inHg.toggle();
                break;
            case 3:
                radioButton_mmHg.toggle();
                break;
            default:
                radioButton_inHg.toggle();
                break;
        }
        barometerUnitGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (id) {
                    case R.id.radioButton_hPa:
                        pressureMeasureChoice = 1;
                        break;
                    case R.id.radioButton_inHg:
                        pressureMeasureChoice = 2;
                        break;
                    case R.id.radioButton_mmHg:
                        pressureMeasureChoice = 3;
                        break;
                    default:
                        pressureMeasureChoice = 2;
                }
            }
        });
    }

    private void changeAltitudeSettings() {
        switch (altitudeChoice) {
            case 0:
                radioFeet.toggle();
                break;
            case 1:
                radioMeter.toggle();
                break;
            default:
                radioMeter.toggle();
                break;
        }
        altitudeUnitGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (id) {
                    case R.id.radioFeet:
                        altitudeChoice = 0;
                        break;
                    case R.id.radioMeter:
                        altitudeChoice = 1;
                        break;
                    default:
                        altitudeChoice = 1;
                        break;
                }
            }
        });
    }

    private void changeSpeedSettings() {
        switch (speedChoice) {
            case 1:
                radioKilometer.toggle();
                break;
            case 2:
                radioMile.toggle();
                break;
            case 3:
                radioKnot.toggle();
                break;
            default:
                radioMach.toggle();
                break;
        }
        speedUnitGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                switch (id) {
                    case R.id.radioKilometers:
                        speedChoice = 1;
                        break;
                    case R.id.radioMiles:
                        speedChoice = 2;
                        break;
                    case R.id.radioKnots:
                        speedChoice = 3;
                        break;
                    case R.id.radioMach:
                        speedChoice = 4;
                        break;
                    default:
                        speedChoice = 1;
                        break;
                }
            }
        });
    }

    private void clickMinus() {
        minus = (Button) findViewById(R.id.calibrationMinusSetting);
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                            calibrator = calibrator - 0.133322f;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private void clickPlus() {
        plus = (Button) findViewById(R.id.calibrationPlusSetting);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                            calibrator = calibrator + 0.133322f;
                        default:
                            break;
                    }
                }
            }
        });
    }
}