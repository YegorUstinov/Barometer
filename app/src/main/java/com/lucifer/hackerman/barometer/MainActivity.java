package com.lucifer.hackerman.barometer;

import android.content.Context;
import android.content.pm.ActivityInfo;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double mBar = event.values[0]; // get air pressure
        // show pressure in millibars
        TextView millibars = (TextView) findViewById(R.id.millibars);
        final DecimalFormat df_hPa = new DecimalFormat("####.#");
        millibars.setText(String.valueOf(df_hPa.format(mBar)));

        // show pressure in mm
        TextView mmHg = (TextView) findViewById(R.id.mmHg);
        double mmhg = (mBar * 0.750062);
        DecimalFormat df_mmHg = new DecimalFormat("###");
        mmHg.setText(String.valueOf(df_mmHg.format(mmhg)));

        // show pressure in inches
        TextView inHg = (TextView) findViewById(R.id.inch);
        double inhg = (mBar * 0.02953);
        final DecimalFormat df_inhg = new DecimalFormat("##.##");
        inHg.setText(String.valueOf(df_inhg.format(inhg)));

        // getting QNH
        final EditText setQNH = (EditText) findViewById(R.id.setQNH);
        final EditText setQNHinch = (EditText) findViewById(R.id.setQNHinch);

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
            meanSeaLevelPressureInch = qnhinch / 0.02953;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        // getting temperature
        final EditText temp = (EditText) findViewById(R.id.temperature);
        final EditText tempFahrenheit = (EditText) findViewById(R.id.temperatureFahrenheit);

        String tFahrenheit = tempFahrenheit.getText().toString();
        String t = temp.getText().toString();

        final DecimalFormat df_temp = new DecimalFormat("##.#");

        double temperature = 0;
        try {
            Double tmp = Double.valueOf(t);
            temperature = tmp;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        double temperatureFahrenheit = 0;
        try {
            Double tmp = Double.valueOf(tFahrenheit);
            temperatureFahrenheit = tmp;
            temperature = (temperatureFahrenheit - 32) * 5 / 9;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        // checking focus
        final double finalTemperature = (temperature * 9 / 5) + 32;
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
            setQNHinch.setHint(String.valueOf(df_inhg.format(finalMeanSeaLevelPressure * 0.02953)) + " in Hg");
            setQNH.setHint(String.valueOf(df_hPa.format(finalMeanSeaLevelPressureInch)) + " hPa");
        } else {
            setQNHinch.setHint("in Hg");
            setQNH.setHint("hPa");
        }


        //getting altitude
        double pressure = mBar;
        double altitude = 0;
        if (setQNH.getText().length() > 0) {
            // barometric formula
            altitude = 18400 * (1 + 0.00366 * temperature) * Math.log10(meanSeaLevelPressure / pressure);
        } else if (setQNHinch.getText().length() > 0) {
            altitude = 18400 * (1 + 0.00366 * temperature) * Math.log10(meanSeaLevelPressureInch / pressure);
        }

        // show altitude
        TextView altimeter = (TextView) findViewById(R.id.altimeter);
        DecimalFormat df_meters = new DecimalFormat("#####");
        altimeter.setText(String.valueOf(df_meters.format(altitude)) + " m");

        TextView altimeterInFoot = (TextView) findViewById(R.id.altimeterft);
        double footSize = 3.28084;
        DecimalFormat df_ft = new DecimalFormat("#####");
        altimeterInFoot.setText(String.valueOf(df_ft.format(altitude * footSize)) + " ft");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
}