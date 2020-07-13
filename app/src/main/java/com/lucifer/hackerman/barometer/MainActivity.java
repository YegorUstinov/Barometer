package com.lucifer.hackerman.barometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
        float millibarsOfPressure = event.values[0];
        float mmhg = (float) (millibarsOfPressure * 0.750062);
        float kpa = (float) (millibarsOfPressure * 0.1);
        float psi = (float) (millibarsOfPressure * 0.0145038);

        TextView tv = (TextView) findViewById(R.id.tv);
        tv.setText("Pressure in millibars: " + String.valueOf(millibarsOfPressure));

        TextView mmHg = (TextView) findViewById(R.id.mmHg);
        mmHg.setText("Pressure in mmHg: " + String.valueOf(mmhg));

        TextView kPa = (TextView) findViewById(R.id.kPa);
        kPa.setText("Pressure in kPa: " + String.valueOf(kpa));

        TextView PSI = (TextView) findViewById(R.id.PSI);
        PSI.setText("Pressure in psi: " + String.valueOf(psi));
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