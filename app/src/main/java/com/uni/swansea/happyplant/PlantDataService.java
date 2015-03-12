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


    IBinder mBinder = new LocalBinder();

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
            dHandler.addStatusData(new PlantStatusData(1, (int) Math.round(Math.random() * 100), new Date()));

            Log.d("PlantDataService", "Logging data in service");
            if(isAttached){//Phidget attached
                int i = 0;
                while(i < 3) {
                    try {
                        ik.getSensorValue(i);
                        i++;
                    } catch(PhidgetException pException){
                        Log.d("PlantDataService", pException.toString());
                    }
                }
            }
            //do work
            sendMessage();
            mHandler.postDelayed(this, 1000);
        }
    };

    private void sendMessage(){
        Intent i = new Intent("NEWMESSAGE");
        i.putExtra("test", "sample");
        sendBroadcast(i);
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
                //ImageView phidgetStatusImg = (ImageView) findViewById(R.id.phidgetStatusImg);
                if(attach)
                {
                    isAttached = true;
                    //phidgetStatusImg.setImageResource(R.drawable.connected);
                }
                else {
                    isAttached = false;
                }
                // phidgetStatusImg.setImageResource(R.drawable.disconnected);
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

                //if(sensorsTextViews[sensorIndex]!=null) {
                /*
                if (sensorIndex == 0) {
                    sensorsTextViews[sensorIndex].setText("Temperature:" + plantStatus.getCurrValue(sensorIndex));
                    plantStatus.addValue(0, sensorVal);
                } else if (sensorIndex == 1) {
                    sensorsTextViews[sensorIndex].setText("Light:" + plantStatus.getCurrValue(sensorIndex));
                    plantStatus.addValue(1, sensorVal);
                }
                if (sensorIndex == 2) {
                    sensorsTextViews[sensorIndex].setText("Humidity:" + plantStatus.getCurrValue(sensorIndex));
                    plantStatus.addValue(2, sensorVal);
                }
                */
//                    plantStatus.addValue(sensorIndex,sensorVal);
//                    updateSensorView(sensorIndex);
//                    updatePlantStatusView();
//                }
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
//        Toast.makeText(this, "Congrats! MyService Created", Toast.LENGTH_LONG).show();
        Log.d("PlantDataService", "onCreate");
        mHandler = new Handler();
        showNotification();
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
        Log.d("PlantDataService", "onDestroy");
    }
}