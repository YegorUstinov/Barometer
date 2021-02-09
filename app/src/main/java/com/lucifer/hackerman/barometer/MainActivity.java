package com.lucifer.hackerman.barometer;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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
        DecimalFormat df_hPa = new DecimalFormat("####");
        millibars.setText(String.valueOf(df_hPa.format(mBar)));

        // show pressure in mm
        TextView mmHg = (TextView) findViewById(R.id.mmHg);
        double mmhg = (mBar * 0.750062);
        DecimalFormat df_mmHg = new DecimalFormat("###");
        mmHg.setText(String.valueOf(df_mmHg.format(mmhg)));

        // show pressure in inches
        TextView inHg = (TextView) findViewById(R.id.inch);
        double inhg = (mBar * 0.02953);
        DecimalFormat df_inhg = new DecimalFormat("##.##");
        inHg.setText(String.valueOf(df_inhg.format(inhg)));

        // getting QNH
        EditText setQNH = (EditText) findViewById(R.id.setQNH);
        String QNH = setQNH.getText().toString();
        double meanSeaLevelOfPressure = 0;
        try {
            Double qnh = Double.valueOf(QNH);
            meanSeaLevelOfPressure = qnh;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        // getting temperature
        EditText temp = (EditText) findViewById(R.id.temperature);
        String t = temp.getText().toString();
        double temperature = 0;
        try {
            Double tmp = Double.valueOf(t);
            temperature = tmp;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        double pressure = mBar;
        // barometric formula
        double altitude = ((8000 / pressure) * (1 + (0.00366 * temperature))) * (meanSeaLevelOfPressure - pressure);

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