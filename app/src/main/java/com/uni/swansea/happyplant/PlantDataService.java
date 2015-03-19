package com.uni.swansea.happyplant;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
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

    Handler handler;

    private int NOTIFICATION = 11;
    private Handler mHandler;

    public InterfaceKitPhidget ik;
    private boolean isAttached;

    PlantCurrentStatus plantCurrentStatus;
    PlantCurrentStatus oldPlantCurrentStatus;

    PlantStatusData[] oldPlantStatusData;
    PlantStatusData[] newPlantStatusData;

    private IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
    }

    private Runnable myTask = new Runnable() {
        @Override
        public void run() {

            Log.d("PlantDataService", "Logging data in service");
            if(isAttached){//Phidget attached
                for( int sensor=0; sensor<3; sensor++) {
                    try {
                        int value = ik.getSensorValue(sensor);
                        //value = PhidgeMetaInfo.convertValue(sensor, value);
                        //value = PhidgeMetaInfo.filterValue(value, oldPlantStatusData[sensor].getValue());
                        newPlantStatusData[sensor] = new PlantStatusData(sensor, value, new Date());
                        dHandler.addStatusData(newPlantStatusData[sensor]);
                        Log.d("PlantDataService", "Is attached logging: " + sensor);
                    } catch(PhidgetException pException){
                        Log.d("PlantDataService", pException.toString());
                    }
                }
            } else if(true) {
                for( int sensor=0; sensor<3; sensor++) {
                    // Fake data - comment when using the phidget values
                    int fakeValue;
                    fakeValue = (int) Math.round(Math.random() * 30);
                    newPlantStatusData[sensor] = new PlantStatusData(sensor, fakeValue, new Date());
                    dHandler.addStatusData(newPlantStatusData[sensor]);
                    Log.d("PlantDataService", "!!!--Isn't attached logging: " + sensor + "--!!!");
                    // end fake data
                }
            }

            // Update the plantCurrentStatus with the last read value
            plantCurrentStatus.setGeneralPlantStatusData(newPlantStatusData);

            showValuesInNotifcation();

            sendMessage();
            mHandler.postDelayed(this, 1000);
        }
    };

    public void showValuesInNotifcation(){
        //check each sensor against function
        int i = 0;
        String statusText = "";
        while(i < 3){
            statusText += getSensorBoundsString(i);
            if(plantCurrentStatus.sensorIsInBounds(i) != 0){
                break;
            }
            i++;
        }
        showNotification(statusText);
    }

    public String getSensorBoundsString(int sensor){
        int sensorinBounds = plantCurrentStatus.sensorIsInBounds(sensor);
        if(sensorinBounds > 0){
            return PlantMetaInfo.labels[sensor] + " is too high";
        }
        if(sensorinBounds < 0){
            return PlantMetaInfo.labels[sensor] + " is too low";
        }
        return "";
    }


    private void sendMessage(){
            Intent intent = new Intent();
            intent.setAction("com.uni.swansea.happyplant.MessageReceiver");
            intent.putExtra("CURRSTATUS", plantCurrentStatus);
            intent.putExtra("PHIDGCONN", isAttached);
            sendBroadcast(intent);
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
            Log.d("AttachEvent", "Attached = " + attach);
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

    private void registerPhidget(){

        try
        {
            //Phidget.enableLogging(Phidget.PHIDGET_LOG_VERBOSE, "/Removable/USBdisk2/logfile.log");
            com.phidgets.usb.Manager.Initialize(this);
            ik = new InterfaceKitPhidget();
            ik.addAttachListener(new AttachListener() {
                public void attached(final AttachEvent ae) {
                   AttachDetachRunnable handlerAt = new AttachDetachRunnable(ae.getSource(), true);
                    synchronized(handlerAt)
                    {
                        runOnUiThread(handlerAt);
                        try {
                            handlerAt.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            ik.addDetachListener(new DetachListener() {
                public void detached(final DetachEvent ae) {
                    AttachDetachRunnable handlerDe = new AttachDetachRunnable(ae.getSource(), false);
                    synchronized(handlerDe)
                    {
                        runOnUiThread(handlerDe);
                        try {
                            handlerDe.wait();
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
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    private void showNotification(String details) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Happy Plant")
                        .setContentText(details);


        Intent resultIntent = new Intent(this, MainActivity.class);


        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, mBuilder.build());
    }


    @Override
    public void onCreate() {
        handler = new Handler();

        this.isAttached = false;
        dHandler = PlantDatabaseHandler.getHelper(getApplicationContext());
        plantCurrentStatus = dHandler.getUpdatedPlantCurrentStatus();

        oldPlantStatusData = plantCurrentStatus.getGeneralPlantStatusData();
        newPlantStatusData = plantCurrentStatus.getGeneralPlantStatusData();

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mHandler = new Handler();
        showNotification("Service started");

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

    class SensorChangeRunnable implements Runnable {
        int sensorIndex, sensorVal;

        public SensorChangeRunnable(int index, int val)
        {
            this.sensorIndex = index;
            this.sensorVal = val;

        }
        public void run() {
            Log.d("SensorChangeEvent", "Running the SensorChange");
            //newPlantStatusData[sensorIndex] = new PlantStatusData(sensorIndex, sensorVal, new Date());
            //plantCurrentStatus.setGeneralPlantStatusData(newPlantStatusData);

            //dHandler.addStatusData(newPlantStatusData[sensorIndex]);
            showValuesInNotifcation();
        }
    }
}

