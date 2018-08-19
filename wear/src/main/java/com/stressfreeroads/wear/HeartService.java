package com.stressfreeroads.wear;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Calculate Heart Rate for first 40 seconds and then automatically switches to calculate
 * PPG for rest of the trip.
 * Created by satender.
 */
public class HeartService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private int currentValue=0;
    private static final String LOG_TAG = "HeartService";
    private IBinder binder = new HeartServiceBinder();
    private OnChangeListener onChangeListener;
    private GoogleApiClient mGoogleApiClient;
    public static Timestamp mTimestamp;

    // interface to pass a ppg value to the implementing class
    public interface OnChangeListener {
        void onValueChanged(int newValue);
    }

    /**
     *
     * Binder for this service. The binding activity passes a listener we send the heartbeat to.
     */
    public class HeartServiceBinder extends Binder {
        public void setChangeListener(OnChangeListener listener) {
            onChangeListener = listener;
            // return currently known value
            listener.onValueChanged(currentValue);
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // register us as a sensor listener
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mPpgRateSensor = mSensorManager.getDefaultSensor(33171027);
        Sensor mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        // delay SENSOR_DELAY_UI is sufficient
        boolean res = mSensorManager.registerListener(this, mPpgRateSensor,  SensorManager.SENSOR_DELAY_FASTEST);
        boolean res1 = mSensorManager.registerListener(this, mHeartRateSensor,  SensorManager.SENSOR_DELAY_FASTEST);
        Log.d(LOG_TAG, " sensor registered: " + (res ? "yes" : "no"));
        Log.d(LOG_TAG, " sensor registered1: " + (res1 ? "yes" : "no"));

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        Log.d(LOG_TAG," sensor unregistered");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        int sec = 40;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mTimestamp.getTime());
        cal.add(Calendar.SECOND, sec);
        Timestamp limit = new Timestamp(cal.getTime().getTime());
        if(currentTimestamp.before(limit)) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_HEART_RATE && sensorEvent.values.length > 0) {
                int newValue = Math.round(sensorEvent.values[0]);
                // only do something if the value differs from the value before and the value is not 0.
                if (currentValue != newValue && newValue != 0) {
                    // save the new value
                    currentValue = newValue;
                    // send the value to the listener
                    if (onChangeListener != null) {
                        onChangeListener.onValueChanged(newValue);
                    }
                }
            }
        } else {
            // is this a heartbeat event and does it have data?
            if (sensorEvent.sensor.getType() == 33171027 && sensorEvent.values.length > 0) {
                int newValue = Math.round(sensorEvent.values[0]);
                // only do something if the value differs from the value before and the value is not 0.
                if (currentValue != newValue && newValue != 0) {
                    // save the new value
                    currentValue = newValue;
                    // send the value to the listener
                    if (onChangeListener != null) {
                        onChangeListener.onValueChanged(newValue);
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    /**
     * Set Current Timestamp to enable switching between Heart Beat and PPG recording.s
     * @param timestamp
     */
    public static void setTimestamp(Timestamp timestamp){
        mTimestamp = timestamp;
    }

}
