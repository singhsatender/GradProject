package com.stressfreeroads.wear;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.Timestamp;

public class MainActivity extends WearableActivity implements  PPGService.OnChangeListener, HeartRateService.OnChangeListener  {

    private TextView mTextView;
    private Button mstart;
    private Button mstop;
    private static final String LOG_TAG = "MainWearActivity";
    BufferedWriter writer;
    Long x;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
                // as soon as layout is there...
                mTextView = (TextView) findViewById(R.id.ppg);//
        mstart = (Button)findViewById(R.id.start);
        mstart.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
        // bind to our service.
        bindService(new Intent(MainActivity.this, PPGService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
        bindService(new Intent(MainActivity.this, HeartRateService.class), mServiceConnection, Service.BIND_AUTO_CREATE);

                fileManager();

            }
        });

        mstop  = (Button) findViewById(R.id.stop);
        mstop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                try {
                    unbindService(mServiceConnection);
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });



    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            Log.d(LOG_TAG, "connected to service.");
            // set our change listener to get change events
           // ((PPGService.PPGServiceBinder) binder).setChangeListener(MainActivity.this);
            ((HeartRateService.HeartRateServiceBinder) binder).setChangeListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    protected  void fileManager(){
        // Code here executes on main thread after user presses button
        x = System.currentTimeMillis() / 1000;
        try {
            // Creates a file in the primary external storage space of the
            // current application.
            // If the file does not exists, it is created.
            File testFile = new File(this.getExternalFilesDir(null), "PPGData.txt");
            if (!testFile.exists())
                testFile.createNewFile();

            // Adds a line to the file
             writer = new BufferedWriter(new FileWriter(testFile, true /*append*/));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onValueChanged(int newValue) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(timestamp);
        String timestampedVal = newValue+" : "+timestamp+", ";
        Log.d(LOG_TAG,"sending new value to listener: " + timestampedVal);
        try {
            writer.write(timestampedVal+"\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // will be called by the service whenever the heartbeat value changes.
        mTextView.setText(Integer.toString(newValue));
    }

    @Override
    protected void onStop(){
        super.onStop();

    }


}
