package com.uni.swansea.happyplant;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


public class SensorDetailActivity extends ActionBarActivity {

    public PlantStatus plantStatus;
    public int CURR_SENSOR;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_detail);

        // Check if the intent has extra data
        // and update the plantStatus variable
        if(getDataFromIntent(getIntent())) {
            refreshHeader();
            refreshSensorValues();
        }
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                if(getDataFromIntent(data)){
                    refreshHeader();
                    refreshSensorValues();
                }
            }
            if (resultCode == RESULT_CANCELED) {
                //Do nothing?
            }
        }
    }


    // Get the current sensor values and update the status of the plant
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
        if (CURR_SENSOR == PlantStatus.TEMP)
            sensorLabelText.setText(R.string.tempLabelText);
        else if (CURR_SENSOR == PlantStatus.HUM)
            sensorLabelText.setText(R.string.humLabelText);
        else if (CURR_SENSOR == PlantStatus.LIGHT)
            sensorLabelText.setText(R.string.lightLabelText);
    }

    // Refresh value if they has been changed
    public void refreshSensorValues(){
        TextView sensorValuesText = (TextView) findViewById(R.id.sensorValuesText);
        String temp = getResources().getString(R.string.sensorValuesText);
        temp = temp.replaceFirst("CURR", String.valueOf(plantStatus.getCurrValue(CURR_SENSOR)));
        temp = temp.replaceFirst("MIN", String.valueOf(plantStatus.minReqValues[CURR_SENSOR]));
        temp = temp.replaceFirst("MAX", String.valueOf(plantStatus.maxReqValues[CURR_SENSOR]));
        sensorValuesText.setText(temp);
    }

    public void refreshGraph(){

    }
    
    // Intent for changing required values
    public void editRequiredValue(View view)
    {
        Intent intent = new Intent(SensorDetailActivity.this, EditRequiredValueActivity.class);
        intent.putExtra("SENSOR", CURR_SENSOR);
        intent.putExtra("VALUE", plantStatus);
        startActivityForResult(intent,1);
    }

}
