package com.uni.swansea.happyplant;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;


public class EditRequiredValueActivity extends ActionBarActivity {

    private PlantDatabaseHandler dHandler;
    private MessageReceiver messageReceiver;

    private int CURR_SENSOR;
    private PlantCurrentStatus plantCurrentStatus;

    private NumberPicker minNumberPicker, maxNumberPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_required_value);

        // Set the CURR_VALUE taking the info from the intent
        CURR_SENSOR = getCurrSensorFromIntent(getIntent());

        dHandler = PlantDatabaseHandler.getHelper(getApplicationContext());

        // Update the plantCurrentStatus and refresh all views (need to be the first thing)
        plantCurrentStatus = dHandler.getUpdatedPlantCurrentStatus();
        refreshHeader();
        refreshEditValues();

    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(messageReceiver);
    }


    protected void onStart() {
        super.onStart();
        addCustomReceiver();
    }


    private void addCustomReceiver() {
        messageReceiver = new MessageReceiver(){
            @Override
            protected void onMessageReceived(){
                plantCurrentStatus.setGeneralPlantStatusData(this.getBroadcastCurrentStatus().getGeneralPlantStatusData());
                refreshHeader();
            }
        };

        IntentFilter intentFilter = new IntentFilter("com.uni.swansea.happyplant.MessageReceiver");
        intentFilter.setPriority(10);
        this.registerReceiver(messageReceiver,intentFilter);
    }


    public void saveNewValues(View view){

        NumberPicker minNumberPicker = (NumberPicker) findViewById(R.id.minReqValue);
        NumberPicker maxNumberPicker = (NumberPicker) findViewById(R.id.maxReqValue);

        PlantDataRange newPlantDataRange = new PlantDataRange(CURR_SENSOR, CURR_SENSOR, minNumberPicker.getValue(), maxNumberPicker.getValue());
        plantCurrentStatus.getGeneralPlantDataRange(CURR_SENSOR).setMinValue(newPlantDataRange.getMinValue());
        plantCurrentStatus.getGeneralPlantDataRange(CURR_SENSOR).setMaxValue(newPlantDataRange.getMaxValue());
        dHandler.addRangeValues(newPlantDataRange);

        sendMessageUpdateRange();

        finish();
    }



    private void sendMessageUpdateRange(){
        Intent serviceIntent = new Intent(PlantDataService.class.getName());
        serviceIntent.putExtra("CURRSTATUS", plantCurrentStatus);
        startService(serviceIntent);
    }


    // Refresh header if values has been changed
    public void refreshHeader(){

        ImageView sensorStatusImg = (ImageView) findViewById(R.id.sensorStatusImg);
        if (plantCurrentStatus.sensorIsOK(CURR_SENSOR))
            sensorStatusImg.setImageResource(R.drawable.green_led);
        else
            sensorStatusImg.setImageResource(R.drawable.red_led);

        TextView sensorLabelText = (TextView) findViewById(R.id.sensorLabelText);
        sensorLabelText.setText(PlantMetaInfo.labels[CURR_SENSOR]);
    }


    public void refreshEditValues() {
        int phiMinValue = PhidgeMetaInfo.minValues[CURR_SENSOR];
        int phiMaxValue = PhidgeMetaInfo.maxValues[CURR_SENSOR];
        int currMinValue = plantCurrentStatus.getGeneralPlantDataRange(CURR_SENSOR).getMinValue();
        int currMaxValue = plantCurrentStatus.getGeneralPlantDataRange(CURR_SENSOR).getMaxValue();

        minNumberPicker = (NumberPicker) findViewById(R.id.minReqValue);
        minNumberPicker.setMaxValue(currMaxValue);
        minNumberPicker.setMinValue(phiMinValue);
        minNumberPicker.setValue(currMinValue);
        minNumberPicker.setOnValueChangedListener( new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                maxNumberPicker.setMinValue(newVal);
            }
        });


        maxNumberPicker = (NumberPicker) findViewById(R.id.maxReqValue);
        maxNumberPicker.setMaxValue(phiMaxValue);
        maxNumberPicker.setMinValue(currMinValue);
        maxNumberPicker.setValue(currMaxValue);
        maxNumberPicker.setOnValueChangedListener( new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                minNumberPicker.setMaxValue(newVal);
            }
        });

        TextView minUnitText = (TextView) findViewById(R.id.unitMinText);
        minUnitText.setText(PlantMetaInfo.unit[CURR_SENSOR]);

        TextView maxUnitText = (TextView) findViewById(R.id.unitMaxText);
        maxUnitText.setText(PlantMetaInfo.unit[CURR_SENSOR]);
    }


    public int getCurrSensorFromIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null)
            return (int) extras.get("SENSOR");
        else
            return -1;
    }

}
