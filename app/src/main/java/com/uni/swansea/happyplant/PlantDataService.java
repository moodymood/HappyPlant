package com.uni.swansea.happyplant;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
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

            // Fake data
            PlantStatusData[] newPlantStatusData = new PlantStatusData[3];
            newPlantStatusData[0] = new PlantStatusData(0, (int) Math.round(Math.random() * 100), new Date());
            newPlantStatusData[1] = new PlantStatusData(1, (int) Math.round(Math.random() * 100), new Date());
            newPlantStatusData[2] = new PlantStatusData(2, (int) Math.round(Math.random() * 100), new Date());

            Log.d("PlantDataService", "Logging data in service");
            if(isAttached){//Phidget attached
                for( int i=0; i<3; i++) {
                    try {
                        int value = ik.getSensorValue(i);
                        newPlantStatusData[i] = new PlantStatusData(i, value, new Date());

                    } catch(PhidgetException pException){
                        Log.d("PlantDataService", pException.toString());
                    }
                }
            }


            for(int i=0; i<3; i++)
                dHandler.addStatusData(newPlantStatusData[i]);


            sendMessage(newPlantStatusData);
            mHandler.postDelayed(this, 1000);
        }
    };

    private void sendMessage(PlantStatusData[] newPlantStatusData){
        PlantCurrentStatus plantCurrentStatus = new PlantCurrentStatus(newPlantStatusData, null);

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
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mHandler = new Handler();
        showNotification();

        registerPhidget();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Toast.makeText(this, "Plant Data Service Started", Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(myTask, 1000);
        Log.d("PlantDataService", "onStart");
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