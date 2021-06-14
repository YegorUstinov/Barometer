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

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor pressure;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        editBarometerCalibration = (EditText) findViewById(R.id.barometerCalibration);
        setQNH = (EditText) findViewById(R.id.setQNH);
        setQNHinch = (EditText) findViewById(R.id.setQNHinch);
        temp = (EditText) findViewById(R.id.temperature);
        tempFahrenheit = (EditText) findViewById(R.id.temperatureFahrenheit);
        loadEditTextsState();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float milliBar = event.values[0]; // get air pressure
        //aliasing data from device
        DecimalFormat decimalFormatFor_mBar = new DecimalFormat("####.##");
        float mBar = Float.parseFloat(decimalFormatFor_mBar.format(milliBar));

        //barometer calibration
        String barometerCalibrationString = editBarometerCalibration.getText().toString();
        try {
            Float barCal = Float.valueOf(barometerCalibrationString);
            mBar = mBar + barCal;
        } catch (Exception e) {
            System.out.println("calibration value error!");
        }

        // show pressure in millibars
        int millibarsToShow = (int) mBar;
        TextView millibars = (TextView) findViewById(R.id.millibars);
        millibars.setText(String.valueOf(millibarsToShow));

        // show pressure in mm
        TextView mmHg = (TextView) findViewById(R.id.mmHg);
        int mmhg = (int) (mBar * 0.750062);
        mmHg.setText(String.valueOf(mmhg));

        // show pressure in inches
        TextView inHg = (TextView) findViewById(R.id.inch);
        float inhg = (float) (mBar * 0.02953);
        final DecimalFormat df_inhg = new DecimalFormat("##.##");
        inhg = Float.parseFloat(df_inhg.format(inhg));
        inHg.setText(String.valueOf(inhg));

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

        // getting temperature
        String tFahrenheit = tempFahrenheit.getText().toString();
        String t = temp.getText().toString();

        final DecimalFormat df_temp = new DecimalFormat("##.#");

        float temperatureAtStation = 0;
        try {
            Float tmp = Float.valueOf(t);
            temperatureAtStation = tmp;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        float temperatureFahrenheit = 0;
        try {
            Float tmp = Float.valueOf(tFahrenheit);
            temperatureFahrenheit = tmp;
            temperatureAtStation = (temperatureFahrenheit - 32) * 5 / 9;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        // checking focus
        final float finalTemperature = (temperatureAtStation * 9 / 5) + 32;
        final float finalTemperatureFahrenheit = (temperatureFahrenheit - 32) * 5 / 9;
        final float finalMeanSeaLevelPressure = meanSeaLevelPressure;
        final float finalMeanSeaLevelPressureInch = meanSeaLevelPressureInch;

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

        final DecimalFormat df_hPa = new DecimalFormat("####.#");
        if (temp.getText().length() > 0 || tempFahrenheit.getText().length() > 0) {
            temp.setHint(String.valueOf(df_temp.format(finalTemperatureFahrenheit)) + " °C");
            tempFahrenheit.setHint(String.valueOf(df_temp.format(finalTemperature)) + " °F");

        } else {
            temp.setHint("°C");
            tempFahrenheit.setHint("°F");
        }
        if (setQNH.getText().length() > 0 || setQNHinch.getText().length() > 0) {
            setQNHinch.setHint(String.valueOf(df_inhg.format(finalMeanSeaLevelPressure * 0.02953)) + " in Hg");
            setQNH.setHint(String.valueOf(df_hPa.format(finalMeanSeaLevelPressureInch / 0.02953)) + " hPa");
        } else {
            setQNHinch.setHint("29.92 in Hg");
            setQNH.setHint("1013.3 hPa");
        }


        //getting altitude
        float pressureAtStationLevel = mBar;
        float pressureAtStationLevelInInchHg = inhg;
        int hypsometricConstant = 18400;
        float altitude = 0;
        float temporaryStationElevation = 0;
        double coefficientOfExpansion = (float) 0.00367;

        // temporary altitude (using Laplace shortened formula)
        int tempForMeasurement = 0;
        if (setQNH.getText().length() > 0) {
            temporaryStationElevation = (float) (hypsometricConstant *
                    (1 + coefficientOfExpansion * tempForMeasurement) *
                    Math.log10(meanSeaLevelPressure / pressureAtStationLevel));
        } else if (setQNHinch.getText().length() > 0) {
            temporaryStationElevation = (float) (hypsometricConstant *
                    (1 + coefficientOfExpansion * tempForMeasurement) *
                    Math.log10(meanSeaLevelPressureInch / pressureAtStationLevelInInchHg));
        }
        double meanTemperatureOfAirColumn = temperatureAtStation + (3 * temporaryStationElevation) / 1000;

        // accuracy altitude (using Laplace shortened formula)
        if (setQNH.getText().length() > 0) {
            altitude = (float) (hypsometricConstant *
                    (1 + coefficientOfExpansion * meanTemperatureOfAirColumn) *
                    Math.log10(meanSeaLevelPressure / pressureAtStationLevel));
        } else if (setQNHinch.getText().length() > 0) {
            altitude = (float) (hypsometricConstant *
                    (1 + coefficientOfExpansion * meanTemperatureOfAirColumn) *
                    Math.log10(meanSeaLevelPressureInch / pressureAtStationLevelInInchHg));
        }

        // show altitude in meter
        TextView altimeter = (TextView) findViewById(R.id.altimeter);
        int altitudeToShow = (int) altitude;
        altimeter.setText(String.valueOf(altitudeToShow) + " m");
        // show in foot
        TextView altimeterInFoot = (TextView) findViewById(R.id.altimeterft);
        double footSize = 3.28084;
        int altitudeInFootToShow = (int) (altitude * footSize);
        altimeterInFoot.setText(String.valueOf(altitudeInFootToShow) + " ft");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
        editBarometerCalibration.setText(savedCalibration);
        setQNH.setText(savedQNH);
        setQNHinch.setText(savedQNHinch);
        temp.setText(savedTemperature);
        tempFahrenheit.setText(savedTemperatureFahrenheit);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveEditTextsState();
    }
}