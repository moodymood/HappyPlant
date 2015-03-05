package com.uni.swansea.happyplant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

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


/**
 * An activity representing a single Sensor detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link SensorListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link SensorDetailFragment}.
 */
public class SensorDetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_detail);


        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(SensorDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(SensorDetailFragment.ARG_ITEM_ID));
            SensorDetailFragment fragment = new SensorDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.sensor_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, SensorListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class InterfaceKitUSBExampleActivity extends Activity {
        InterfaceKitPhidget ik;

        TextView[] sensorsTextViews;
        CheckBox[] inputCheckBoxes;

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);

            sensorsTextViews = new TextView[8];
            sensorsTextViews[0] = (TextView)findViewById(R.id.sensor0);
            sensorsTextViews[1] = (TextView)findViewById(R.id.sensor1);
            sensorsTextViews[2] = (TextView)findViewById(R.id.sensor2);

            inputCheckBoxes = new CheckBox[8];


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
                ik.addInputChangeListener(new InputChangeListener() {
                    public void inputChanged(InputChangeEvent ie) {
                        runOnUiThread(new InputChangeRunnable(ie.getIndex(), ie.getState()));
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

        class OnOutputClickListener implements CheckBox.OnClickListener {
            InterfaceKitPhidget phidget;
            int index = 0;
            public OnOutputClickListener(InterfaceKitPhidget phidget, int index)
            {
                this.phidget = phidget;
                this.index = index;
            }
            @Override
            public void onClick(View v) {
                try {
                    if(phidget.isAttached()){
                        // Perform action on clicks, depending on whether it's now checked
                        if (((CheckBox) v).isChecked()) {
                            phidget.setOutputState(index, true);
                        } else {
                            phidget.setOutputState(index, false);
                        }
                    }
                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
            }

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
                try {
                    this.name = p.getDeviceName();
                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
                try {
                    this.serial = p.getSerialNumber();
                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
                try {
                    this.pclass = p.getDeviceClass();
                } catch (PhidgetException e) {
                    e.printStackTrace();
                }
            }
            public void run() {

                if(sensorsTextViews[sensorIndex]!=null)
                    sensorsTextViews[sensorIndex].setText(sensorIndex +"-a-"+sensorVal+"-b-"+name+"-c-"+serial+"-d-"+pclass);
            }
        }

        class InputChangeRunnable implements Runnable {
            int index;
            boolean val;
            public InputChangeRunnable(int index, boolean val)
            {
                this.index = index;
                this.val = val;
            }
            public void run() {
                if(inputCheckBoxes[index]!=null)
                    inputCheckBoxes[index].setChecked(val);
            }
        }
    }
}
