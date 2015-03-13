package com.uni.swansea.happyplant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by michaelwaterworth on 10/03/15.
 */

public class MessageReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if(action.equalsIgnoreCase("NEWMESSAGE")){
            Bundle extra = intent.getExtras();
            String username = extra.getString("username");
            Log.d("DataReceived", username);
        }


    }
}