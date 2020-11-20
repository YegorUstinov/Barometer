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
    public void onSensorChanged(SensorEvent event
    ) {
        // float type formatter
        DecimalFormat df_hPa = new DecimalFormat("####.#");
        DecimalFormat df_mmHg = new DecimalFormat("####");
        DecimalFormat df_inhg = new DecimalFormat("####.##");

        float millibarsOfPressure = event.values[0]; // get air pressure

        // other measure units
        float mmhg = (float) (millibarsOfPressure * 0.750062);
        float inhg = (float) (millibarsOfPressure * 0.02953);

        // block below shows air pressure on screen
        TextView millibars = (TextView) findViewById(R.id.millibars);
        millibars.setText(String.valueOf(df_hPa.format(millibarsOfPressure)));

        TextView mmHg = (TextView) findViewById(R.id.mmHg);
        mmHg.setText(String.valueOf(df_mmHg.format(mmhg)));

        TextView inHg = (TextView) findViewById(R.id.inch);
        inHg.setText(String.valueOf(df_inhg.format(inhg)));
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