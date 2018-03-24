package com.example.abdallah.controlunit;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements SensorEventListener, CompoundButton.OnCheckedChangeListener {

    Sensor lightSensor ;
    SensorManager sensorManager ;
    ImageView lampImageView ;
    Switch notificationSwitch ;
    FirebaseDatabase firebaseDatabase ;
    DatabaseReference databaseReference ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        lampImageView = findViewById(R.id.lampImageView);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        notificationSwitch.setOnCheckedChangeListener(this);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("SmartLight");



    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.values[0]>60){
            lampImageView.setImageResource(R.drawable.lamp_on);
           // Toast.makeText(this, "Light is On", Toast.LENGTH_SHORT).show();
            databaseReference.child("Light_Status").setValue("ON");



            //Handle Push Notification Here..

        }
        else {
            lampImageView.setImageResource(R.drawable.lamp_off);
           // Toast.makeText(this, "Light is Off", Toast.LENGTH_SHORT).show();
            databaseReference.child("Light_Status").setValue("OFF");



        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            Toast.makeText(this, "Notification Is Turned On.", Toast.LENGTH_SHORT).show();

            databaseReference.child("Notification_Status").setValue("ON");
        }
        else if(!isChecked){
            Toast.makeText(this, "Notification Is Turned Off", Toast.LENGTH_SHORT).show();

            databaseReference.child("Notification_Status").setValue("OFF");

        }

    }
}
