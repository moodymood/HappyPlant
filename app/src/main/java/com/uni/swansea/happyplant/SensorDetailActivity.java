package com.uni.swansea.happyplant;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
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
        //refreshGraph();
        addLastHourChart();
        addLastDayChart();
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
        //refreshGraph();
        addLastHourChart();
        addLastDayChart();


    }


    private void addCustomReceiver() {
        messageReceiver = new MessageReceiver(){
            @Override
            protected void onMessageReceived(){
                plantCurrentStatus.setGeneralPlantStatusData(this.getBroadcastCurrentStatus().getGeneralPlantStatusData());
                refreshHeader();
                refreshSensorValues();
                // Better not to refresh the time everytime?
                addLastHourChart();
                addLastDayChart();

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

    // Create the min value allowed function
    public LineDataSet getMinValueFunction(int max){

        ArrayList<Entry> minValueList = new ArrayList<Entry>();
        minValueList.add(new Entry(plantCurrentStatus.getGeneralPlantDataRange(CURR_SENSOR).getMinValue(), 0));
        minValueList.add(new Entry(plantCurrentStatus.getGeneralPlantDataRange(CURR_SENSOR).getMinValue(), max));

        LineDataSet minValueDataSet = new LineDataSet(minValueList, "Minimum value allowed");
        // Setting all values to int
        minValueDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        minValueDataSet.setDrawCircles(false);
        // set colour
        minValueDataSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);
        minValueDataSet.setDrawValues(false);

        return minValueDataSet;

    }

    // Create the min value allowed function
    public LineDataSet getMaxValueFunction(int max){

        ArrayList<Entry> maxValueList = new ArrayList<Entry>();
        maxValueList.add(new Entry(plantCurrentStatus.getGeneralPlantDataRange(CURR_SENSOR).getMaxValue(), 0));
        maxValueList.add(new Entry(plantCurrentStatus.getGeneralPlantDataRange(CURR_SENSOR).getMaxValue(), max));

        LineDataSet maxValueDataSet = new LineDataSet(maxValueList, "Maximum value allowed");
        // Setting all values to int
        maxValueDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        maxValueDataSet.setDrawCircles(false);
        // set colour
        maxValueDataSet.setColor(ColorTemplate.COLORFUL_COLORS[0]);
        maxValueDataSet.setDrawValues(false);

        return maxValueDataSet;

    }


    public void addLastHourChart(){

        List<PlantStatusData> plantStatusDataList = dHandler.findByTypeGroupedHourMinutes(CURR_SENSOR);

        LineChart lastHourLineChart = (LineChart) findViewById(R.id.lastHourChart);

        // Creating arraylist with all values

        ArrayList<Entry> lastHourList = new ArrayList<Entry>();
        for(PlantStatusData plantStatusData : plantStatusDataList){
                Entry e = new Entry(plantStatusData.getValue(), getMinFromDate(plantStatusData.getTimeStamp()));
                lastHourList.add(e);
        }


        // Creating the graph function

        LineDataSet lastHourDataSet = new LineDataSet(lastHourList, "Recorded values");
        // Setting bigger size
        lastHourDataSet.setLineWidth(2);
        // Setting all Y values to int
        lastHourDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        ArrayList<LineDataSet> lastHourDatasets = new ArrayList<LineDataSet>();
        lastHourDatasets.add(getMinValueFunction(60));
        lastHourDatasets.add(lastHourDataSet);
        lastHourDatasets.add(getMaxValueFunction(60));



        ArrayList<String> xVals = new ArrayList<String>();
        for(int i=0; i<=60; i++){
            xVals.add(String.valueOf(i));
        }

        LineData lastHourLineData = new LineData(xVals, lastHourDatasets);
        lastHourLineChart.setData(lastHourLineData);


        // Setting X and Y options
        lastHourLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lastHourLineChart.getAxisLeft().setAxisMaxValue(PhidgeMetaInfo.maxValues[CURR_SENSOR]+1);
        lastHourLineChart.getAxisRight().setEnabled(false);

        // Centering the legend label
        lastHourLineChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);

        // Setting all X values to int
        lastHourLineChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // remove grid on touch
        lastHourLineChart.setHighlightIndicatorEnabled(false);

        // Changing description label
        lastHourLineChart.setDescription("Minutes");

        lastHourLineChart.fitScreen();

        lastHourLineChart.invalidate();


    }




    public void addLastDayChart(){

        List<PlantStatusData> plantStatusDataList = dHandler.findByTypeGroupedHour(CURR_SENSOR);

        LineChart lastDayLineChart = (LineChart) findViewById(R.id.lastDayChart);

        ArrayList<Entry> lastDayList = new ArrayList<Entry>();
        for(PlantStatusData plantStatusData : plantStatusDataList){
                Entry e = new Entry(plantStatusData.getValue(), getHourFromDate(plantStatusData.getTimeStamp()));
                lastDayList.add(e);
        }


        LineDataSet lastDayDataSet = new LineDataSet(lastDayList, "Recorded values");
        // Setting bigger size
        lastDayDataSet.setLineWidth(2);
        // Setting all values to int
        lastDayDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });


        ArrayList<LineDataSet> lastDayDatasets = new ArrayList<>();
        lastDayDatasets.add(getMinValueFunction(24));
        lastDayDatasets.add(lastDayDataSet);
        lastDayDatasets.add(getMaxValueFunction(24));



        ArrayList<String> xVals = new ArrayList<>();
        for(int i=0; i<=24; i++){
            xVals.add(String.valueOf(i));
        }

        LineData lastDayLineData = new LineData(xVals, lastDayDatasets);
        lastDayLineChart.setData(lastDayLineData);

        // Setting X and Y options
        lastDayLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lastDayLineChart.getAxisLeft().setAxisMaxValue(PhidgeMetaInfo.maxValues[CURR_SENSOR]);
        lastDayLineChart.getAxisRight().setEnabled(false);

        // Setting all values to int
        lastDayLineChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // Centering the legend
        lastDayLineChart.getLegend().setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);

        // Changing the description label
        lastDayLineChart.setDescription("Hours");

        // remove grid on touch
        lastDayLineChart.setHighlightIndicatorEnabled(false);

        // Refresh the view
        lastDayLineChart.invalidate();


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


    public static int getMinFromDate(Date date){
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int res = calendar.get(Calendar.MINUTE);
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
