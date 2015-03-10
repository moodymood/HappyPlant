package com.uni.swansea.happyplant;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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


    public InterfaceKitPhidget ik;
    public PlantStatus plantStatus;
    public TextView[] sensorsTextViews;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        plantStatus = new PlantStatus();




        sensorsTextViews = new TextView[8];
        sensorsTextViews[0] = (TextView)findViewById(R.id.tempLabelText);
        sensorsTextViews[1] = (TextView)findViewById(R.id.lightLabelText);
        sensorsTextViews[2] = (TextView)findViewById(R.id.humLabelText);

        initAllView();


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
                plantStatus.addValue(sensorIndex,sensorVal);
                updateSensorView(sensorIndex);
                updatePlantStatusView();
            }
        }
    }



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


    // Intent methods, called when the user touch a specific sensor
    public void tempDetail(View view)
    {

        Intent intent = new Intent(MainActivity.this, SensorDetailActivity.class);
        intent.putExtra("SENSOR", PlantStatus.TEMP);
        intent.putExtra("VALUE", plantStatus);
        startActivityForResult(intent,1);
    }

    public void lightDetail(View view)
    {
        Intent intent = new Intent(MainActivity.this, SensorDetailActivity.class);
        intent.putExtra("SENSOR", PlantStatus.LIGHT);
        intent.putExtra("VALUE", plantStatus);
        startActivityForResult(intent,1);
    }

    public void humDetail(View view)
    {
        Intent intent = new Intent(MainActivity.this, SensorDetailActivity.class);
        intent.putExtra("SENSOR", PlantStatus.HUM);
        intent.putExtra("VALUE", plantStatus);
        startActivityForResult(intent,1);
    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("Pausing");
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("Stopping");
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("Resuming");
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("Starting");
    }
}