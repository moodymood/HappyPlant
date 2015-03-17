package com.uni.swansea.happyplant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by michaelwaterworth on 10/03/15.
 */

public  class MessageReceiver extends BroadcastReceiver
{
    PlantCurrentStatus newPlantCurrentStatus = null;
    boolean phidgetIsConnected = false;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        // Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            newPlantCurrentStatus = (PlantCurrentStatus) extras.getSerializable("CURRSTATUS");
            phidgetIsConnected = (boolean) extras.get("PHIDGCONN");
            //Log.d("Receiver", newPlantCurrentStatus.toString());
        }
        onMessageReceived();
    }

    public PlantCurrentStatus getBroadcastCurrentStatus(){
        return newPlantCurrentStatus;
    }

    public boolean isPhidgetConnected(){
        return phidgetIsConnected;
    }

    protected void onMessageReceived(){
        // Method overridden in the Activity class
    };
}