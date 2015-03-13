package com.uni.swansea.happyplant;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

public class MainActivity extends Activity {


    boolean mBounded;
    PlantDataService mServer;
    MessageReceiver messageReceiver;

    //public InterfaceKitPhidget ik;
    // TODO remove plantStatus when everything is over
    public PlantStatus plantStatus;
    // TOdo change TextView array
    public TextView[] sensorsTextViews;



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        plantStatus = new PlantStatus(new PlantStatusData(0,1,));

        final ImageView serviceStatusImageView = (ImageView)findViewById(R.id.serviceStatusImageView);
        serviceStatusImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMyServiceRunning(PlantDataService.class)) {
                    startService(new Intent(getApplicationContext(), PlantDataService.class));
                    serviceStatusImageView.setImageResource(R.drawable.pause);
                } else {
                    stopService(new Intent(getApplicationContext(), PlantDataService.class));
                    serviceStatusImageView.setImageResource(R.drawable.start);
                }
            }
        });

        messageReceiver = new MessageReceiver();

        sensorsTextViews = new TextView[8];
        sensorsTextViews[0] = (TextView)findViewById(R.id.tempLabelText);
        sensorsTextViews[1] = (TextView)findViewById(R.id.lightLabelText);
        sensorsTextViews[2] = (TextView)findViewById(R.id.humLabelText);

        initAllView();

/*
        try
        {
            //Phidget.enableLogging(Phidget.PHIDGET_LOG_VERBOSE, "/Removable/USBdisk2/logfile.log");
            com.phidgets.usb.Manager.Initialize(this);
            ik = new InterfaceKitPhidget();
            ik.addAttachListener(new AttachListener() {
                public void attached(final AttachEvent ae) {
                    AttachDetachRunnable handler = new AttachDetachRunnable(ae.getSource(), true);
                    synchronized(handler)
                    {
                        runOnUiThread(handler);
                        try {
                            handler.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            ik.addDetachListener(new DetachListener() {
                public void detached(final DetachEvent ae) {
                    AttachDetachRunnable handler = new AttachDetachRunnable(ae.getSource(), false);
                    synchronized(handler)
                    {
                        runOnUiThread(handler);
                        try {
                            handler.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            ik.addSensorChangeListener(new SensorChangeListener() {
                public void sensorChanged(SensorChangeEvent se) {
                    runOnUiThread(new SensorChangeRunnable(se.getIndex(), se.getValue()));
                }
            });

            ik.openAny();


        }
        catch (PhidgetException pe)
        {
            pe.printStackTrace();
        }
        */
    }


/*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
           ik.close();
        } catch (PhidgetException e) {
            e.printStackTrace();
        }
 //       com.phidgets.usb.Manager.Uninitialize();
    }


    class AttachDetachRunnable implements Runnable {
        Phidget phidget;
        boolean attach;
        public AttachDetachRunnable(Phidget phidget, boolean attach)
        {
            this.phidget = phidget;
            this.attach = attach;
        }
        public void run() {
            ImageView phidgetStatusImg = (ImageView) findViewById(R.id.phidgetStatusImg);
            if(attach)
            {
                phidgetStatusImg.setImageResource(R.drawable.connected);
            }
            else
                phidgetStatusImg.setImageResource(R.drawable.disconnected);
            //notify that we're done
            synchronized(this)
            {
                this.notify();
            }
        }
    }

    class SensorChangeRunnable implements Runnable {
        int sensorIndex, sensorVal;

        public SensorChangeRunnable(int index, int val)
        {
            this.sensorIndex = index;
            this.sensorVal = val;

        }
        public void run() {

            if(sensorsTextViews[sensorIndex]!=null) {

                plantStatus.addValue(sensorIndex,sensorVal);
                updateSensorView(sensorIndex);
                updatePlantStatusView();
            }
        }
    }

*/

    // Update the status message for the plant
    public void updatePlantStatusView() {
        TextView plantStatusText = (TextView) findViewById(R.id.plantStatusMessage);
        if(plantStatus.plantIsOK()) {
            plantStatusText.setText(R.string.plantStatusMessageOK);
        }
        else{
            plantStatusText.setText(R.string.plantStatusMessageKO);
        }
    }

    // Update the status image of currSensor
    public void updateSensorView(int currSensor){

        ImageView sensorStatus;

        // Select the current sensor's image
        if(currSensor == PlantStatus.TEMP)
            sensorStatus = (ImageView) findViewById(R.id.tempStatusImg);
        else if(currSensor == PlantStatus.LIGHT)
            sensorStatus = (ImageView) findViewById(R.id.lightStatusImg);
        else
            sensorStatus = (ImageView) findViewById(R.id.humStatusImg);

        // Set the current sensor's image according to the new status
        if(plantStatus.sensorIsOK(currSensor))
            sensorStatus.setImageResource(R.drawable.green_led);
        else
            sensorStatus.setImageResource(R.drawable.red_led);
    }



    // Initialize all sensors status when the activity is created
    public void initAllView(){
        updateSensorView(PlantStatus.TEMP);
        updateSensorView(PlantStatus.LIGHT);
        updateSensorView(PlantStatus.HUM);
        updatePlantStatusView();
    }


    // Refresh the view if the user changed the required values for the plant
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                Bundle extras = data.getExtras();
                if (extras != null) {
                    plantStatus = (PlantStatus) extras.getSerializable("VALUE");
                    updateSensorView(extras.getInt("SENSOR"));
                    updatePlantStatusView();
                }
            }
            if (resultCode == RESULT_CANCELED) {
                //Do nothing?
            }
        }
    }

    private void createDetailsIntent(int sensor, PlantStatus value){
        Intent intent = new Intent(MainActivity.this, SensorDetailActivity.class);
        intent.putExtra("SENSOR", sensor);
        intent.putExtra("VALUE", value);
        startActivityForResult(intent,1);
    }

    // Intent methods, called when the user touch a specific sensor
    public void tempDetail(View view)
    {
        createDetailsIntent(PlantStatus.TEMP, plantStatus);
    }

    public void lightDetail(View view)
    {
        createDetailsIntent(PlantStatus.LIGHT, plantStatus);
    }

    public void humDetail(View view)
    {
        createDetailsIntent(PlantStatus.HUM, plantStatus);
    }


/*

    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(getApplicationContext(), "Service is disconnected", Toast.LENGTH_LONG).show();
            //mBounded = false;
            mServer = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(getApplicationContext(), "Service is connected", Toast.LENGTH_LONG).show();
            mBounded = true;
            PlantDataService.LocalBinder mLocalBinder = (PlantDataService.LocalBinder)service;
            mServer = mLocalBinder.getServerInstance();
        }
    };
*/
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}