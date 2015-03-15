package com.uni.swansea.happyplant;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class PlantCurrentStatus implements Serializable {
    // Phidget sensors must be attached as following:
    PlantStatusData[] generalPlantStatusData;
    PlantDataRange[] generalPlantDataRange;

    public PlantCurrentStatus(PlantStatusData[] generalPlantStatusData, PlantDataRange[] generalPlantDataRange){

        this.generalPlantStatusData = generalPlantStatusData;
        this.generalPlantDataRange = generalPlantDataRange;
    }

    public PlantStatusData[] getGeneralPlantStatusData(){
        return generalPlantStatusData;
    }

    public PlantStatusData getGeneralPlantStatusData(int sensor){
        return generalPlantStatusData[sensor];
    }

    public PlantDataRange[] getGeneralPlantDataRange(){
        return generalPlantDataRange;
    }

    public PlantDataRange getGeneralPlantDataRange(int sensor){
        return generalPlantDataRange[sensor];
    }

    public void setGeneralPlantStatusData(PlantStatusData[] generalPlantStatusData){
        this.generalPlantStatusData = generalPlantStatusData;
    }


    public boolean sensorIsOK(int sensor){
        if(generalPlantStatusData[sensor].getValue() < generalPlantDataRange[sensor].getMaxValue()
            && generalPlantStatusData[sensor].getValue() > generalPlantDataRange[sensor].getMinValue())
            return true;
        else
            return false;
    }


    public boolean plantIsOK(){
        if(!sensorIsOK(PlantMetaInfo.TEMP))
            return false;
        else if(!sensorIsOK(PlantMetaInfo.LIGHT))
            return false;
        else if(!sensorIsOK(PlantMetaInfo.HUM))
            return false;
        else
            return true;
    }

    // Convert the value according to the sensor
    public int convertValue(int sensor, int value){
        // Temperature (C°) = (Sensor value * 0.2222) - 61.111
        if(sensor == PlantMetaInfo.TEMP){
            return (int) Math.round((value * 0.2222) - 61.111);
        }
        // Light (Lux) = (m * Sensor value) + b
        else if(sensor == PlantMetaInfo.LIGHT){
            return (int) Math.round((1.478777 * value) +  33.67076);
        }
        // Humidity (RH%) = (Sensor value * 0.1906) - 40.2
        else{
            return (int) Math.round((value * 0.1906) - 40.2);
        }
    }

    public String toString(){
        String res = "";
        if(generalPlantStatusData!= null & generalPlantDataRange!= null) {
            for (int i = 0; i < 3; i++) {
                res +=
                        "Sensor: " + generalPlantStatusData[i].getType() +
                                " value: " + generalPlantStatusData[i].getValue() +
                                " min: " + generalPlantDataRange[i].getMinValue() +
                                " max: " + generalPlantDataRange[i].getMaxValue() +
                                "\n";
            }
        }
        return res;
    }

}
