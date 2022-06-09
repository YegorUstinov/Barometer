package com.lucifer.hackerman.barometer;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView aboutTextView = (TextView) findViewById(R.id.aboutTextView);
        aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}