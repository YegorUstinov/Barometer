package com.lucifer.hackerman.barometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // float type formatter
        DecimalFormat df = new DecimalFormat("####.#");

        float millibarsOfPressure = event.values[0]; // get air pressure

        // other measure units
        float atm = (float) (millibarsOfPressure * 0.000986923);
        float mmhg = (float) (millibarsOfPressure * 0.750062);
        float kpa = (float) (millibarsOfPressure * 0.1);
        float psi = (float) (millibarsOfPressure * 0.0145038);

        // block below shows air pressure on screen
        TextView millibars = (TextView) findViewById(R.id.millibars);
        millibars.setText(String.valueOf(df.format(millibarsOfPressure)));

        TextView Atmosphere = (TextView) findViewById(R.id.atm);
        Atmosphere.setText(String.valueOf(df.format(atm)));

        TextView mmHg = (TextView) findViewById(R.id.mmHg);
        mmHg.setText(String.valueOf(df.format(mmhg)));

        TextView kPa = (TextView) findViewById(R.id.kPa);
        kPa.setText(String.valueOf(df.format(kpa)));

        TextView PSI = (TextView) findViewById(R.id.PSI);
        PSI.setText(String.valueOf(df.format(psi)));
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