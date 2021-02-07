package com.lucifer.hackerman.barometer;

import android.content.Context;
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
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // float type formatter
        DecimalFormat df_hPa = new DecimalFormat("####.#");
        DecimalFormat df_mmHg = new DecimalFormat("####");
        DecimalFormat df_inhg = new DecimalFormat("####.##");
        DecimalFormat df_meters = new DecimalFormat("####");

        float millibarsOfPressure = event.values[0]; // get air pressure
        TextView millibars = (TextView) findViewById(R.id.millibars);
        millibars.setText(String.valueOf(df_hPa.format(millibarsOfPressure)));

        TextView mmHg = (TextView) findViewById(R.id.mmHg);
        float mmhg = (float) (millibarsOfPressure * 0.750062);
        mmHg.setText(String.valueOf(df_mmHg.format(mmhg)));

        TextView inHg = (TextView) findViewById(R.id.inch);
        float inhg = (float) (millibarsOfPressure * 0.02953);
        inHg.setText(String.valueOf(df_inhg.format(inhg)));

        EditText setQNH = (EditText) findViewById(R.id.setQNH);
        String QNH = setQNH.getText().toString();

        EditText temp = (EditText) findViewById(R.id.temperature);
        String t = temp.getText().toString();

        // getting qnh
        float meanSeaLevelOfPressure = 0;
        try {
            Float qnh = Float.valueOf(QNH);
            meanSeaLevelOfPressure = qnh;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        // getting temperature
        float temperature = 0;
        try {
            Float tmp = Float.valueOf(t);
            temperature = tmp;
        } catch (Exception e) {
            System.out.println("input error!");
        }

        float pressure = millibarsOfPressure;
        float altitude = (float) ((8000 / pressure) * (1 + (0.00366 * temperature))) * (meanSeaLevelOfPressure - pressure);

        TextView altimeter = (TextView) findViewById(R.id.altimeter);
        altimeter.setText(String.valueOf(df_meters.format(altitude)) + " m");
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