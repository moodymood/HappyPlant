package com.uni.swansea.happyplant;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class EditRequiredValueActivity extends ActionBarActivity {

    public PlantStatus plantStatus;
    public int CURR_SENSOR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_required_value);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            CURR_SENSOR = (int) extras.get("SENSOR");
            plantStatus = (PlantStatus) extras.getSerializable("VALUE");
        }

        ImageView sensorStatusImg = (ImageView) findViewById(R.id.sensorStatusImg);
        if(plantStatus.sensorIsOK(CURR_SENSOR))
            sensorStatusImg.setImageResource(R.drawable.green_led);
        else
            sensorStatusImg.setImageResource(R.drawable.red_led);

        TextView sensorLabelText = (TextView) findViewById(R.id.sensorLabelText);
        if(CURR_SENSOR == PlantStatus.TEMP)
            sensorLabelText.setText(R.string.tempLabelText);
        else if(CURR_SENSOR == PlantStatus.HUM)
            sensorLabelText.setText(R.string.humLabelText);
        else if(CURR_SENSOR == PlantStatus.LIGHT)
            sensorLabelText.setText(R.string.lightLabelText);

        EditText minReqValueText = (EditText) findViewById(R.id.minReqValue);
        minReqValueText.setText(String.valueOf(plantStatus.minReqValues[CURR_SENSOR]));

        EditText maxReqValueText = (EditText) findViewById(R.id.maxReqValue);
        maxReqValueText.setText(String.valueOf(plantStatus.maxReqValues[CURR_SENSOR]));

    }



    public void saveNewValues(View view){
        EditText newMinReqValueText = (EditText) findViewById(R.id.minReqValue);
        plantStatus.minReqValues[CURR_SENSOR] = Integer.parseInt(newMinReqValueText.getText().toString());

        EditText newMaxReqValueText = (EditText) findViewById(R.id.maxReqValue);
        plantStatus.maxReqValues[CURR_SENSOR] = Integer.parseInt(newMaxReqValueText.getText().toString());


        Intent returnIntent = new Intent();
        returnIntent.putExtra("SENSOR", CURR_SENSOR);
        returnIntent.putExtra("VALUE", plantStatus);
        setResult(RESULT_OK,returnIntent);
        finish();

    }
}
