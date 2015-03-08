package com.uni.swansea.happyplant;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;


public class EditRequiredValueActivity extends ActionBarActivity {

    public PlantStatus plantStatus;
    public int CURR_SENSOR;
    NumberPicker minNumberPicker, maxNumberPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_required_value);

        // Check if the intent has extra data
        // and update the plantStatus variable
        if(getDataFromIntent(getIntent())) {
            refreshHeader();
            refreshEditValues();
        }
    }



    public void saveNewValues(View view){
        NumberPicker minNumberPicker = (NumberPicker) findViewById(R.id.minReqValue);
        plantStatus.minReqValues[CURR_SENSOR] = minNumberPicker.getValue();

        NumberPicker maxNumberPicker = (NumberPicker) findViewById(R.id.maxReqValue);
        plantStatus.maxReqValues[CURR_SENSOR] = maxNumberPicker.getValue();


        Intent returnIntent = new Intent();
        returnIntent.putExtra("SENSOR", CURR_SENSOR);
        returnIntent.putExtra("VALUE", plantStatus);
        setResult(RESULT_OK,returnIntent);
        finish();

    }


    public boolean getDataFromIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            CURR_SENSOR = (int) extras.get("SENSOR");
            plantStatus = (PlantStatus) extras.getSerializable("VALUE");
            return true;
        }
        else
            return false;
    }


    // Refresh header if values has been changed
    public void refreshHeader(){
        ImageView sensorStatusImg = (ImageView) findViewById(R.id.sensorStatusImg);
        if (plantStatus.sensorIsOK(CURR_SENSOR))
            sensorStatusImg.setImageResource(R.drawable.green_led);
        else
            sensorStatusImg.setImageResource(R.drawable.red_led);

        TextView sensorLabelText = (TextView) findViewById(R.id.sensorLabelText);
        sensorLabelText.setText(plantStatus.labels[CURR_SENSOR]);
    }

    public void refreshEditValues() {
        minNumberPicker = (NumberPicker) findViewById(R.id.minReqValue);
        minNumberPicker.setMaxValue(plantStatus.maxReqValues[CURR_SENSOR]);
        minNumberPicker.setMinValue(0);
        minNumberPicker.setValue(plantStatus.minReqValues[CURR_SENSOR]);
        minNumberPicker.setOnValueChangedListener( new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                maxNumberPicker.setMinValue(newVal);
            }
        });

        maxNumberPicker = (NumberPicker) findViewById(R.id.maxReqValue);
        maxNumberPicker.setMaxValue(100);
        maxNumberPicker.setMinValue(plantStatus.minReqValues[CURR_SENSOR]);
        maxNumberPicker.setValue(plantStatus.maxReqValues[CURR_SENSOR]);
        maxNumberPicker.setOnValueChangedListener( new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                minNumberPicker.setMaxValue(newVal);
            }
        });


        TextView minUnitText = (TextView) findViewById(R.id.unitMinText);
        minUnitText.setText(plantStatus.unit[CURR_SENSOR]);

        TextView maxUnitText = (TextView) findViewById(R.id.unitMaxText);
        maxUnitText.setText(plantStatus.unit[CURR_SENSOR]);
    }
}
