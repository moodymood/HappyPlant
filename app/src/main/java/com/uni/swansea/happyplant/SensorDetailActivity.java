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

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class SensorDetailActivity extends ActionBarActivity {

    MessageReceiver messageReceiver;
    public PlantStatus plantStatus;
    public int CURR_SENSOR;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_detail);

        messageReceiver = new MessageReceiver();

        // Check if the intent has extra data
        // and update the plantStatus variable
        if(getDataFromIntent(getIntent())) {
            refreshHeader();
            refreshSensorValues();
            refreshGraph();
        }
    }


    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("SENSOR", CURR_SENSOR);
        returnIntent.putExtra("VALUE", plantStatus);
        setResult(RESULT_OK, returnIntent);
        finish();
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
        sensorLabelText.setText(plantStatus.labels[CURR_SENSOR]);
    }

    // Refresh value if they has been changed
    public void refreshSensorValues(){

        TextView sensorCurrValuesText = (TextView) findViewById(R.id.sensorCurrValuesText);
        String temp = getResources().getString(R.string.sensorCurrValuesText);
        temp = temp.replace("CURR", String.valueOf(plantStatus.getCurrValue(CURR_SENSOR)));
        temp = temp.replace("-UNIT-", plantStatus.unit[CURR_SENSOR]);
        sensorCurrValuesText.setText(temp);

        TextView sensorReqValuesText = (TextView) findViewById(R.id.sensorReqValuesText);
        temp = getResources().getString(R.string.sensorReqValuesText);
        temp = temp.replace("MIN", String.valueOf(plantStatus.minReqValues[CURR_SENSOR]));
        temp = temp.replace("MAX", String.valueOf(plantStatus.maxReqValues[CURR_SENSOR]));
        temp = temp.replace("-UNIT-", plantStatus.unit[CURR_SENSOR]);
        sensorReqValuesText.setText(temp);


    }

    public void refreshGraph(){

        int todayHours = plantStatus.getCurrentHour();
        int yesterdayHours = 24 - todayHours;
        int[] sensorValues = plantStatus.sensorsMap.get(CURR_SENSOR);


        GraphView graph1 = (GraphView) findViewById(R.id.graph1);
        GraphView graph2 = (GraphView) findViewById(R.id.graph2);

        DataPoint[] dp1 = new DataPoint[yesterdayHours];
        DataPoint[] dp2 = new DataPoint[todayHours];



        for(int i = 0; i<yesterdayHours; i++){
            dp1[i] = new DataPoint(i+todayHours,sensorValues[i+todayHours]);
        }

        for(int i = 0; i<todayHours; i++){
            dp2[i] = new DataPoint(i,sensorValues[i]);
        }


        LineGraphSeries<DataPoint> series1 = new LineGraphSeries<>(dp1);
        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(dp2);

        graph1.getGridLabelRenderer().setNumHorizontalLabels(24);
        graph1.getGridLabelRenderer().setTextSize(25);
        graph1.getViewport().setXAxisBoundsManual(true);
        graph1.getViewport().setMinX(0);
        graph1.getViewport().setMaxX(23);

        graph2.getGridLabelRenderer().setNumHorizontalLabels(24);
        graph2.getGridLabelRenderer().setTextSize(25);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(23);



        graph1.setTitle("Yesterday");
        graph2.setTitle("Today");

        graph1.addSeries(series1);
        graph2.addSeries(series2);
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
