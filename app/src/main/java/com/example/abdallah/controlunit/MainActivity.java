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

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity implements SensorEventListener, CompoundButton.OnCheckedChangeListener {

    Sensor lightSensor;
    SensorManager sensorManager;
    ImageView lampImageView;
    Switch notificationSwitch;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    JSONObject message;
    JSONObject messageInfo;

    private final String PUSH_URL = "https://fcm.googleapis.com/fcm/send";

    private String USER_TOKEN = "f1suFSGwlSU:APA91bEXvZJ61wgsRgNTMUklroyGLIWJZdc0uD4_5ynw1V4mWdbyl7Xq9lgOinraXwI84b84HcGMAxzL7MLJ0lwS5LYE1xRahSEG5R3E7GEkWab7A-3BLwNxbkfawmTKlgBMwbjlFkQK";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        lampImageView = findViewById(R.id.lampImageView);
        notificationSwitch = findViewById(R.id.notificationSwitch);
        notificationSwitch.setOnCheckedChangeListener(this);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("SmartLight");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                USER_TOKEN = dataSnapshot.child("PushToken").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        message = new JSONObject();
        messageInfo = new JSONObject();

        try {
            messageInfo.put("title", "Light Notification");
            messageInfo.put("message", "You forget your home light turned on!");
            messageInfo.put("image-url", "https://lh3.googleusercontent.com/JrGwExTVGhm24PWMa6mjFFPXMmE1n-LnBtRC1_jtV_gmKiVrt9hVYPoZQPC9e66FBA=h900");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            message.put("to", USER_TOKEN);
            message.put("data", messageInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AndroidNetworking.initialize(getApplicationContext());

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addNetworkInterceptor(new StethoInterceptor()).build();

        AndroidNetworking.initialize(getApplicationContext(), okHttpClient);


    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > 60) {
            lampImageView.setImageResource(R.drawable.lamp_on);
            // Toast.makeText(this, "Light is On", Toast.LENGTH_SHORT).show();
            databaseReference.child("Light_Status").setValue("ON");

            //Handle Push Notification Here..

            AndroidNetworking.post(PUSH_URL)
                    .addJSONObjectBody(message)
                    .addHeaders("Authorization", "key=AAAAHcpVzbU:APA91bFBPHcYTmxltvg0_AI1_Gtou2zc-frJdyts6muO0VNOZmAGnbwksfXNg53lje2mwi_NeFW1svoV_Mi01sqqEdWL3So6AL1NAYSFn0Vp2texgrOtdvF3iUqPsp4zzjGlOCSdGDCB")
                    .addHeaders("Content-Type", "application/json")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Toast.makeText(MainActivity.this, "l:" + response.getString("success").toString(), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {

                        }
                    });


        } else {
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
        if (isChecked) {
            Toast.makeText(this, "Notification Is Turned On.", Toast.LENGTH_SHORT).show();

            databaseReference.child("Notification_Status").setValue("ON");
        } else if (!isChecked) {
            Toast.makeText(this, "Notification Is Turned Off", Toast.LENGTH_SHORT).show();

            databaseReference.child("Notification_Status").setValue("OFF");

        }

    }
}
