package com.vertex.mobilemouse;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Socket socket;
    private OutputStream outputStream;
    private float[] acceleration = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button leftClickButton = findViewById(R.id.leftClickButton);
        Button rightClickButton = findViewById(R.id.rightClickButton);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        new Thread(() -> {
            try {
                socket = new Socket("10.90.0.1", 8888);
                outputStream = socket.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        leftClickButton.setOnClickListener(v -> sendCommand("LEFTCLICK--"));
        rightClickButton.setOnClickListener(v -> sendCommand("RIGHTCLICK--"));

    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            if (Math.abs(event.values[0] - acceleration[0]) > 0.2 || Math.abs(event.values[1] - acceleration[1]) > 0.2){
                acceleration[0] = event.values[0];
                acceleration[1] = event.values[1];
                acceleration[2] = event.values[2];
                sendCommand("MOVE_" + acceleration[0] + "_" + acceleration[1] + "--");
                Log.d("XYZ", "XYZ : " + acceleration[0] + " " + acceleration[1]);
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void sendCommand(String command) {
        new Thread(() -> {
            try {
                outputStream.write(command.getBytes());
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}