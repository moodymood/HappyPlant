package com.uni.swansea.happyplant;


import com.phidgets.InterfaceKitPhidget;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class InterfaceKitUSBExampleActivity extends Activity {
    InterfaceKitPhidget ik;

    TextView[] sensorsTextViews;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sensorsTextViews = new TextView[8];
        sensorsTextViews[0] = (TextView)findViewById(R.id.sensor0);
        sensorsTextViews[1] = (TextView)findViewById(R.id.sensor1);
        sensorsTextViews[2] = (TextView)findViewById(R.id.sensor2);



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
                    runOnUiThread(new SensorChangeRunnable(se.getSource(), se.getIndex(), se.getValue()));
                }
            });

            ik.openAny();


        }
        catch (PhidgetException pe)
        {
            pe.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ik.close();
        } catch (PhidgetException e) {
            e.printStackTrace();
        }
        com.phidgets.usb.Manager.Uninitialize();
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
            TextView attachedTxt = (TextView) findViewById(R.id.attachedTxt);
            if(attach)
            {
                attachedTxt.setText("Attached");
                try {
                    TextView nameTxt = (TextView) findViewById(R.id.nameTxt);
                    TextView serialTxt = (TextView) findViewById(R.id.serialTxt);
                    TextView versionTxt = (TextView) findViewById(R.id.versionTxt);
                    TextView labelTxt = (TextView) findViewById(R.id.labelTxt);

                    nameTxt.setText(phidget.getDeviceName());
                    serialTxt.setText(Integer.toString(phidget.getSerialNumber()));
                    versionTxt.setText(Integer.toString(phidget.getDeviceVersion()));
                    labelTxt.setText(phidget.getDeviceLabel());

                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
            }
            else
                attachedTxt.setText("Detached");
            //notify that we're done
            synchronized(this)
            {
                this.notify();
            }
        }
    }

    class SensorChangeRunnable implements Runnable {
        int sensorIndex, sensorVal, serial, pclass;
        String name;
        Phidget p;
        public SensorChangeRunnable(Phidget p, int index, int val)
        {
            this.p = p;
            this.sensorIndex = index;
            this.sensorVal = val;

        }
        public void run() {

            if(sensorsTextViews[sensorIndex]!=null){
                if(sensorIndex==0)
                    sensorsTextViews[sensorIndex].setText("Temperature:" + sensorVal);
                else if(sensorIndex==1)
                    sensorsTextViews[sensorIndex].setText("Light:" + sensorVal);
                if(sensorIndex==2)
                    sensorsTextViews[sensorIndex].setText("Humidity:" + sensorVal);
            }
        }
    }


    public void goToMain(View view)
    {
        Intent intent = new Intent(this, SensorListActivity.class);
        startActivity(intent);
    }
}