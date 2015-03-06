package com.uni.swansea.happyplant;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class SensorDetailActivity extends ActionBarActivity {

    public final int TEMP = 0;
    public final int LIGHT = 1;
    public final int HUM = 2;

    public PlantStatus plantStatus;
    public int CURR_SENSOR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_detail);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            CURR_SENSOR = (int) extras.get("SENSOR");
            plantStatus = (PlantStatus) extras.getSerializable("VALUE");
        }
        TextView currValue = (TextView) findViewById(R.id.currValue);
        currValue.setText(String.valueOf(plantStatus.getValue(CURR_SENSOR)));
        TextView reqValue = (TextView) findViewById(R.id.reqValue);
        reqValue.setText(String.valueOf(plantStatus.reqValues[CURR_SENSOR]));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sensor_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
