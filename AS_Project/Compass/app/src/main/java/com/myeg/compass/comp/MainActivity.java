package com.myeg.compass.comp;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    SensorManager mySensorManager ;
    private CompassView myCompassView;
    private Sensor orientationSensor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        myCompassView=findViewById(R.id.compassview);
        mySensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        orientationSensor=mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mySensorManager.registerListener(mySensorEventListener, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mySensorManager.unregisterListener(mySensorEventListener);
    }


    private SensorEventListener mySensorEventListener = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
          float  presentAngel =(float)Math.toDegrees(event.values[0]);//    (float)event.values[0];
//            float bearing;
//            bearing = compassBearing - templeBearing;
//            if (bearing < 0)
//                bearing = 360 + bearing;
            myCompassView.updateDirection(presentAngel);
        }
    };

}
