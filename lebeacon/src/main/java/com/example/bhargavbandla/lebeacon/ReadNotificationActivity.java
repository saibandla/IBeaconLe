package com.example.bhargavbandla.lebeacon;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by BhargavBandla on 02/03/15.
 */
public class ReadNotificationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readnotify);
        TextView textView = (TextView) findViewById(R.id.textViewnoity);
        textView.setText(getIntent().getStringExtra("NotificationMsg"));
    }
}
