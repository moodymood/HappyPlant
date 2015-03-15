package com.uni.swansea.happyplant;

/**
 * Created by moody on 15/03/15.
 */
public class PhidgeMetaInfo {

    public static final int TEMP = 0;
    public static final int LIGHT = 1;
    public static final int HUM = 2;

    // need to change min temp to -30
    public static int[] minValues =
            {
                    0,
                   10,
                    1

            };
    public static int[] maxValues =
            {
                   80,
                   95,
                   660
            };


    // Convert the value according to the sensor
    public static int convertValue(int sensor, int value){
        // Temperature (C°) = (Sensor value * 0.22222) - 61.11
        if(sensor == TEMP){
            return (int) Math.round((value * 0.22222) - 61.11);
        }
        // Light (Lux)  = (Sensor value * 0.22222) - 61.11
        else if(sensor == LIGHT){

            return (int) Math.round((value * 0.22222) - 61.11);
        }
        // Humidity (RH%) = (Sensor value * 0.1906) - 40.2
        else{
            return (int) Math.round((value * 0.1906) - 40.2);
        }
    }

    public static int filterValue(int current, int last){
        double a = 0.1;

        return (int) Math.round(last * (1.0 - a) + current * a);

    }
}
