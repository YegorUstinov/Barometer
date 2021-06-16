package com.lucifer.hackerman.barometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor pressure;
    private float[] pressValue = new float[1];
    final String SAVED_CALIBRATION = "SAVED_CALIBRATION";
    final String SAVED_QNH = "SAVED_QNH";
    final String SAVED_QNH_INCH = "SAVED_QNH_INCH";
    final String SAVED_TEMP = "SAVED_TEMP";
    final String SAVED_TEMP_FAHRENHEIT = "SAVED_FAHRENHEIT";
    SharedPreferences preferences;
    EditText editBarometerCalibration;
    EditText setQNH;
    EditText setQNHinch;
    EditText temp;
    EditText tempFahrenheit;
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
        temp = (EditText) findViewById(R.id.temperature);
        tempFahrenheit = (EditText) findViewById(R.id.temperatureFahrenheit);
        loadEditTextsState();
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            final float alfa = 0.99f;
            pressValue[0] = alfa * pressValue[0] + (1 - alfa) * event.values[0]; // low pass filter
            while (pressValue[0] < 100) {
                pressValue[0] = event.values[0];
            }
            updateLayout(pressValue);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void updateLayout(float[] value) {
        double mBar = value[0];
        //barometer calibration
        String barometerCalibrationString = editBarometerCalibration.getText().toString();
        try {
            double barCal = Double.valueOf(barometerCalibrationString);
            mBar = mBar + barCal;
        } catch (Exception e) {
            System.out.println("calibration value error!");
        }

        // show pressure in millibars
        TextView millibars = (TextView) findViewById(R.id.millibars);
        DecimalFormat df_mBar = new DecimalFormat("####.#");
        millibars.setText(String.valueOf(df_mBar.format(mBar)));

        // show pressure in mm
        TextView mmHg = (TextView) findViewById(R.id.mmHg);
        double mmhg = mBar * 0.750062;
        DecimalFormat df_mmHg = new DecimalFormat("###.#");
        mmHg.setText(String.valueOf(df_mmHg.format(mmhg)));

        // show pressure in inches
        TextView inHg = (TextView) findViewById(R.id.inch);
        double inhg = (mBar * 0.02953);
        DecimalFormat df_inHg = new DecimalFormat("##.##");
        inHg.setText(String.valueOf(df_inHg.format(inhg)));

        // getting QNH
        String QNH = setQNH.getText().toString();
        String QNHinch = setQNHinch.getText().toString();

        double meanSeaLevelPressure = 0;
        try {
            Double qnh = Double.valueOf(QNH);
            meanSeaLevelPressure = qnh;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        double meanSeaLevelPressureInch = 0;
        try {
            Double qnhinch = Double.valueOf(QNHinch);
            meanSeaLevelPressureInch = qnhinch;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        // getting temperature
        String tFahrenheit = tempFahrenheit.getText().toString();
        String t = temp.getText().toString();

        final DecimalFormat df_temp = new DecimalFormat("##.#");

        double temperatureAtStation = 0;
        try {
            Double tmp = Double.valueOf(t);
            temperatureAtStation = tmp;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        double temperatureFahrenheit = 0;
        try {
            Double tmp = Double.valueOf(tFahrenheit);
            temperatureFahrenheit = tmp;
            temperatureAtStation = (temperatureFahrenheit - 32) * 5 / 9;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        // checking focus
        final double finalTemperature = (temperatureAtStation * 9 / 5) + 32;
        final double finalTemperatureFahrenheit = (temperatureFahrenheit - 32) * 5 / 9;
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

        // focus temp
        temp.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                tempFahrenheit.setText("");
            }
        });

        tempFahrenheit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                temp.setText("");
            }
        });

        if (temp.getText().length() > 0 || tempFahrenheit.getText().length() > 0) {
            temp.setHint(String.valueOf(df_temp.format(finalTemperatureFahrenheit)) + " °C");
            tempFahrenheit.setHint(String.valueOf(df_temp.format(finalTemperature)) + " °F");

        } else {
            temp.setHint("°C");
            tempFahrenheit.setHint("°F");
        }
        if (setQNH.getText().length() > 0 || setQNHinch.getText().length() > 0) {
            setQNHinch.setHint(String.valueOf(df_inHg.format(finalMeanSeaLevelPressure * 0.02953)) + " in Hg");
            setQNH.setHint(String.valueOf(df_mBar.format(finalMeanSeaLevelPressureInch / 0.02953)) + " hPa");
        } else {
            setQNHinch.setHint("29.92 in Hg");
            setQNH.setHint("1013.3 hPa");
        }


        //getting altitude
        double pressureAtStationLevel = mBar;
        double pressureAtStationLevelInInchHg = inhg;
        int hypsometricConstant = 18400;
        double altitude = 0;
        double temporaryStationElevation = 0;
        double coefficientOfExpansion = (float) 0.00367;

        // temporary altitude (using Laplace shortened formula)
        int tempForMeasurement = 0;
        if (setQNH.getText().length() > 0) {
            temporaryStationElevation = (hypsometricConstant *
                    (1 + coefficientOfExpansion * tempForMeasurement) *
                    Math.log10(meanSeaLevelPressure / pressureAtStationLevel));
        } else if (setQNHinch.getText().length() > 0) {
            temporaryStationElevation = (hypsometricConstant *
                    (1 + coefficientOfExpansion * tempForMeasurement) *
                    Math.log10(meanSeaLevelPressureInch / pressureAtStationLevelInInchHg));
        }
        double meanTemperatureOfAirColumn = temperatureAtStation + (3 * temporaryStationElevation) / 1000;

        // accuracy altitude (using Laplace shortened formula)
        if (setQNH.getText().length() > 0) {
            altitude = (hypsometricConstant *
                    (1 + coefficientOfExpansion * meanTemperatureOfAirColumn) *
                    Math.log10(meanSeaLevelPressure / pressureAtStationLevel));
        } else if (setQNHinch.getText().length() > 0) {
            altitude = (hypsometricConstant *
                    (1 + coefficientOfExpansion * meanTemperatureOfAirColumn) *
                    Math.log10(meanSeaLevelPressureInch / pressureAtStationLevelInInchHg));
        }

        DecimalFormat df_altitude = new DecimalFormat("######");
        // show altitude in meter
        TextView altimeter = (TextView) findViewById(R.id.altimeter);
        altimeter.setText(String.valueOf(df_altitude.format(altitude)) + " m");
        // show in foot
        TextView altimeterInFoot = (TextView) findViewById(R.id.altimeterft);
        double footSize = 3.28084;
        altimeterInFoot.setText(String.valueOf(df_altitude.format(altitude * footSize)) + " ft");
    }

    // saving editTexts contains, calls in onDestroy()
    void saveEditTextsState() {
        preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SAVED_CALIBRATION, editBarometerCalibration.getText().toString());
        editor.putString(SAVED_QNH, setQNH.getText().toString());
        editor.putString(SAVED_QNH_INCH, setQNHinch.getText().toString());
        editor.putString(SAVED_TEMP, temp.getText().toString());
        editor.putString(SAVED_TEMP_FAHRENHEIT, tempFahrenheit.getText().toString());
        editor.commit();
    }

    // load data before destroying and push it to editTexts, calls in onCreate()
    void loadEditTextsState() {
        preferences = getPreferences(MODE_PRIVATE);
        String savedCalibration = preferences.getString(SAVED_CALIBRATION, "");
        String savedQNH = preferences.getString(SAVED_QNH, "");
        String savedQNHinch = preferences.getString(SAVED_QNH_INCH, "");
        String savedTemperature = preferences.getString(SAVED_TEMP, "");
        String savedTemperatureFahrenheit = preferences.getString(SAVED_TEMP_FAHRENHEIT, "");
        Float savedPress = preferences.getFloat("press", 0);
        editBarometerCalibration.setText(savedCalibration);
        setQNH.setText(savedQNH);
        setQNHinch.setText(savedQNHinch);
        temp.setText(savedTemperature);
        tempFahrenheit.setText(savedTemperatureFahrenheit);
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