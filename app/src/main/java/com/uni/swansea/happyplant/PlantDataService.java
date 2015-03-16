package com.uni.swansea.happyplant;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
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

import java.util.Date;


/**
 * Created by michaelwaterworth on 10/03/15.
 */
public class PlantDataService extends Service{
    private NotificationManager mNM;
    PlantDatabaseHandler dHandler;

    private int NOTIFICATION = 11;
    private Handler mHandler;

    public InterfaceKitPhidget ik;
    private boolean isAttached;

    PlantCurrentStatus plantCurrentStatus;
    PlantCurrentStatus oldPlantCurrentStatus;


    private IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public PlantDataService getServerInstance() {
            return PlantDataService.this;
        }
    }

    private Runnable myTask = new Runnable() {
        @Override
        public void run() {

            PlantStatusData[] oldPlantStatusData = plantCurrentStatus.getGeneralPlantStatusData();
            PlantStatusData[] newPlantStatusData = plantCurrentStatus.getGeneralPlantStatusData();

            // Fake data - comment when using the phidget values
            int fakeValue;
            fakeValue = (int) Math.round(Math.random() * 20) + 400;
            fakeValue = PhidgeMetaInfo.convertValue(PhidgeMetaInfo.TEMP, fakeValue);
            fakeValue = PhidgeMetaInfo.filterValue(fakeValue, oldPlantStatusData[PhidgeMetaInfo.TEMP].getValue());
            newPlantStatusData[PhidgeMetaInfo.TEMP] = new PlantStatusData(PhidgeMetaInfo.TEMP, fakeValue, new Date());

            fakeValue = (int) Math.round(Math.random() * 20) + 450;
            fakeValue = PhidgeMetaInfo.convertValue(PhidgeMetaInfo.HUM, fakeValue);
            fakeValue = PhidgeMetaInfo.filterValue(fakeValue, oldPlantStatusData[PhidgeMetaInfo.HUM].getValue());
            newPlantStatusData[PhidgeMetaInfo.HUM] = new PlantStatusData(PhidgeMetaInfo.HUM, fakeValue, new Date());

            fakeValue = (int) Math.round(Math.random() * 20) + 500;
            fakeValue = PhidgeMetaInfo.convertValue(PhidgeMetaInfo.LIGHT, fakeValue);
            fakeValue = PhidgeMetaInfo.filterValue(fakeValue, oldPlantStatusData[PhidgeMetaInfo.LIGHT].getValue());
            newPlantStatusData[PhidgeMetaInfo.LIGHT] = new PlantStatusData(PhidgeMetaInfo.LIGHT, fakeValue, new Date());

            dHandler.addStatusData( newPlantStatusData[PhidgeMetaInfo.TEMP]);
            dHandler.addStatusData( newPlantStatusData[PhidgeMetaInfo.HUM]);
            dHandler.addStatusData( newPlantStatusData[PhidgeMetaInfo.LIGHT]);
            // end fake data

            Log.d("PlantDataService", "Logging data in service");
            if(isAttached){//Phidget attached
                for( int sensor=0; sensor<3; sensor++) {
                    try {
                        int value = ik.getSensorValue(sensor);
                        value = PhidgeMetaInfo.convertValue(sensor, value);
                        value = PhidgeMetaInfo.filterValue(value, oldPlantStatusData[sensor].getValue());
                        newPlantStatusData[sensor] = new PlantStatusData(sensor, value, new Date());
                        dHandler.addStatusData(newPlantStatusData[sensor]);

                    } catch(PhidgetException pException){
                        Log.d("PlantDataService", pException.toString());
                    }
                }
            }

            // Update the plantCurrentStatus with the last read value
            plantCurrentStatus.setGeneralPlantStatusData(newPlantStatusData);

            sendMessage();
            mHandler.postDelayed(this, 1000);
        }
    };


    private void sendMessage(){
        Intent intent = new Intent();
        intent.setAction("com.uni.swansea.happyplant.MessageReceiver");
        intent.putExtra("CURRSTATUS", plantCurrentStatus);
        intent.putExtra("PHIDGCONN", isAttached);
        sendBroadcast(intent);
    }


    private void registerPhidget(){
        class AttachDetachRunnable implements Runnable {
            Phidget phidget;
            boolean attach;
            public AttachDetachRunnable(Phidget phidget, boolean attach)
            {
                this.phidget = phidget;
                this.attach = attach;
            }
            public void run() {
                if(attach)
                {
                    isAttached = true;
                }
                else {
                    isAttached = false;
                }

                synchronized(this)
                {
                    this.notify();
                }
            }
        }


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
                        handler.run();
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
                        handler.run();
                        try {
                            handler.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
//            ik.addSensorChangeListener(new SensorChangeListener() {
//                public void sensorChanged(SensorChangeEvent se) {
//                    runOnUiThread(new SensorChangeRunnable(se.getIndex(), se.getValue()));
//                }
//            });
            ik.openAny();


        }
        catch (PhidgetException pe)
        {
            pe.printStackTrace();
        }
    }


    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "Service Running";

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.ic_launcher, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        //getText(R.string.local_service_label)
        notification.setLatestEventInfo(this, "HappyPlant",
                text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }


    @Override
    public void onCreate() {

        this.isAttached = false;
        dHandler = PlantDatabaseHandler.getHelper(getApplicationContext());
        plantCurrentStatus = dHandler.getUpdatedPlantCurrentStatus();

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mHandler = new Handler();
        showNotification();

        registerPhidget();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this, "Plant Data Service Started", Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(myTask, 1000);
        Log.d("PlantDataService", "onStart");
        Bundle extras = intent.getExtras();
        if (extras != null) {
            plantCurrentStatus = (PlantCurrentStatus) extras.getSerializable("CURRSTATUS");
        }
        return flags;
    }


    @Override
    public void onDestroy() {
        mNM.cancel(NOTIFICATION);
        Toast.makeText(this, "Plant Data Service Stopped", Toast.LENGTH_SHORT).show();
        mHandler.removeCallbacks(myTask);

        try {
            ik.close();
        } catch (PhidgetException e) {
            e.printStackTrace();
        }
        com.phidgets.usb.Manager.Uninitialize();
        Log.d("PlantDataService", "onDestroy");
    }
}