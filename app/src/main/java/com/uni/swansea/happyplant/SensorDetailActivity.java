package com.uni.swansea.happyplant;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class SensorDetailActivity extends ActionBarActivity {

    private PlantDatabaseHandler dHandler;
    private MessageReceiver messageReceiver;

    private int CURR_SENSOR;
    private PlantCurrentStatus plantCurrentStatus;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_detail);

        // Set the CURR_VALUE taking the info from the intent
        CURR_SENSOR = getCurrSensorFromIntent(getIntent());

        dHandler = PlantDatabaseHandler.getHelper(getApplicationContext());

        // Update the plantCurrentStatus and refresh all views (need to be the first thing)
        plantCurrentStatus = dHandler.getUpdatedPlantCurrentStatus();
        refreshHeader();
        refreshSensorValues();
        refreshGraph();
    }


    protected void onPause() {
        super.onPause();
        unregisterReceiver(messageReceiver);
    }


    protected void onStart() {
        super.onStart();
        addCustomReceiver();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Data must be refreshed from the db
        plantCurrentStatus = dHandler.getUpdatedPlantCurrentStatus();
        refreshHeader();
        refreshSensorValues();
        refreshGraph();
    }


    private void addCustomReceiver() {
        messageReceiver = new MessageReceiver(){
            @Override
            protected void onMessageReceived(){
                plantCurrentStatus.setGeneralPlantStatusData(this.getBroadcastCurrentStatus().getGeneralPlantStatusData());
                refreshHeader();
                refreshSensorValues();
                // Better not to refresh the time everytime?
                //refreshGraph();
            }
        };

        IntentFilter intentFilter = new IntentFilter("com.uni.swansea.happyplant.MessageReceiver");
        intentFilter.setPriority(10);
        this.registerReceiver(messageReceiver,intentFilter);
    }


    // Get the current sensor values from the intent and update CURR_SENSOR
    public int getCurrSensorFromIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null)
            return CURR_SENSOR = (int) extras.get("SENSOR");
        else
            return -1;
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


    // Refresh value if they has been changed
    public void refreshSensorValues(){

        TextView sensorCurrValuesText = (TextView) findViewById(R.id.sensorCurrValuesText);
        String temp = getResources().getString(R.string.sensorCurrValuesText);
        temp = temp.replace("CURR", String.valueOf(plantCurrentStatus.getGeneralPlantStatusData(CURR_SENSOR).getValue()));
        temp = temp.replace("-UNIT-", PlantMetaInfo.unit[CURR_SENSOR]);
        sensorCurrValuesText.setText(temp);

        TextView sensorReqValuesText = (TextView) findViewById(R.id.sensorReqValuesText);
        temp = getResources().getString(R.string.sensorReqValuesText);
        temp = temp.replace("MIN", String.valueOf(plantCurrentStatus.getGeneralPlantDataRange(CURR_SENSOR).getMinValue()));
        temp = temp.replace("MAX", String.valueOf(plantCurrentStatus.getGeneralPlantDataRange(CURR_SENSOR).getMaxValue()));
        temp = temp.replace("-UNIT-", PlantMetaInfo.unit[CURR_SENSOR]);
        sensorReqValuesText.setText(temp);
    }


    public void refreshGraph(){

        List<PlantStatusData> plantStatusData = dHandler.findByTypeGroupedHour(CURR_SENSOR);

        GraphView todayGraph = (GraphView) findViewById(R.id.graph1);
        todayGraph.removeAllSeries();
        todayGraph.getGridLabelRenderer().setNumHorizontalLabels(24);
        todayGraph.getGridLabelRenderer().setTextSize(25);
        todayGraph.getViewport().setXAxisBoundsManual(true);
        todayGraph.getViewport().setMinX(0);
        todayGraph.getViewport().setMaxX(23);
        todayGraph.setTitle("Today");

        GraphView yesterdayGraph = (GraphView) findViewById(R.id.graph2);
        yesterdayGraph.removeAllSeries();
        yesterdayGraph.getGridLabelRenderer().setNumHorizontalLabels(24);
        yesterdayGraph.getGridLabelRenderer().setTextSize(25);
        yesterdayGraph.getViewport().setXAxisBoundsManual(true);
        yesterdayGraph.getViewport().setMinX(0);
        yesterdayGraph.getViewport().setMaxX(23);
        yesterdayGraph.setTitle("Yesterday");


        DataPoint[] todayDataPoint = new DataPoint[plantStatusData.size()];
        DataPoint[] yesterdayDataPoint = new DataPoint[24];
        DataPoint[] minDataPoint = new DataPoint[24];
        DataPoint[] maxDataPoint = new DataPoint[24];


        for(int i=0; i<plantStatusData.size(); i++){
            todayDataPoint[i] = new DataPoint(getHourFromDate(plantStatusData.get(i).getTimeStamp()), plantStatusData.get(i).getValue());
        }

        for(int i=0; i<24; i++) {
            minDataPoint[i] = new DataPoint(i, plantCurrentStatus.getGeneralPlantDataRange(CURR_SENSOR).getMinValue());
            maxDataPoint[i] = new DataPoint(i, plantCurrentStatus.getGeneralPlantDataRange(CURR_SENSOR).getMaxValue());
            yesterdayDataPoint[i] = new DataPoint(i, (int) Math.round(Math.random() * 5) + 20);
        }

        LineGraphSeries<DataPoint> todaySeries = new LineGraphSeries<>(todayDataPoint);
        LineGraphSeries<DataPoint> yesterdaySeries = new LineGraphSeries<>(yesterdayDataPoint);

        LineGraphSeries<DataPoint> minSeries = new LineGraphSeries<>(minDataPoint);
        LineGraphSeries<DataPoint> maxSeries = new LineGraphSeries<>(maxDataPoint);


        todaySeries.setThickness(5);
        yesterdaySeries.setThickness(5);
        minSeries.setThickness(2);
        maxSeries.setThickness(2);
        minSeries.setColor(Color.RED);
        maxSeries.setColor(Color.RED);

        todayGraph.addSeries(todaySeries);
        todayGraph.addSeries(minSeries);
        todayGraph.addSeries(maxSeries);


        yesterdayGraph.addSeries(yesterdaySeries);
        yesterdayGraph.addSeries(minSeries);
        yesterdayGraph.addSeries(maxSeries);
    }


    public static int getCurrentHour(){
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int res = calendar.get(Calendar.HOUR_OF_DAY);
        return res;
    }


    public static int getHourFromDate(Date date){
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int res = calendar.get(Calendar.HOUR_OF_DAY);
        return res;
    }


    // Intent for changing required values
    public void editRequiredValue(View view)
    {
        Intent intent = new Intent(SensorDetailActivity.this, EditRequiredValueActivity.class);
        intent.putExtra("SENSOR", CURR_SENSOR);
        startActivityForResult(intent, 1);
    }

}
